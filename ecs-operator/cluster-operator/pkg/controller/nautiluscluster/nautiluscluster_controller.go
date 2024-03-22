/**
 * Copyright (c) 2018 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package ecscluster

import (
	"context"
	"fmt"
	"time"

	ecsv1alpha1 "github.com/ecs/ecs-operator/pkg/apis/ecs/v1alpha1"
	"github.com/ecs/ecs-operator/pkg/controller/ecs"
	"github.com/ecs/ecs-operator/pkg/util"

	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"

	"k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	"sigs.k8s.io/controller-runtime/pkg/handler"
	"sigs.k8s.io/controller-runtime/pkg/manager"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
	"sigs.k8s.io/controller-runtime/pkg/source"

	log "github.com/sirupsen/logrus"
)

// ReconcileTime is the delay between reconciliations
const ReconcileTime = 30 * time.Second

// Add creates a new ECSCluster Controller and adds it to the Manager. The Manager will set fields on the Controller
// and Start it when the Manager is Started.
func Add(mgr manager.Manager) error {
	return add(mgr, newReconciler(mgr))
}

// newReconciler returns a new reconcile.Reconciler
func newReconciler(mgr manager.Manager) reconcile.Reconciler {
	return &ReconcileECSCluster{client: mgr.GetClient(), scheme: mgr.GetScheme()}
}

// add adds a new Controller to mgr with r as the reconcile.Reconciler
func add(mgr manager.Manager, r reconcile.Reconciler) error {
	// Create a new controller
	c, err := controller.New("ecscluster-controller", mgr, controller.Options{Reconciler: r})
	if err != nil {
		return err
	}

	// Watch for changes to primary resource ECSCluster
	err = c.Watch(&source.Kind{Type: &ecsv1alpha1.ECSCluster{}}, &handler.EnqueueRequestForObject{})
	if err != nil {
		return err
	}

	return nil
}

var _ reconcile.Reconciler = &ReconcileECSCluster{}

// ReconcileECSCluster reconciles a ECSCluster object
type ReconcileECSCluster struct {
	// This client, initialized using mgr.Client() above, is a split client
	// that reads objects from the cache and writes to the apiserver
	client client.Client
	scheme *runtime.Scheme
}

// Reconcile reads that state of the cluster for a ECSCluster object and makes changes based on the state read
// and what is in the ECSCluster.Spec
// Note:
// The Controller will requeue the Request to be processed again if the returned error is non-nil or
// Result.Requeue is true, otherwise upon completion it will remove the work from the queue.
func (r *ReconcileECSCluster) Reconcile(request reconcile.Request) (reconcile.Result, error) {
	log.Printf("Reconciling ECSCluster %s/%s\n", request.Namespace, request.Name)

	// Fetch the ECSCluster instance
	ecsCluster := &ecsv1alpha1.ECSCluster{}
	err := r.client.Get(context.TODO(), request.NamespacedName, ecsCluster)
	if err != nil {
		if errors.IsNotFound(err) {
			// Request object not found, could have been deleted after reconcile request.
			// Owned objects are automatically garbage collected. For additional cleanup logic use finalizers.
			// Return and don't requeue
			log.Printf("ECSCluster %s/%s not found. Ignoring since object must be deleted\n", request.Namespace, request.Name)
			return reconcile.Result{}, nil
		}
		// Error reading the object - requeue the request.
		log.Printf("failed to get ECSCluster: %v", err)
		return reconcile.Result{}, err
	}

	// Set default configuration for unspecified values
	changed := ecsCluster.WithDefaults()
	if changed {
		log.Printf("Setting default settings for ecs-cluster: %s", request.Name)
		if err = r.client.Update(context.TODO(), ecsCluster); err != nil {
			return reconcile.Result{}, err
		}
		return reconcile.Result{Requeue: true}, nil
	}

	err = r.run(ecsCluster)
	if err != nil {
		log.Printf("failed to reconcile ecs cluster (%s): %v", ecsCluster.Name, err)
		return reconcile.Result{}, err
	}

	return reconcile.Result{RequeueAfter: ReconcileTime}, nil
}

func (r *ReconcileECSCluster) run(p *ecsv1alpha1.ECSCluster) (err error) {
	// Clean up zookeeper metadata
	err = r.reconcileFinalizers(p)
	if err != nil {
		log.Printf("failed to clean up zookeeper: %v", err)
		return err
	}

	err = r.deployCluster(p)
	if err != nil {
		log.Printf("failed to deploy cluster: %v", err)
		return err
	}

	err = r.syncClusterSize(p)
	if err != nil {
		log.Printf("failed to sync cluster size: %v", err)
		return err
	}

	err = r.reconcileClusterStatus(p)
	if err != nil {
		log.Printf("failed to reconcile cluster status: %v", err)
		return err
	}
	return nil
}

func (r *ReconcileECSCluster) deployCluster(p *ecsv1alpha1.ECSCluster) (err error) {
	err = r.deployBookie(p)
	if err != nil {
		log.Printf("failed to deploy bookie: %v", err)
		return err
	}

	err = r.deployController(p)
	if err != nil {
		log.Printf("failed to deploy controller: %v", err)
		return err
	}

	err = r.deployNode(p)
	if err != nil {
		log.Printf("failed to deploy segment store: %v", err)
		return err
	}
	return nil
}

func (r *ReconcileECSCluster) deployController(p *ecsv1alpha1.ECSCluster) (err error) {
	pdb := ecs.MakeControllerPodDisruptionBudget(p)
	controllerutil.SetControllerReference(p, pdb, r.scheme)
	err = r.client.Create(context.TODO(), pdb)
	if err != nil && !errors.IsAlreadyExists(err) {
		return err
	}

	configMap := ecs.MakeControllerConfigMap(p)
	controllerutil.SetControllerReference(p, configMap, r.scheme)
	err = r.client.Create(context.TODO(), configMap)
	if err != nil && !errors.IsAlreadyExists(err) {
		return err
	}

	deployment := ecs.MakeControllerDeployment(p)
	controllerutil.SetControllerReference(p, deployment, r.scheme)
	err = r.client.Create(context.TODO(), deployment)
	if err != nil && !errors.IsAlreadyExists(err) {
		return err
	}

	service := ecs.MakeControllerService(p)
	controllerutil.SetControllerReference(p, service, r.scheme)
	err = r.client.Create(context.TODO(), service)
	if err != nil && !errors.IsAlreadyExists(err) {
		return err
	}

	return nil
}

func (r *ReconcileECSCluster) deployNode(p *ecsv1alpha1.ECSCluster) (err error) {

	headlessService := ecs.MakeNodeHeadlessService(p)
	controllerutil.SetControllerReference(p, headlessService, r.scheme)
	err = r.client.Create(context.TODO(), headlessService)
	if err != nil && !errors.IsAlreadyExists(err) {
		return err
	}

	if p.Spec.ExternalAccess.Enabled {
		services := ecs.MakeNodeExternalServices(p)
		for _, service := range services {
			controllerutil.SetControllerReference(p, service, r.scheme)
			err = r.client.Create(context.TODO(), service)
			if err != nil && !errors.IsAlreadyExists(err) {
				return err
			}
		}
	}

	pdb := ecs.MakeNodePodDisruptionBudget(p)
	controllerutil.SetControllerReference(p, pdb, r.scheme)
	err = r.client.Create(context.TODO(), pdb)
	if err != nil && !errors.IsAlreadyExists(err) {
		return err
	}

	configMap := ecs.MakeNodeConfigMap(p)
	controllerutil.SetControllerReference(p, configMap, r.scheme)
	err = r.client.Create(context.TODO(), configMap)
	if err != nil && !errors.IsAlreadyExists(err) {
		return err
	}

	statefulSet := ecs.MakeNodeStatefulSet(p)
	controllerutil.SetControllerReference(p, statefulSet, r.scheme)
	for i := range statefulSet.Spec.VolumeClaimTemplates {
		controllerutil.SetControllerReference(p, &statefulSet.Spec.VolumeClaimTemplates[i], r.scheme)
	}
	err = r.client.Create(context.TODO(), statefulSet)
	if err != nil && !errors.IsAlreadyExists(err) {
		return err
	}

	return nil
}

func (r *ReconcileECSCluster) deployBookie(p *ecsv1alpha1.ECSCluster) (err error) {
	headlessService := ecs.MakeBookieHeadlessService(p)
	controllerutil.SetControllerReference(p, headlessService, r.scheme)
	err = r.client.Create(context.TODO(), headlessService)
	if err != nil && !errors.IsAlreadyExists(err) {
		return err
	}

	pdb := ecs.MakeBookiePodDisruptionBudget(p)
	controllerutil.SetControllerReference(p, pdb, r.scheme)
	err = r.client.Create(context.TODO(), pdb)
	if err != nil && !errors.IsAlreadyExists(err) {
		return err
	}

	configMap := ecs.MakeBookieConfigMap(p)
	controllerutil.SetControllerReference(p, configMap, r.scheme)
	err = r.client.Create(context.TODO(), configMap)
	if err != nil && !errors.IsAlreadyExists(err) {
		return err
	}

	statefulSet := ecs.MakeBookieStatefulSet(p)
	controllerutil.SetControllerReference(p, statefulSet, r.scheme)
	for i := range statefulSet.Spec.VolumeClaimTemplates {
		controllerutil.SetControllerReference(p, &statefulSet.Spec.VolumeClaimTemplates[i], r.scheme)
	}
	err = r.client.Create(context.TODO(), statefulSet)
	if err != nil && !errors.IsAlreadyExists(err) {
		return err
	}

	return nil
}

func (r *ReconcileECSCluster) syncClusterSize(p *ecsv1alpha1.ECSCluster) (err error) {
	err = r.syncBookieSize(p)
	if err != nil {
		return err
	}

	err = r.syncNodeSize(p)
	if err != nil {
		return err
	}

	err = r.syncControllerSize(p)
	if err != nil {
		return err
	}

	return nil
}

func (r *ReconcileECSCluster) syncBookieSize(p *ecsv1alpha1.ECSCluster) (err error) {
	sts := &appsv1.StatefulSet{}
	name := util.StatefulSetNameForBookie(p.Name)
	err = r.client.Get(context.TODO(), types.NamespacedName{Name: name, Namespace: p.Namespace}, sts)
	if err != nil {
		return fmt.Errorf("failed to get stateful-set (%s): %v", sts.Name, err)
	}

	if *sts.Spec.Replicas != p.Spec.Bookkeeper.Replicas {
		sts.Spec.Replicas = &(p.Spec.Bookkeeper.Replicas)
		err = r.client.Update(context.TODO(), sts)
		if err != nil {
			return fmt.Errorf("failed to update size of stateful-set (%s): %v", sts.Name, err)
		}

		err = r.syncStatefulSetPvc(sts)
		if err != nil {
			return fmt.Errorf("failed to sync pvcs of stateful-set (%s): %v", sts.Name, err)
		}
	}
	return nil
}

func (r *ReconcileECSCluster) syncNodeSize(p *ecsv1alpha1.ECSCluster) (err error) {
	sts := &appsv1.StatefulSet{}
	name := util.StatefulSetNameForNode(p.Name)
	err = r.client.Get(context.TODO(), types.NamespacedName{Name: name, Namespace: p.Namespace}, sts)
	if err != nil {
		return fmt.Errorf("failed to get stateful-set (%s): %v", sts.Name, err)
	}

	if *sts.Spec.Replicas != p.Spec.ECS.NodeReplicas {
		sts.Spec.Replicas = &(p.Spec.ECS.NodeReplicas)
		err = r.client.Update(context.TODO(), sts)
		if err != nil {
			return fmt.Errorf("failed to update size of stateful-set (%s): %v", sts.Name, err)
		}

		err = r.syncStatefulSetPvc(sts)
		if err != nil {
			return fmt.Errorf("failed to sync pvcs of stateful-set (%s): %v", sts.Name, err)
		}
	}
	return nil
}

func (r *ReconcileECSCluster) syncControllerSize(p *ecsv1alpha1.ECSCluster) (err error) {
	deploy := &appsv1.Deployment{}
	name := util.DeploymentNameForController(p.Name)
	err = r.client.Get(context.TODO(), types.NamespacedName{Name: name, Namespace: p.Namespace}, deploy)
	if err != nil {
		return fmt.Errorf("failed to get deployment (%s): %v", deploy.Name, err)
	}

	if *deploy.Spec.Replicas != p.Spec.ECS.ControllerReplicas {
		deploy.Spec.Replicas = &(p.Spec.ECS.ControllerReplicas)
		err = r.client.Update(context.TODO(), deploy)
		if err != nil {
			return fmt.Errorf("failed to update size of deployment (%s): %v", deploy.Name, err)
		}
	}
	return nil
}

func (r *ReconcileECSCluster) reconcileFinalizers(p *ecsv1alpha1.ECSCluster) (err error) {
	if p.DeletionTimestamp.IsZero() {
		if !util.ContainsString(p.ObjectMeta.Finalizers, util.ZkFinalizer) {
			p.ObjectMeta.Finalizers = append(p.ObjectMeta.Finalizers, util.ZkFinalizer)
			if err = r.client.Update(context.TODO(), p); err != nil {
				return fmt.Errorf("failed to add the finalizer (%s): %v", p.Name, err)
			}
		}
	} else {
		if util.ContainsString(p.ObjectMeta.Finalizers, util.ZkFinalizer) {
			p.ObjectMeta.Finalizers = util.RemoveString(p.ObjectMeta.Finalizers, util.ZkFinalizer)
			if err = r.client.Update(context.TODO(), p); err != nil {
				return fmt.Errorf("failed to update ECS object (%s): %v", p.Name, err)
			}
			if err = r.cleanUpZookeeperMeta(p); err != nil {
				return fmt.Errorf("failed to clean up metadata (%s): %v", p.Name, err)
			}
		}
	}
	return nil
}

func (r *ReconcileECSCluster) cleanUpZookeeperMeta(p *ecsv1alpha1.ECSCluster) (err error) {
	if err = util.WaitForClusterToTerminate(r.client, p); err != nil {
		return fmt.Errorf("failed to wait for cluster pods termination (%s): %v", p.Name, err)
	}

	if err = util.DeleteAllZnodes(p); err != nil {
		return fmt.Errorf("failed to delete zookeeper znodes for (%s): %v", p.Name, err)
	}
	return nil
}

func (r *ReconcileECSCluster) syncStatefulSetPvc(sts *appsv1.StatefulSet) error {
	selector, err := metav1.LabelSelectorAsSelector(&metav1.LabelSelector{
		MatchLabels: sts.Spec.Template.Labels,
	})
	if err != nil {
		return fmt.Errorf("failed to convert label selector: %v", err)
	}

	pvcList := &corev1.PersistentVolumeClaimList{}
	pvclistOps := &client.ListOptions{
		Namespace:     sts.Namespace,
		LabelSelector: selector,
	}
	err = r.client.List(context.TODO(), pvclistOps, pvcList)
	if err != nil {
		return err
	}

	for _, pvcItem := range pvcList.Items {
		if util.PvcIsOrphan(pvcItem.Name, *sts.Spec.Replicas) {
			pvcDelete := &corev1.PersistentVolumeClaim{
				ObjectMeta: metav1.ObjectMeta{
					Name:      pvcItem.Name,
					Namespace: pvcItem.Namespace,
				},
			}

			err = r.client.Delete(context.TODO(), pvcDelete)
			if err != nil {
				return fmt.Errorf("failed to delete pvc: %v", err)
			}
		}
	}
	return nil
}

func (r *ReconcileECSCluster) reconcileClusterStatus(p *ecsv1alpha1.ECSCluster) error {
	expectedSize := util.GetClusterExpectedSize(p)
	listOps := &client.ListOptions{
		Namespace:     p.Namespace,
		LabelSelector: labels.SelectorFromSet(util.LabelsForECSCluster(p)),
	}
	podList := &corev1.PodList{}
	err := r.client.List(context.TODO(), listOps, podList)
	if err != nil {
		return err
	}

	var (
		readyMembers   []string
		unreadyMembers []string
	)

	for _, p := range podList.Items {
		if util.IsPodReady(&p) {
			readyMembers = append(readyMembers, p.Name)
		} else {
			unreadyMembers = append(unreadyMembers, p.Name)
		}
	}

	if len(readyMembers) == expectedSize {
		p.Status.SetPodsReadyConditionTrue()
	} else {
		p.Status.SetPodsReadyConditionFalse()
	}

	p.Status.Replicas = int32(expectedSize)
	p.Status.CurrentReplicas = int32(len(podList.Items))
	p.Status.ReadyReplicas = int32(len(readyMembers))
	p.Status.Members.Ready = readyMembers
	p.Status.Members.Unready = unreadyMembers

	err = r.client.Status().Update(context.TODO(), p)
	if err != nil {
		return fmt.Errorf("failed to update cluster status: %v", err)
	}
	return nil
}
