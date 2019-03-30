package storageset

import (
	"context"

	appv1alpha1 "github.com/operator-framework/operator-sdk/storageset-operator/pkg/apis/app/v1alpha1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	"sigs.k8s.io/controller-runtime/pkg/handler"
	"sigs.k8s.io/controller-runtime/pkg/manager"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
	"sigs.k8s.io/controller-runtime/pkg/source"
)

var log = logf.Log.WithName("controller_storageset")

/**
* USER ACTION REQUIRED: This is a scaffold file intended for the user to modify with their own Controller
* business logic.  Delete these comments after modifying this file.*
 */

// Add creates a new StorageArray Controller and adds it to the Manager. The Manager will set fields on the Controller
// and Start it when the Manager is Started.
func Add(mgr manager.Manager) error {
	return add(mgr, newReconciler(mgr))
}

// newReconciler returns a new reconcile.Reconciler
func newReconciler(mgr manager.Manager) reconcile.Reconciler {
	return &ReconcileStorageArray{client: mgr.GetClient(), scheme: mgr.GetScheme()}
}

// add adds a new Controller to mgr with r as the reconcile.Reconciler
func add(mgr manager.Manager, r reconcile.Reconciler) error {
	// Create a new controller
	c, err := controller.New("storageset-controller", mgr, controller.Options{Reconciler: r})
	if err != nil {
		return err
	}

	// Watch for changes to primary resource StorageArray
	err = c.Watch(&source.Kind{Type: &appv1alpha1.StorageArray{}}, &handler.EnqueueRequestForObject{})
	if err != nil {
		return err
	}

	// TODO(user): Modify this to be the types you create that are owned by the primary resource
	// Watch for changes to secondary resource Storages and requeue the owner StorageArray
	err = c.Watch(&source.Kind{Type: &corev1.Storage{}}, &handler.EnqueueRequestForOwner{
		IsController: true,
		OwnerType:    &appv1alpha1.StorageArray{},
	})
	if err != nil {
		return err
	}

	return nil
}

var _ reconcile.Reconciler = &ReconcileStorageArray{}

// ReconcileStorageArray reconciles a StorageArray object
type ReconcileStorageArray struct {
	// This client, initialized using mgr.Client() above, is a split client
	// that reads objects from the cache and writes to the apiserver
	client client.Client
	scheme *runtime.Scheme
}

// Reconcile reads that state of the cluster for a StorageArray object and makes changes based on the state read
// and what is in the StorageArray.Spec
// TODO(user): Modify this Reconcile function to implement your Controller logic.  This example creates
// a Storage as an example
// Note:
// The Controller will requeue the Request to be processed again if the returned error is non-nil or
// Result.Requeue is true, otherwise upon completion it will remove the work from the queue.
func (r *ReconcileStorageArray) Reconcile(request reconcile.Request) (reconcile.Result, error) {
	reqLogger := log.WithValues("Request.Namespace", request.Namespace, "Request.Name", request.Name)
	reqLogger.Info("Reconciling StorageArray")

	// Fetch the StorageArray instance
	instance := &appv1alpha1.StorageArray{}
	err := r.client.Get(context.TODO(), request.NamespacedName, instance)
	if err != nil {
		if errors.IsNotFound(err) {
			// Request object not found, could have been deleted after reconcile request.
			// Owned objects are automatically garbage collected. For additional cleanup logic use finalizers.
			// Return and don't requeue
			return reconcile.Result{}, nil
		}
		// Error reading the object - requeue the request.
		return reconcile.Result{}, err
	}

	// Define a new Storage object
	storage := newStorageForCR(instance)

	// Set StorageArray instance as the owner and controller
	if err := controllerutil.SetControllerReference(instance, storage, r.scheme); err != nil {
		return reconcile.Result{}, err
	}

	// Check if this Storage already exists
	found := &corev1.Storage{}
	err = r.client.Get(context.TODO(), types.NamespacedName{Name: storage.Name, Namespace: storage.Namespace}, found)
	if err != nil && errors.IsNotFound(err) {
		reqLogger.Info("Creating a new Storage", "Storage.Namespace", storage.Namespace, "Storage.Name", storage.Name)
		err = r.client.Create(context.TODO(), storage)
		if err != nil {
			return reconcile.Result{}, err
		}

		// Storage created successfully - don't requeue
		return reconcile.Result{}, nil
	} else if err != nil {
		return reconcile.Result{}, err
	}

	// Storage already exists - don't requeue
	reqLogger.Info("Skip reconcile: Storage already exists", "Storage.Namespace", found.Namespace, "Storage.Name", found.Name)
	return reconcile.Result{}, nil
}

// newStorageForCR returns a busybox storage with the same name/namespace as the cr
func newStorageForCR(cr *appv1alpha1.StorageArray) *corev1.Storage {
	labels := map[string]string{
		"app": cr.Name,
	}
	return &corev1.Storage{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Name + "-storage",
			Namespace: cr.Namespace,
			Labels:    labels,
		},
		Spec: corev1.StorageSpec{
			Containers: []corev1.Container{
				{
					Name:    "busybox",
					Image:   "busybox",
					Command: []string{"sleep", "3600"},
				},
			},
		},
	}
}
