/**
 * Copyright (c) 2018 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package e2eutil

import (
	goctx "context"
	"fmt"
	"testing"
	"time"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/apimachinery/pkg/util/wait"

	framework "github.com/operator-framework/operator-sdk/pkg/test"
	api "github.com/ecs/ecs-operator/pkg/apis/ecs/v1alpha1"
	"github.com/ecs/ecs-operator/pkg/util"
	apierrors "k8s.io/apimachinery/pkg/api/errors"
)

var (
	RetryInterval        = time.Second * 5
	Timeout              = time.Second * 60
	CleanupRetryInterval = time.Second * 1
	CleanupTimeout       = time.Second * 5
)

// CreateCluster creates a ECSCluster CR with the desired spec
func CreateCluster(t *testing.T, f *framework.Framework, ctx *framework.TestCtx, p *api.ECSCluster) (*api.ECSCluster, error) {
	t.Logf("creating ecs cluster: %s", p.Name)
	err := f.Client.Create(goctx.TODO(), p, &framework.CleanupOptions{TestContext: ctx, Timeout: CleanupTimeout, RetryInterval: CleanupRetryInterval})
	if err != nil {
		return nil, fmt.Errorf("failed to create CR: %v", err)
	}

	ecs := &api.ECSCluster{}
	err = f.Client.Get(goctx.TODO(), types.NamespacedName{Namespace: p.Namespace, Name: p.Name}, ecs)
	if err != nil {
		return nil, fmt.Errorf("failed to obtain created CR: %v", err)
	}
	t.Logf("created ecs cluster: %s", ecs.Name)
	return ecs, nil
}

// DeleteCluster deletes the ECSCluster CR specified by cluster spec
func DeleteCluster(t *testing.T, f *framework.Framework, ctx *framework.TestCtx, p *api.ECSCluster) error {
	t.Logf("deleting ecs cluster: %s", p.Name)
	err := f.Client.Delete(goctx.TODO(), p)
	if err != nil {
		return fmt.Errorf("failed to delete CR: %v", err)
	}

	t.Logf("deleted ecs cluster: %s", p.Name)
	return nil
}

// UpdateCluster updates the ECSCluster CR
func UpdateCluster(t *testing.T, f *framework.Framework, ctx *framework.TestCtx, p *api.ECSCluster) error {
	t.Logf("updating ecs cluster: %s", p.Name)
	err := f.Client.Update(goctx.TODO(), p)
	if err != nil {
		return fmt.Errorf("failed to update CR: %v", err)
	}

	t.Logf("updated ecs cluster: %s", p.Name)
	return nil
}

// GetCluster returns the latest ECSCluster CR
func GetCluster(t *testing.T, f *framework.Framework, ctx *framework.TestCtx, p *api.ECSCluster) (*api.ECSCluster, error) {
	ecs := &api.ECSCluster{}
	err := f.Client.Get(goctx.TODO(), types.NamespacedName{Namespace: p.Namespace, Name: p.Name}, ecs)
	if err != nil {
		return nil, fmt.Errorf("failed to obtain created CR: %v", err)
	}
	return ecs, nil
}

// WaitForClusterToBecomeReady will wait until all cluster pods are ready
func WaitForClusterToBecomeReady(t *testing.T, f *framework.Framework, ctx *framework.TestCtx, p *api.ECSCluster, size int) error {
	t.Logf("waiting for cluster pods to become ready: %s", p.Name)

	err := wait.Poll(RetryInterval, 5*time.Minute, func() (done bool, err error) {
		cluster, err := GetCluster(t, f, ctx, p)
		if err != nil {
			return false, err
		}

		t.Logf("\twaiting for pods to become ready (%d/%d), pods (%v)", cluster.Status.ReadyReplicas, size, cluster.Status.Members.Ready)

		_, condition := cluster.Status.GetClusterCondition(api.ClusterConditionPodsReady)
		if condition != nil && condition.Status == corev1.ConditionTrue && cluster.Status.ReadyReplicas == int32(size) {
			return true, nil
		}
		return false, nil
	})

	if err != nil {
		return err
	}

	t.Logf("ecs cluster ready: %s", p.Name)
	return nil
}

// WaitForClusterToTerminate will wait until all cluster pods are terminated
func WaitForClusterToTerminate(t *testing.T, f *framework.Framework, ctx *framework.TestCtx, p *api.ECSCluster) error {
	t.Logf("waiting for ecs cluster to terminate: %s", p.Name)

	listOptions := metav1.ListOptions{
		LabelSelector: labels.SelectorFromSet(util.LabelsForECSCluster(p)).String(),
	}

	// Wait for Pods to terminate
	err := wait.Poll(RetryInterval, 2*time.Minute, func() (done bool, err error) {
		podList, err := f.KubeClient.Core().Pods(p.Namespace).List(listOptions)
		if err != nil {
			return false, err
		}

		var names []string
		for i := range podList.Items {
			pod := &podList.Items[i]
			names = append(names, pod.Name)
		}
		t.Logf("waiting for pods to terminate, running pods (%v)", names)
		if len(names) != 0 {
			return false, nil
		}
		return true, nil
	})

	if err != nil {
		return err
	}

	// Wait for PVCs to terminate
	err = wait.Poll(RetryInterval, 1*time.Minute, func() (done bool, err error) {
		pvcList, err := f.KubeClient.Core().PersistentVolumeClaims(p.Namespace).List(listOptions)
		if err != nil {
			return false, err
		}

		var names []string
		for i := range pvcList.Items {
			pvc := &pvcList.Items[i]
			names = append(names, pvc.Name)
		}
		t.Logf("waiting for pvc to terminate (%v)", names)
		if len(names) != 0 {
			return false, nil
		}
		return true, nil
	})

	if err != nil {
		return err
	}

	t.Logf("ecs cluster terminated: %s", p.Name)
	return nil
}

// WriteAndReadData writes sample data and reads it back from the given ECS cluster
func WriteAndReadData(t *testing.T, f *framework.Framework, ctx *framework.TestCtx, p *api.ECSCluster) error {
	t.Logf("writing and reading data from ecs cluster: %s", p.Name)
	testJob := NewTestWriteReadJob(p.Namespace, util.ServiceNameForController(p.Name))
	err := f.Client.Create(goctx.TODO(), testJob, &framework.CleanupOptions{TestContext: ctx, Timeout: CleanupTimeout, RetryInterval: CleanupRetryInterval})
	if err != nil {
		return fmt.Errorf("failed to create job: %s", err)
	}

	err = wait.Poll(RetryInterval, 3*time.Minute, func() (done bool, err error) {
		job, err := f.KubeClient.BatchV1().Jobs(p.Namespace).Get(testJob.Name, metav1.GetOptions{IncludeUninitialized: false})
		if err != nil {
			return false, err
		}
		if job.Status.CompletionTime.IsZero() {
			return false, nil
		}
		if job.Status.Failed > 0 {
			return false, fmt.Errorf("failed to write and read data from cluster")
		}
		return true, nil
	})

	if err != nil {
		return err
	}

	t.Logf("ecs cluster validated: %s", p.Name)
	return nil
}

func RestartTier2(t *testing.T, f *framework.Framework, ctx *framework.TestCtx, namespace string) error {
	t.Log("restarting tier2 storage")
	tier2 := NewTier2(namespace)

	err := f.Client.Delete(goctx.TODO(), tier2)
	if err != nil {
		return fmt.Errorf("failed to delete tier2: %v", err)
	}

	err = wait.Poll(RetryInterval, 3*time.Minute, func() (done bool, err error) {
		_, err = f.KubeClient.CoreV1().PersistentVolumeClaims(namespace).Get(tier2.Name, metav1.GetOptions{IncludeUninitialized: false})
		if err != nil {
			if apierrors.IsNotFound(err) {
				return true, nil
			}
			return false, err
		}
		return false, nil
	})

	if err != nil {
		return fmt.Errorf("failed to wait for tier2 termination: %s", err)
	}

	tier2 = NewTier2(namespace)
	err = f.Client.Create(goctx.TODO(), tier2, &framework.CleanupOptions{TestContext: ctx, Timeout: CleanupTimeout, RetryInterval: CleanupRetryInterval})
	if err != nil {
		return fmt.Errorf("failed to create tier2: %s", err)
	}

	t.Logf("ecs cluster tier2 restarted")
	return nil
}

func CheckPvcSanity(t *testing.T, f *framework.Framework, ctx *framework.TestCtx, p *api.ECSCluster) error {
	t.Logf("checking pvc sanity: %s", p.Name)
	listOptions := metav1.ListOptions{
		LabelSelector: labels.SelectorFromSet(util.LabelsForBookie(p)).String(),
	}
	pvcList, err := f.KubeClient.CoreV1().PersistentVolumeClaims(p.Namespace).List(listOptions)
	if err != nil {
		return err
	}

	for _, pvc := range pvcList.Items {
		if pvc.Status.Phase != corev1.ClaimBound {
			continue
		}
		if util.PvcIsOrphan(pvc.Name, p.Spec.Bookkeeper.Replicas) {
			return fmt.Errorf("bookie pvc is illegal")
		}

	}

	listOptions = metav1.ListOptions{
		LabelSelector: labels.SelectorFromSet(util.LabelsForNode(p)).String(),
	}
	pvcList, err = f.KubeClient.CoreV1().PersistentVolumeClaims(p.Namespace).List(listOptions)
	if err != nil {
		return err
	}

	for _, pvc := range pvcList.Items {
		if pvc.Status.Phase != corev1.ClaimBound {
			continue
		}
		if util.PvcIsOrphan(pvc.Name, p.Spec.ECS.NodeReplicas) {
			return fmt.Errorf("segment store pvc is illegal")
		}

	}

	t.Logf("pvc validated: %s", p.Name)
	return nil
}
