package nautilusupgrade

import (
	"context"
	"fmt"
	"log"
	"strings"
	"time"

	nautilusv1 "github.com/nautilus/cluster-operator/pkg/apis/nautilus/v1"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	rbacv1 "k8s.io/api/rbac/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/client-go/tools/record"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	"sigs.k8s.io/controller-runtime/pkg/handler"
	"sigs.k8s.io/controller-runtime/pkg/manager"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
	"sigs.k8s.io/controller-runtime/pkg/source"

	nautilusapi "github.com/nautilus/go-api"
)

var (
	// operatorImage is the image name of controller-operator. This is needed
	// because the upgrader binary is baked into the same cluster-operator image.
	// This is set at build time using linker flags to be the same as build
	// container image name.
	operatorImage string
)

// Add creates a new NautilusUpgrade Controller and adds it to the Manager. The Manager will set fields on the Controller
// and Start it when the Manager is Started.
func Add(mgr manager.Manager) error {
	return add(mgr, newReconciler(mgr))
}

// newReconciler returns a new reconcile.Reconciler
func newReconciler(mgr manager.Manager) reconcile.Reconciler {
	return &ReconcileNautilusUpgrade{client: mgr.GetClient(), scheme: mgr.GetScheme(), recorder: mgr.GetRecorder("nautilus-upgrader")}
}

// add adds a new Controller to mgr with r as the reconcile.Reconciler
func add(mgr manager.Manager, r reconcile.Reconciler) error {
	// Create a new controller
	c, err := controller.New("nautilusupgrade-controller", mgr, controller.Options{Reconciler: r})
	if err != nil {
		return err
	}

	// Watch for changes to primary resource NautilusUpgrade
	err = c.Watch(&source.Kind{Type: &nautilusv1.NautilusUpgrade{}}, &handler.EnqueueRequestForObject{})
	if err != nil {
		return err
	}

	return nil
}

var _ reconcile.Reconciler = &ReconcileNautilusUpgrade{}

// ReconcileNautilusUpgrade reconciles a NautilusUpgrade object
type ReconcileNautilusUpgrade struct {
	// This client, initialized using mgr.Client() above, is a split client
	// that reads objects from the cache and writes to the apiserver
	client         client.Client
	scheme         *runtime.Scheme
	currentUpgrade *nautilusv1.NautilusUpgrade
	currentCluster *nautilusv1.NautilusCluster
	imagePuller    *nautilusv1.Job
	recorder       record.EventRecorder
}

// SetCurrentUpgradeIfNone checks if there's any existing current upgrade and
// sets a new current upgrade if it wasn't set before.
func (r *ReconcileNautilusUpgrade) SetCurrentUpgradeIfNone(upgrade *nautilusv1.NautilusUpgrade) {
	if r.currentUpgrade == nil {
		r.SetCurrentUpgrade(upgrade)
	}
}

// SetCurrentUpgrade sets the currently active upgrade in the controller.
func (r *ReconcileNautilusUpgrade) SetCurrentUpgrade(upgrade *nautilusv1.NautilusUpgrade) {
	r.currentUpgrade = upgrade
}

// IsCurrentUpgrade compares a given upgrade with the current cluster to check
// if they are the same.
func (r *ReconcileNautilusUpgrade) IsCurrentUpgrade(upgrade *nautilusv1.NautilusUpgrade) bool {
	if upgrade == nil {
		return false
	}

	if (r.currentUpgrade.GetName() == upgrade.GetName()) && (r.currentUpgrade.GetNamespace() == upgrade.GetNamespace()) {
		return true
	}
	return false
}

// ResetCurrentUpgrade resets the current upgrade of the controller.
func (r *ReconcileNautilusUpgrade) ResetCurrentUpgrade(request reconcile.Request) {
	// Reset current upgrade only when the request is from the current upgrade.
	if r.requestFromCurrentUpgrade(request) {
		r.currentUpgrade = nil
	}
}

// findCurrentCluster finds the running cluster.
func (r *ReconcileNautilusUpgrade) findCurrentCluster() (*nautilusv1.NautilusCluster, error) {
	clusterList := &nautilusv1.NautilusClusterList{}
	if err := r.client.List(context.TODO(), &client.ListOptions{}, clusterList); err != nil {
		return nil, fmt.Errorf("failed to list clusters: %v", err)
	}

	var currentCluster *nautilusv1.NautilusCluster
	for _, cluster := range clusterList.Items {
		// The cluster with Phase "Running" is the only active cluster.
		if cluster.Status.Phase == nautilusv1.ClusterPhaseRunning {
			currentCluster = &cluster
			break
		}
	}

	if currentCluster == nil {
		return nil, fmt.Errorf("failed to find currently running cluster")
	}

	return currentCluster, nil
}

// findAndSetCurrentCluster finds the running cluster and sets it as
// currentCluster.
func (r *ReconcileNautilusUpgrade) findAndSetCurrentCluster() error {
	cc, err := r.findCurrentCluster()
	if err != nil {
		return err
	}
	r.currentCluster = cc
	return nil
}

func (r *ReconcileNautilusUpgrade) resetCurrentCluster(request reconcile.Request) {
	// Reset current cluster only when the request is from the current upgrade.
	if r.requestFromCurrentUpgrade(request) {
		r.currentCluster = nil
	}
}

func (r *ReconcileNautilusUpgrade) resetImagePuller(request reconcile.Request) {
	// Reset image puller only when the request is from the current upgrade.
	if r.requestFromCurrentUpgrade(request) {
		r.imagePuller = nil
	}
}

// pauseCluster pauses the current cluster.
func (r *ReconcileNautilusUpgrade) pauseCluster() error {
	r.currentCluster.Spec.Pause = true
	if err := r.client.Update(context.Background(), r.currentCluster); err != nil {
		return err
	}
	if err := r.EnableClusterMaintenance(); err != nil {
		return err
	}
	return nil
}

// resumeCluster resumes the current cluster.
func (r *ReconcileNautilusUpgrade) resumeCluster(request reconcile.Request) error {
	if r.currentCluster == nil {
		// Already resumed and current cluster reset.
		return nil
	}

	// Check if the request is from the current upgrade instance.
	// Current cluster must be updated only when the request is from current
	// upgrade.
	if r.requestFromCurrentUpgrade(request) {
		r.currentCluster.Spec.Pause = false
		if err := r.client.Update(context.Background(), r.currentCluster); err != nil {
			return err
		}
		if err := r.DisableClusterMaintenance(); err != nil {
			return err
		}
	}
	return nil
}

// requestFromCurrentUpgrade checks if the given request is from the current
// upgrade.
func (r *ReconcileNautilusUpgrade) requestFromCurrentUpgrade(request reconcile.Request) bool {
	if r.currentUpgrade.GetName() == request.NamespacedName.Name &&
		r.currentUpgrade.GetNamespace() == request.NamespacedName.Namespace {
		return true
	}
	return false
}

// Reconcile reads that state of the cluster for a NautilusUpgrade object and makes changes based on the state read
// and what is in the NautilusUpgrade.Spec
// TODO(user): Modify this Reconcile function to implement your Controller logic.  This example creates
// a Pod as an example
// Note:
// The Controller will requeue the Request to be processed again if the returned error is non-nil or
// Result.Requeue is true, otherwise upon completion it will remove the work from the queue.
func (r *ReconcileNautilusUpgrade) Reconcile(request reconcile.Request) (reconcile.Result, error) {
	// log.Printf("Reconciling NautilusUpgrade %s/%s\n", request.Namespace, request.Name)

	reconcilePeriod := 10 * time.Second
	reconcileResult := reconcile.Result{RequeueAfter: reconcilePeriod}

	// Fetch the NautilusUpgrade instance
	instance := &nautilusv1.NautilusUpgrade{}
	err := r.client.Get(context.TODO(), request.NamespacedName, instance)
	if err != nil {
		if errors.IsNotFound(err) {
			// Upgrade instance not found. Reset the current cluster.
			// In this order: resume cluster, reset cluster, reset upgrade.
			r.resumeCluster(request)
			r.resetImagePuller(request)
			r.resetCurrentCluster(request)
			r.ResetCurrentUpgrade(request)
			// Request object not found, could have been deleted after reconcile request.
			// Owned objects are automatically garbage collected. For additional cleanup logic use finalizers.
			// Return and don't requeue.
			return reconcile.Result{}, nil
		}
		// Error reading the object - requeue the request.
		return reconcileResult, err
	}

	r.SetCurrentUpgradeIfNone(instance)

	// Check if it's the only upgrade before continuing.
	if !r.IsCurrentUpgrade(instance) {
		err := fmt.Errorf("can't create more than one nautilus upgrade")
		r.recorder.Event(instance, corev1.EventTypeWarning, "FailedCreation", err.Error())
		// Don't requeue. This is a failed upgrade.
		return reconcile.Result{}, err
	}

	// Pull image.
	if r.imagePuller == nil {
		// Get the current cluster. Use this to define node selector for image
		// puller job, based on the cluster's node selector.
		currentCluster, err := r.findCurrentCluster()
		if err != nil {
			// Re-queue if it fails to get the current cluster.
			return reconcileResult, err
		}
		// Create image puller.
		r.imagePuller = newImagePullJob(instance, currentCluster)
		if err := controllerutil.SetControllerReference(instance, r.imagePuller, r.scheme); err != nil {
			return reconcileResult, err
		}
		if err := r.client.Create(context.Background(), r.imagePuller); err != nil && !errors.IsAlreadyExists(err) {
			return reconcileResult, fmt.Errorf("failed to create image puller job: %v", err)
		}

		r.recorder.Event(instance, corev1.EventTypeNormal, "PullImage", "Pulling the new container image")

		// Re-queue, let the puller create and continue.
		return reconcileResult, nil
	}

	// Update the image puller instance and check if it has completed.
	nsdName := types.NamespacedName{
		Name:      r.imagePuller.Name,
		Namespace: r.imagePuller.Namespace,
	}
	err = r.client.Get(context.TODO(), nsdName, r.imagePuller)
	if err != nil {
		log.Println("error fetching image puller status:", err)
	}
	// Re-queue if the image pull didn't complete.
	if !r.imagePuller.Status.Completed {
		return reconcileResult, fmt.Errorf("image pull didn't complete")
	}

	// Find and pause the running cluster.
	if r.currentCluster == nil {
		if err := r.findAndSetCurrentCluster(); err != nil {
			return reconcileResult, err
		}
		if err := r.pauseCluster(); err != nil {
			return reconcileResult, err
		}
		r.recorder.Event(instance, corev1.EventTypeNormal, "PauseClusterCtrl", "Pausing the cluster controller and enabling cluster maintenance mode")
	}

	// Create a ServiceAccount for the upgrader.
	sa := newServiceAccountForCR("nautilus-upgrader-sa", instance)
	if err := controllerutil.SetControllerReference(instance, sa, r.scheme); err != nil {
		return reconcileResult, err
	}
	if err := r.client.Create(context.Background(), sa); err != nil && !errors.IsAlreadyExists(err) {
		return reconcileResult, fmt.Errorf("failed to create service account: %v", err)
	}

	// Create a ClusterRole for the upgrader.
	// This must cluster scoped because the applications can be in any namespace.
	rules := []rbacv1.PolicyRule{
		{
			APIGroups: []string{"apps"},
			Resources: []string{"daemonsets", "deployments", "statefulsets"},
			Verbs:     []string{"get", "list", "update", "patch"},
		},
		{
			APIGroups: []string{""},
			Resources: []string{"pods"},
			Verbs:     []string{"get", "list"},
		},
		{
			APIGroups: []string{""},
			Resources: []string{"persistentvolumeclaims"},
			Verbs:     []string{"get", "list"},
		},
		{
			APIGroups: []string{"storage.k8s.io"},
			Resources: []string{"storageclasses"},
			Verbs:     []string{"get", "list"},
		},
	}
	cr := newClusterRole("nautilus-upgrader-role", rules)
	if err := controllerutil.SetControllerReference(instance, cr, r.scheme); err != nil {
		return reconcileResult, err
	}
	if err := r.client.Create(context.Background(), cr); err != nil && !errors.IsAlreadyExists(err) {
		return reconcileResult, fmt.Errorf("failed to create cluster role: %v", err)
	}

	// Create ClusterRoleBinding for the ServiceAccount.
	subjects := []rbacv1.Subject{
		{
			Kind:      rbacv1.ServiceAccountKind,
			Name:      "nautilus-upgrader-sa",
			Namespace: instance.GetNamespace(),
		},
	}
	roleRef := rbacv1.RoleRef{
		Kind:     "ClusterRole",
		Name:     cr.GetName(),
		APIGroup: rbacv1.GroupName,
	}
	crb := newClusterRoleBinding("nautilus-upgrader-clusterrolebinding", subjects, roleRef)
	if err := controllerutil.SetControllerReference(instance, crb, r.scheme); err != nil {
		return reconcileResult, err
	}
	if err := r.client.Create(context.Background(), crb); err != nil && !errors.IsAlreadyExists(err) {
		return reconcileResult, fmt.Errorf("failed to create cluster role binding: %v", err)
	}

	// Define a new Job object.
	job := newJobForCR(instance)

	// Set NautilusUpgrade instance as the owner and controller.
	if err := controllerutil.SetControllerReference(instance, job, r.scheme); err != nil {
		return reconcileResult, err
	}

	// Check if this Job already exists.
	found := &batchv1.Job{}
	err = r.client.Get(context.TODO(), types.NamespacedName{Name: job.Name, Namespace: job.Namespace}, found)
	if err != nil && errors.IsNotFound(err) {
		err = r.client.Create(context.TODO(), job)
		if err != nil {
			return reconcileResult, err
		}

		// Job created successfully - requeue.
		// Requeue is needed here to enable repeated check on the upgrade
		// process.
		upgradeInitMessage := fmt.Sprintf("Nautilus upgrade of cluster %s started", r.currentCluster.GetName())
		r.recorder.Event(instance, corev1.EventTypeNormal, "UpgradeInit", upgradeInitMessage)
		return reconcileResult, nil
	} else if err != nil {
		return reconcileResult, err
	}

	if r.currentUpgrade.Status.Completed {
		// Upgrade completed. Do nothing.
		return reconcileResult, nil
	}

	if found.Status.Succeeded == 1 {
		r.currentUpgrade.Status.Completed = true
		successMessage := fmt.Sprintf("Nautilus upgraded to %s. Delete upgrade object to disable cluster maintenance mode", instance.Spec.NewImage)
		r.recorder.Event(instance, corev1.EventTypeNormal, "UpgradeComplete", successMessage)
	}

	return reconcileResult, nil
}

// GetNautilusClient returns a Nautilus client.
func (r *ReconcileNautilusUpgrade) GetNautilusClient() (*nautilusapi.Client, error) {
	serviceNamespacedName := types.NamespacedName{
		Namespace: r.currentCluster.Spec.GetResourceNS(),
		Name:      r.currentCluster.Spec.GetServiceName(),
	}
	serviceInstance := &corev1.Service{}
	err := r.client.Get(context.TODO(), serviceNamespacedName, serviceInstance)
	if err != nil {
		return nil, err
	}

	stosCli, err := nautilusapi.NewVersionedClient(strings.Join([]string{serviceInstance.Spec.ClusterIP, nautilusapi.DefaultPort}, ":"), nautilusapi.DefaultVersionStr)
	if err != nil {
		return nil, err
	}

	secretNamespacedName := types.NamespacedName{
		Namespace: r.currentCluster.Spec.SecretRefNamespace,
		Name:      r.currentCluster.Spec.SecretRefName,
	}
	secretInstance := &corev1.Secret{}
	err = r.client.Get(context.TODO(), secretNamespacedName, secretInstance)
	if err != nil {
		return nil, err
	}

	stosCli.SetUserAgent("cluster-operator")
	stosCli.SetAuth(string(secretInstance.Data["apiUsername"]), string(secretInstance.Data["apiPassword"]))

	return stosCli, nil
}

// EnableClusterMaintenance enables maintenance mode in the current cluster.
func (r *ReconcileNautilusUpgrade) EnableClusterMaintenance() error {
	stosCli, err := r.GetNautilusClient()
	if err != nil {
		return err
	}

	if err := stosCli.EnableMaintenance(); err != nil {
		return err
	}
	return nil
}

// DisableClusterMaintenance disables maintenance mode in the current cluster.
func (r *ReconcileNautilusUpgrade) DisableClusterMaintenance() error {
	stosCli, err := r.GetNautilusClient()
	if err != nil {
		return err
	}

	if err := stosCli.DisableMaintenance(); err != nil {
		return err
	}
	return nil
}

// newJobForCR returns a job with the same name/namespace as the cr.
func newJobForCR(cr *nautilusv1.NautilusUpgrade) *batchv1.Job {
	labels := map[string]string{
		"app": cr.Name,
	}

	return &batchv1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Name + "-job",
			Namespace: cr.Namespace,
			Labels:    labels,
		},
		Spec: batchv1.JobSpec{
			Template: corev1.PodTemplateSpec{
				Spec: corev1.PodSpec{
					ServiceAccountName: "nautilus-upgrader-sa",
					Containers: []corev1.Container{
						{
							Image:           operatorImage,
							ImagePullPolicy: corev1.PullIfNotPresent,
							Name:            "nautilus-upgrader",
							Command:         []string{"upgrader"},
							Env: []corev1.EnvVar{
								{
									Name:  "NEW_IMAGE",
									Value: cr.Spec.NewImage,
								},
							},
						},
					},
					RestartPolicy: corev1.RestartPolicyNever,
				},
			},
		},
	}
}

func newServiceAccountForCR(name string, cr *nautilusv1.NautilusUpgrade) *corev1.ServiceAccount {
	return &corev1.ServiceAccount{
		TypeMeta: metav1.TypeMeta{
			APIVersion: "v1",
			Kind:       "ServiceAccount",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      name,
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": "nautilus-upgrader",
			},
		},
	}
}

func newClusterRoleBinding(name string, subjects []rbacv1.Subject, roleRef rbacv1.RoleRef) *rbacv1.ClusterRoleBinding {
	return &rbacv1.ClusterRoleBinding{
		TypeMeta: metav1.TypeMeta{
			APIVersion: "rbac.authorization.k8s.io/v1",
			Kind:       "ClusterRoleBinding",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name: name,
			Labels: map[string]string{
				"app": "nautilus-upgrader",
			},
		},
		Subjects: subjects,
		RoleRef:  roleRef,
	}
}

func newClusterRole(name string, rules []rbacv1.PolicyRule) *rbacv1.ClusterRole {
	return &rbacv1.ClusterRole{
		TypeMeta: metav1.TypeMeta{
			APIVersion: "rbac.authorization.k8s.io/v1",
			Kind:       "ClusterRole",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name: name,
			Labels: map[string]string{
				"app": "nautilus-upgrader",
			},
		},
		Rules: rules,
	}
}

// newImagePullJob creates a jobs.dellemc.com instance for pulling container
// image and return. It uses the cluster instance to set the NodeSelectorTerm,
// in order to pull the image only on the nodes that are part of the cluster.
func newImagePullJob(cr *nautilusv1.NautilusUpgrade, cluster *nautilusv1.NautilusCluster) *nautilusv1.Job {
	return &nautilusv1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Name + "-image-puller",
			Namespace: cr.Namespace,
		},
		Spec: nautilusv1.JobSpec{
			Image:             operatorImage,
			Args:              []string{"docker-puller.sh", cr.Spec.NewImage},
			MountPath:         "/var/run",
			HostPath:          "/var/run",
			CompletionWord:    "done",
			NodeSelectorTerms: cluster.Spec.NodeSelectorTerms,
		},
	}
}
