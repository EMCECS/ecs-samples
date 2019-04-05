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
	"fmt"
	"k8s.io/apimachinery/pkg/api/resource"

	api "github.com/ecs/ecs-operator/pkg/apis/ecs/v1alpha1"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// NewDefaultCluster returns a cluster with an empty spec, which will be filled
// with default values
func NewDefaultCluster(namespace string) *api.ECSCluster {
	return &api.ECSCluster{
		TypeMeta: metav1.TypeMeta{
			Kind:       "ECSCluster",
			APIVersion: "ecs.ecs.io/v1alpha1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      "test",
			Namespace: namespace,
		},
	}
}

func newTestJob(namespace string, command string) *batchv1.Job {
	deadline := int64(180)
	retries := int32(1)
	return &batchv1.Job{
		TypeMeta: metav1.TypeMeta{
			Kind:       "Job",
			APIVersion: "batch/v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			GenerateName: "test-job-",
			Namespace:    namespace,
		},
		Spec: batchv1.JobSpec{
			ActiveDeadlineSeconds: &deadline,
			BackoffLimit:          &retries,

			Template: corev1.PodTemplateSpec{
				Spec: corev1.PodSpec{
					Containers: []corev1.Container{
						{
							Name:            "test-container",
							Image:           "adrianmo/ecs-samples",
							ImagePullPolicy: corev1.PullIfNotPresent,
							Command:         []string{"/bin/sh", "-c"},
							Args:            []string{command},
						},
					},
					RestartPolicy: corev1.RestartPolicyNever,
				},
			},
		},
	}
}

// NewTestWriteReadJob returns a Job that can test ecs cluster by running a sample
func NewTestWriteReadJob(namespace string, controllerUri string) *batchv1.Job {
	command := fmt.Sprintf("cd /samples/ecs-client-examples "+
		"&& bin/helloWorldWriter -u tcp://%s:9090 "+
		"&& bin/helloWorldReader -u tcp://%s:9090",
		controllerUri, controllerUri)
	return newTestJob(namespace, command)
}

func NewTier2(namespace string) *corev1.PersistentVolumeClaim {
	storageName := "nfs"
	return &corev1.PersistentVolumeClaim{
		TypeMeta: metav1.TypeMeta{
			Kind:       "PersistentVolumeClaim",
			APIVersion: "v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      "ecs-tier2",
			Namespace: namespace,
		},
		Spec: corev1.PersistentVolumeClaimSpec{
			StorageClassName: &storageName,
			AccessModes:      []corev1.PersistentVolumeAccessMode{corev1.PersistentVolumeAccessMode(corev1.ReadWriteMany)},
			Resources: corev1.ResourceRequirements{
				Requests: corev1.ResourceList{
					corev1.ResourceStorage: resource.MustParse("5Gi"),
				},
			},
		},
	}
}
