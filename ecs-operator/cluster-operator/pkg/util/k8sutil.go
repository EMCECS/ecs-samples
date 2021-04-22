/**
 * Copyright (c) 2018 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package util

import (
	"context"
	"time"

	"github.com/ecs/ecs-operator/pkg/apis/ecs/v1alpha1"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"

	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/util/wait"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

func DownwardAPIEnv() []corev1.EnvVar {
	return []corev1.EnvVar{
		{
			Name: "POD_NAME",
			ValueFrom: &corev1.EnvVarSource{
				FieldRef: &corev1.ObjectFieldSelector{
					APIVersion: "v1",
					FieldPath:  "metadata.name",
				},
			},
		},
		{
			Name: "POD_NAMESPACE",
			ValueFrom: &corev1.EnvVarSource{
				FieldRef: &corev1.ObjectFieldSelector{
					APIVersion: "v1",
					FieldPath:  "metadata.namespace",
				},
			},
		},
	}
}

func PodAntiAffinity(component string, clusterName string) *corev1.Affinity {
	return &corev1.Affinity{
		PodAntiAffinity: &corev1.PodAntiAffinity{
			PreferredDuringSchedulingIgnoredDuringExecution: []corev1.WeightedPodAffinityTerm{
				{
					Weight: 100,
					PodAffinityTerm: corev1.PodAffinityTerm{
						LabelSelector: &metav1.LabelSelector{
							MatchExpressions: []metav1.LabelSelectorRequirement{
								{
									Key:      "component",
									Operator: metav1.LabelSelectorOpIn,
									Values:   []string{component},
								},
								{
									Key:      "ecs_cluster",
									Operator: metav1.LabelSelectorOpIn,
									Values:   []string{clusterName},
								},
							},
						},
						TopologyKey: "kubernetes.io/hostname",
					},
				},
			},
		},
	}
}

// Wait for pods in cluster to be terminated
func WaitForClusterToTerminate(kubeClient client.Client, p *v1alpha1.ECSCluster) (err error) {
	listOptions := &client.ListOptions{
		LabelSelector: labels.SelectorFromSet(LabelsForECSCluster(p)),
	}

	err = wait.Poll(5*time.Second, 2*time.Minute, func() (done bool, err error) {
		podList := &corev1.PodList{}
		err = kubeClient.List(context.TODO(), listOptions, podList)
		if err != nil {
			return false, err
		}

		var names []string
		for i := range podList.Items {
			pod := &podList.Items[i]
			names = append(names, pod.Name)
		}

		if len(names) != 0 {
			return false, nil
		}
		return true, nil
	})

	return err
}

func IsPodReady(pod *corev1.Pod) bool {
	for _, condition := range pod.Status.Conditions {
		if condition.Type == corev1.PodReady && condition.Status == corev1.ConditionTrue {
			return true
		}
	}
	return false
}
