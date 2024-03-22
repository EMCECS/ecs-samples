/**
 * Copyright (c) 2018 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package ecs

import (
	"fmt"
	"strings"

	"github.com/ecs/ecs-operator/pkg/apis/ecs/v1alpha1"
	"github.com/ecs/ecs-operator/pkg/util"
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	policyv1beta1 "k8s.io/api/policy/v1beta1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
)

const (
	LedgerDiskName  = "ledger"
	JournalDiskName = "journal"
	IndexDiskName   = "index"
)

func MakeBookieHeadlessService(ecsCluster *v1alpha1.ECSCluster) *corev1.Service {
	return &corev1.Service{
		TypeMeta: metav1.TypeMeta{
			Kind:       "Service",
			APIVersion: "v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      util.HeadlessServiceNameForBookie(ecsCluster.Name),
			Namespace: ecsCluster.Namespace,
			Labels:    util.LabelsForBookie(ecsCluster),
		},
		Spec: corev1.ServiceSpec{
			Ports: []corev1.ServicePort{
				{
					Name: "bookie",
					Port: 3181,
				},
			},
			Selector:  util.LabelsForBookie(ecsCluster),
			ClusterIP: corev1.ClusterIPNone,
		},
	}
}

func MakeBookieStatefulSet(ecsCluster *v1alpha1.ECSCluster) *appsv1.StatefulSet {
	return &appsv1.StatefulSet{
		TypeMeta: metav1.TypeMeta{
			Kind:       "StatefulSet",
			APIVersion: "apps/v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      util.StatefulSetNameForBookie(ecsCluster.Name),
			Namespace: ecsCluster.Namespace,
			Labels:    util.LabelsForBookie(ecsCluster),
		},
		Spec: appsv1.StatefulSetSpec{
			ServiceName:         util.HeadlessServiceNameForBookie(ecsCluster.Name),
			Replicas:            &ecsCluster.Spec.Bookkeeper.Replicas,
			PodManagementPolicy: appsv1.ParallelPodManagement,
			Template:            makeBookieStatefulTemplate(ecsCluster),
			Selector: &metav1.LabelSelector{
				MatchLabels: util.LabelsForBookie(ecsCluster),
			},
			VolumeClaimTemplates: makeBookieVolumeClaimTemplates(ecsCluster.Spec.Bookkeeper),
		},
	}
}

func makeBookieStatefulTemplate(ecsCluster *v1alpha1.ECSCluster) corev1.PodTemplateSpec {
	return corev1.PodTemplateSpec{
		ObjectMeta: metav1.ObjectMeta{
			Labels: util.LabelsForBookie(ecsCluster),
		},
		Spec: *makeBookiePodSpec(ecsCluster.Name, ecsCluster.Spec.Bookkeeper),
	}
}

func makeBookiePodSpec(clusterName string, bookkeeperSpec *v1alpha1.BookkeeperSpec) *corev1.PodSpec {
	podSpec := &corev1.PodSpec{
		Containers: []corev1.Container{
			{
				Name:            "bookie",
				Image:           bookkeeperSpec.Image.String(),
				ImagePullPolicy: bookkeeperSpec.Image.PullPolicy,
				Ports: []corev1.ContainerPort{
					{
						Name:          "bookie",
						ContainerPort: 3181,
					},
				},
				EnvFrom: []corev1.EnvFromSource{
					{
						ConfigMapRef: &corev1.ConfigMapEnvSource{
							LocalObjectReference: corev1.LocalObjectReference{
								Name: util.ConfigMapNameForBookie(clusterName),
							},
						},
					},
				},
				VolumeMounts: []corev1.VolumeMount{
					{
						Name:      LedgerDiskName,
						MountPath: "/bk/journal",
					},
					{
						Name:      JournalDiskName,
						MountPath: "/bk/ledgers",
					},
					{
						Name:      IndexDiskName,
						MountPath: "/bk/index",
					},
				},
				Resources: *bookkeeperSpec.Resources,
				ReadinessProbe: &corev1.Probe{
					Handler: corev1.Handler{
						Exec: &corev1.ExecAction{
							Command: []string{"/bin/sh", "-c", "/opt/bookkeeper/bin/bookkeeper shell bookiesanity"},
						},
					},
					// Bookie pods should start fast. We give it up to 1.5 minute to become ready.
					InitialDelaySeconds: 20,
					PeriodSeconds:       10,
					FailureThreshold:    9,
				},
				LivenessProbe: &corev1.Probe{
					Handler: corev1.Handler{
						Exec: &corev1.ExecAction{
							Command: util.HealthcheckCommand(3181),
						},
					},
					// We start the liveness probe from the maximum time the pod can take
					// before becoming ready.
					// If the pod fails the health check during 1 minute, Kubernetes
					// will restart it.
					InitialDelaySeconds: 60,
					PeriodSeconds:       15,
					FailureThreshold:    4,
				},
			},
		},
		Affinity: util.PodAntiAffinity("bookie", clusterName),
	}

	if bookkeeperSpec.ServiceAccountName != "" {
		podSpec.ServiceAccountName = bookkeeperSpec.ServiceAccountName
	}

	return podSpec
}

func makeBookieVolumeClaimTemplates(spec *v1alpha1.BookkeeperSpec) []corev1.PersistentVolumeClaim {
	return []corev1.PersistentVolumeClaim{
		{
			ObjectMeta: metav1.ObjectMeta{
				Name: JournalDiskName,
			},
			Spec: *spec.Storage.JournalVolumeClaimTemplate,
		},
		{
			ObjectMeta: metav1.ObjectMeta{
				Name: LedgerDiskName,
			},
			Spec: *spec.Storage.LedgerVolumeClaimTemplate,
		},
		{
			ObjectMeta: metav1.ObjectMeta{
				Name: IndexDiskName,
			},
			Spec: *spec.Storage.IndexVolumeClaimTemplate,
		},
	}
}

func MakeBookieConfigMap(ecsCluster *v1alpha1.ECSCluster) *corev1.ConfigMap {
	memoryOpts := []string{
		"-Xms1g",
		"-XX:+UnlockExperimentalVMOptions",
		"-XX:+UseCGroupMemoryLimitForHeap",
		"-XX:MaxRAMFraction=2",
		"-XX:MaxDirectMemorySize=1g",
		"-XX:+ExitOnOutOfMemoryError",
		"-XX:+CrashOnOutOfMemoryError",
		"-XX:+HeapDumpOnOutOfMemoryError",
	}

	gcOpts := []string{
		"-XX:+UseG1GC",
		"-XX:MaxGCPauseMillis=10",
		"-XX:+ParallelRefProcEnabled",
		"-XX:+AggressiveOpts",
		"-XX:+DoEscapeAnalysis",
		"-XX:ParallelGCThreads=32",
		"-XX:ConcGCThreads=32",
		"-XX:G1NewSizePercent=50",
		"-XX:+DisableExplicitGC",
		"-XX:-ResizePLAB",
	}

	gcLoggingOpts := []string{
		"-XX:+PrintGCDetails",
		"-XX:+PrintGCDateStamps",
		"-XX:+PrintGCApplicationStoppedTime",
		"-XX:+UseGCLogFileRotation",
		"-XX:NumberOfGCLogFiles=5",
		"-XX:GCLogFileSize=64m",
	}

	configData := map[string]string{
		"BOOKIE_MEM_OPTS":        strings.Join(memoryOpts, " "),
		"BOOKIE_GC_OPTS":         strings.Join(gcOpts, " "),
		"BOOKIE_GC_LOGGING_OPTS": strings.Join(gcLoggingOpts, " "),
		"ZK_URL":                 ecsCluster.Spec.ZookeeperUri,
		// Set useHostNameAsBookieID to false until BookKeeper Docker
		// image is updated to 4.7
		// This value can be explicitly overridden when using the operator
		// with images based on BookKeeper 4.7 or newer
		"BK_useHostNameAsBookieID": "false",
		"ECS_CLUSTER_NAME":     ecsCluster.ObjectMeta.Name,
		"WAIT_FOR":                 ecsCluster.Spec.ZookeeperUri,
	}

	if *ecsCluster.Spec.Bookkeeper.AutoRecovery {
		configData["BK_AUTORECOVERY"] = "true"
	}

	for k, v := range ecsCluster.Spec.Bookkeeper.Options {
		prefixKey := fmt.Sprintf("BK_%s", k)
		configData[prefixKey] = v
	}

	return &corev1.ConfigMap{
		TypeMeta: metav1.TypeMeta{
			Kind:       "ConfigMap",
			APIVersion: "v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      util.ConfigMapNameForBookie(ecsCluster.Name),
			Namespace: ecsCluster.ObjectMeta.Namespace,
		},
		Data: configData,
	}
}

func MakeBookiePodDisruptionBudget(ecsCluster *v1alpha1.ECSCluster) *policyv1beta1.PodDisruptionBudget {
	maxUnavailable := intstr.FromInt(1)
	return &policyv1beta1.PodDisruptionBudget{
		TypeMeta: metav1.TypeMeta{
			Kind:       "PodDisruptionBudget",
			APIVersion: "policy/v1beta1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      util.PdbNameForBookie(ecsCluster.Name),
			Namespace: ecsCluster.Namespace,
		},
		Spec: policyv1beta1.PodDisruptionBudgetSpec{
			MaxUnavailable: &maxUnavailable,
			Selector: &metav1.LabelSelector{
				MatchLabels: util.LabelsForBookie(ecsCluster),
			},
		},
	}
}
