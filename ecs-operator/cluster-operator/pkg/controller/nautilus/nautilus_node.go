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
	"strings"

	"fmt"

	api "github.com/ecs/ecs-operator/pkg/apis/ecs/v1alpha1"
	"github.com/ecs/ecs-operator/pkg/util"
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	policyv1beta1 "k8s.io/api/policy/v1beta1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
)

const (
	cacheVolumeName       = "cache"
	cacheVolumeMountPoint = "/tmp/ecs/cache"
	tier2FileMountPoint   = "/mnt/tier2"
	tier2VolumeName       = "tier2"
	nodeKind      = "ecs-node"
)

func MakeNodeStatefulSet(ecsCluster *api.ECSCluster) *appsv1.StatefulSet {
	return &appsv1.StatefulSet{
		TypeMeta: metav1.TypeMeta{
			Kind:       "StatefulSet",
			APIVersion: "apps/v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      util.StatefulSetNameForNode(ecsCluster.Name),
			Namespace: ecsCluster.Namespace,
		},
		Spec: appsv1.StatefulSetSpec{
			ServiceName:         "ecs-node",
			Replicas:            &ecsCluster.Spec.ECS.NodeReplicas,
			PodManagementPolicy: appsv1.OrderedReadyPodManagement,
			Template: corev1.PodTemplateSpec{
				ObjectMeta: metav1.ObjectMeta{
					Labels: util.LabelsForNode(ecsCluster),
				},
				Spec: makeNodePodSpec(ecsCluster),
			},
			Selector: &metav1.LabelSelector{
				MatchLabels: util.LabelsForNode(ecsCluster),
			},
			VolumeClaimTemplates: makeCacheVolumeClaimTemplate(ecsCluster.Spec.ECS),
		},
	}
}

func makeNodePodSpec(ecsCluster *api.ECSCluster) corev1.PodSpec {
	environment := []corev1.EnvFromSource{
		{
			ConfigMapRef: &corev1.ConfigMapEnvSource{
				LocalObjectReference: corev1.LocalObjectReference{
					Name: util.ConfigMapNameForNode(ecsCluster.Name),
				},
			},
		},
	}

	ecsSpec := ecsCluster.Spec.ECS

	environment = configureTier2Secrets(environment, ecsSpec)

	podSpec := corev1.PodSpec{
		Containers: []corev1.Container{
			{
				Name:            "ecs-node",
				Image:           ecsSpec.Image.String(),
				ImagePullPolicy: ecsSpec.Image.PullPolicy,
				Args: []string{
					"node",
				},
				Ports: []corev1.ContainerPort{
					{
						Name:          "server",
						ContainerPort: 12345,
					},
				},
				EnvFrom: environment,
				Env:     util.DownwardAPIEnv(),
				VolumeMounts: []corev1.VolumeMount{
					{
						Name:      cacheVolumeName,
						MountPath: cacheVolumeMountPoint,
					},
				},
				Resources: *ecsSpec.NodeResources,
				ReadinessProbe: &corev1.Probe{
					Handler: corev1.Handler{
						Exec: &corev1.ExecAction{
							Command: util.HealthcheckCommand(12345),
						},
					},
					// Segment Stores can take a few minutes to become ready when the cluster
					// is configured with external enabled as they need to wait for the allocation
					// of the external IP address.
					// This config gives it up to 5 minutes to become ready.
					PeriodSeconds:    10,
					FailureThreshold: 30,
				},
				LivenessProbe: &corev1.Probe{
					Handler: corev1.Handler{
						Exec: &corev1.ExecAction{
							Command: util.HealthcheckCommand(12345),
						},
					},
					// In the readiness probe we allow the pod to take up to 5 minutes
					// to become ready. Therefore, the liveness probe will give it
					// a 5-minute grace period before starting monitoring the container.
					// If the pod fails the health check during 1 minute, Kubernetes
					// will restart it.
					InitialDelaySeconds: 300,
					PeriodSeconds:       15,
					FailureThreshold:    4,
				},
			},
		},
		Affinity: util.PodAntiAffinity("ecs-node", ecsCluster.Name),
	}

	if ecsSpec.NodeServiceAccountName != "" {
		podSpec.ServiceAccountName = ecsSpec.NodeServiceAccountName
	}

	configureTier2Filesystem(&podSpec, ecsSpec)

	return podSpec
}

func MakeNodeConfigMap(p *api.ECSCluster) *corev1.ConfigMap {
	javaOpts := []string{
		"-Xms1g",
		"-XX:+UnlockExperimentalVMOptions",
		"-XX:+UseCGroupMemoryLimitForHeap",
		"-XX:MaxRAMFraction=2",
		"-XX:+ExitOnOutOfMemoryError",
		"-XX:+CrashOnOutOfMemoryError",
		"-XX:+HeapDumpOnOutOfMemoryError",
		"-Decsservice.clusterName=" + p.Name,
	}

	for name, value := range p.Spec.ECS.Options {
		javaOpts = append(javaOpts, fmt.Sprintf("-D%v=%v", name, value))
	}

	configData := map[string]string{
		"AUTHORIZATION_ENABLED": "false",
		"CLUSTER_NAME":          p.Name,
		"ZK_URL":                p.Spec.ZookeeperUri,
		"JAVA_OPTS":             strings.Join(javaOpts, " "),
		"CONTROLLER_URL":        util.ECSControllerServiceURL(*p),
	}

	// Wait for at least 3 Bookies to come up
	var waitFor []string
	for i := int32(0); i < util.Min(3, p.Spec.Bookkeeper.Replicas); i++ {
		waitFor = append(waitFor,
			fmt.Sprintf("%s-%d.%s.%s:3181",
				util.StatefulSetNameForBookie(p.Name),
				i,
				util.HeadlessServiceNameForBookie(p.Name),
				p.Namespace))
	}
	configData["WAIT_FOR"] = strings.Join(waitFor, ",")

	if p.Spec.ExternalAccess.Enabled {
		configData["K8_EXTERNAL_ACCESS"] = "true"
	}

	if p.Spec.ECS.DebugLogging {
		configData["log.level"] = "DEBUG"
	}

	for k, v := range getTier2StorageOptions(p.Spec.ECS) {
		configData[k] = v
	}

	return &corev1.ConfigMap{
		TypeMeta: metav1.TypeMeta{
			Kind:       "ConfigMap",
			APIVersion: "v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      util.ConfigMapNameForNode(p.Name),
			Namespace: p.Namespace,
			Labels:    util.LabelsForNode(p),
		},
		Data: configData,
	}
}

func makeCacheVolumeClaimTemplate(ecsSpec *api.ECSSpec) []corev1.PersistentVolumeClaim {
	return []corev1.PersistentVolumeClaim{
		{
			ObjectMeta: metav1.ObjectMeta{
				Name: cacheVolumeName,
			},
			Spec: *ecsSpec.CacheVolumeClaimTemplate,
		},
	}
}

func getTier2StorageOptions(ecsSpec *api.ECSSpec) map[string]string {
	if ecsSpec.Tier2.FileSystem != nil {
		return map[string]string{
			"TIER2_STORAGE": "FILESYSTEM",
			"NFS_MOUNT":     tier2FileMountPoint,
		}
	}

	if ecsSpec.Tier2.ECS != nil {
		// EXTENDEDS3_ACCESS_KEY_ID & EXTENDEDS3_SECRET_KEY will come from secret storage
		return map[string]string{
			"TIER2_STORAGE":        "EXTENDEDS3",
			"EXTENDEDS3_BUCKET":    ecsSpec.Tier2.ECS.Bucket,
			"EXTENDEDS3_URI":       ecsSpec.Tier2.ECS.Uri,
			"EXTENDEDS3_ROOT":      ecsSpec.Tier2.ECS.Root,
			"EXTENDEDS3_NAMESPACE": ecsSpec.Tier2.ECS.Namespace,
		}
	}

	if ecsSpec.Tier2.Hdfs != nil {
		return map[string]string{
			"TIER2_STORAGE": "HDFS",
			"HDFS_URL":      ecsSpec.Tier2.Hdfs.Uri,
			"HDFS_ROOT":     ecsSpec.Tier2.Hdfs.Root,
		}
	}

	return make(map[string]string)
}

func configureTier2Secrets(environment []corev1.EnvFromSource, ecsSpec *api.ECSSpec) []corev1.EnvFromSource {
	if ecsSpec.Tier2.ECS != nil {
		return append(environment, corev1.EnvFromSource{
			Prefix: "EXTENDEDS3_",
			SecretRef: &corev1.SecretEnvSource{
				LocalObjectReference: corev1.LocalObjectReference{
					Name: ecsSpec.Tier2.ECS.Credentials,
				},
			},
		})
	}

	return environment
}

func configureTier2Filesystem(podSpec *corev1.PodSpec, ecsSpec *api.ECSSpec) {

	if ecsSpec.Tier2.FileSystem != nil {
		podSpec.Containers[0].VolumeMounts = append(podSpec.Containers[0].VolumeMounts, corev1.VolumeMount{
			Name:      tier2VolumeName,
			MountPath: tier2FileMountPoint,
		})

		podSpec.Volumes = append(podSpec.Volumes, corev1.Volume{
			Name: tier2VolumeName,
			VolumeSource: corev1.VolumeSource{
				PersistentVolumeClaim: ecsSpec.Tier2.FileSystem.PersistentVolumeClaim,
			},
		})
	}
}

func MakeNodeHeadlessService(ecsCluster *api.ECSCluster) *corev1.Service {
	return &corev1.Service{
		TypeMeta: metav1.TypeMeta{
			Kind:       "Service",
			APIVersion: "v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      util.HeadlessServiceNameForNode(ecsCluster.Name),
			Namespace: ecsCluster.Namespace,
			Labels:    util.LabelsForNode(ecsCluster),
		},
		Spec: corev1.ServiceSpec{
			Ports: []corev1.ServicePort{
				{
					Name:     "server",
					Port:     12345,
					Protocol: "TCP",
				},
			},
			Selector:  util.LabelsForNode(ecsCluster),
			ClusterIP: corev1.ClusterIPNone,
		},
	}
}

func MakeNodeExternalServices(ecsCluster *api.ECSCluster) []*corev1.Service {
	var service *corev1.Service
	services := make([]*corev1.Service, ecsCluster.Spec.ECS.NodeReplicas)

	for i := int32(0); i < ecsCluster.Spec.ECS.NodeReplicas; i++ {
		service = &corev1.Service{
			TypeMeta: metav1.TypeMeta{
				Kind:       "Service",
				APIVersion: "v1",
			},
			ObjectMeta: metav1.ObjectMeta{
				Name:      util.ServiceNameForNode(ecsCluster.Name, i),
				Namespace: ecsCluster.Namespace,
				Labels:    util.LabelsForNode(ecsCluster),
			},
			Spec: corev1.ServiceSpec{
				Type: ecsCluster.Spec.ExternalAccess.Type,
				Ports: []corev1.ServicePort{
					{
						Name:       "server",
						Port:       12345,
						Protocol:   "TCP",
						TargetPort: intstr.FromInt(12345),
					},
				},
				ExternalTrafficPolicy: corev1.ServiceExternalTrafficPolicyTypeLocal,
				Selector: map[string]string{
					appsv1.StatefulSetPodNameLabel: fmt.Sprintf("%s-%d", util.StatefulSetNameForNode(ecsCluster.Name), i),
				},
			},
		}
		services[i] = service
	}
	return services
}

func MakeNodePodDisruptionBudget(ecsCluster *api.ECSCluster) *policyv1beta1.PodDisruptionBudget {
	var maxUnavailable intstr.IntOrString

	if ecsCluster.Spec.ECS.NodeReplicas == int32(1) {
		maxUnavailable = intstr.FromInt(0)
	} else {
		maxUnavailable = intstr.FromInt(1)
	}

	return &policyv1beta1.PodDisruptionBudget{
		TypeMeta: metav1.TypeMeta{
			Kind:       "PodDisruptionBudget",
			APIVersion: "policy/v1beta1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      util.PdbNameForNode(ecsCluster.Name),
			Namespace: ecsCluster.Namespace,
		},
		Spec: policyv1beta1.PodDisruptionBudgetSpec{
			MaxUnavailable: &maxUnavailable,
			Selector: &metav1.LabelSelector{
				MatchLabels: util.LabelsForNode(ecsCluster),
			},
		},
	}
}
