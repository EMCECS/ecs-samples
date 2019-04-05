/**
 * Copyright (c) 2018 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package nautilus

import (
	"strings"

	"fmt"

	api "github.com/nautilus/nautilus-operator/pkg/apis/nautilus/v1alpha1"
	"github.com/nautilus/nautilus-operator/pkg/util"
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	policyv1beta1 "k8s.io/api/policy/v1beta1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
)

const (
	cacheVolumeName       = "cache"
	cacheVolumeMountPoint = "/tmp/nautilus/cache"
	tier2FileMountPoint   = "/mnt/tier2"
	tier2VolumeName       = "tier2"
	nodeKind      = "nautilus-node"
)

func MakeNodeStatefulSet(nautilusCluster *api.NautilusCluster) *appsv1.StatefulSet {
	return &appsv1.StatefulSet{
		TypeMeta: metav1.TypeMeta{
			Kind:       "StatefulSet",
			APIVersion: "apps/v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      util.StatefulSetNameForNode(nautilusCluster.Name),
			Namespace: nautilusCluster.Namespace,
		},
		Spec: appsv1.StatefulSetSpec{
			ServiceName:         "nautilus-node",
			Replicas:            &nautilusCluster.Spec.Nautilus.NodeReplicas,
			PodManagementPolicy: appsv1.OrderedReadyPodManagement,
			Template: corev1.PodTemplateSpec{
				ObjectMeta: metav1.ObjectMeta{
					Labels: util.LabelsForNode(nautilusCluster),
				},
				Spec: makeNodePodSpec(nautilusCluster),
			},
			Selector: &metav1.LabelSelector{
				MatchLabels: util.LabelsForNode(nautilusCluster),
			},
			VolumeClaimTemplates: makeCacheVolumeClaimTemplate(nautilusCluster.Spec.Nautilus),
		},
	}
}

func makeNodePodSpec(nautilusCluster *api.NautilusCluster) corev1.PodSpec {
	environment := []corev1.EnvFromSource{
		{
			ConfigMapRef: &corev1.ConfigMapEnvSource{
				LocalObjectReference: corev1.LocalObjectReference{
					Name: util.ConfigMapNameForNode(nautilusCluster.Name),
				},
			},
		},
	}

	nautilusSpec := nautilusCluster.Spec.Nautilus

	environment = configureTier2Secrets(environment, nautilusSpec)

	podSpec := corev1.PodSpec{
		Containers: []corev1.Container{
			{
				Name:            "nautilus-node",
				Image:           nautilusSpec.Image.String(),
				ImagePullPolicy: nautilusSpec.Image.PullPolicy,
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
				Resources: *nautilusSpec.NodeResources,
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
		Affinity: util.PodAntiAffinity("nautilus-node", nautilusCluster.Name),
	}

	if nautilusSpec.NodeServiceAccountName != "" {
		podSpec.ServiceAccountName = nautilusSpec.NodeServiceAccountName
	}

	configureTier2Filesystem(&podSpec, nautilusSpec)

	return podSpec
}

func MakeNodeConfigMap(p *api.NautilusCluster) *corev1.ConfigMap {
	javaOpts := []string{
		"-Xms1g",
		"-XX:+UnlockExperimentalVMOptions",
		"-XX:+UseCGroupMemoryLimitForHeap",
		"-XX:MaxRAMFraction=2",
		"-XX:+ExitOnOutOfMemoryError",
		"-XX:+CrashOnOutOfMemoryError",
		"-XX:+HeapDumpOnOutOfMemoryError",
		"-Dnautilusservice.clusterName=" + p.Name,
	}

	for name, value := range p.Spec.Nautilus.Options {
		javaOpts = append(javaOpts, fmt.Sprintf("-D%v=%v", name, value))
	}

	configData := map[string]string{
		"AUTHORIZATION_ENABLED": "false",
		"CLUSTER_NAME":          p.Name,
		"ZK_URL":                p.Spec.ZookeeperUri,
		"JAVA_OPTS":             strings.Join(javaOpts, " "),
		"CONTROLLER_URL":        util.NautilusControllerServiceURL(*p),
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

	if p.Spec.Nautilus.DebugLogging {
		configData["log.level"] = "DEBUG"
	}

	for k, v := range getTier2StorageOptions(p.Spec.Nautilus) {
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

func makeCacheVolumeClaimTemplate(nautilusSpec *api.NautilusSpec) []corev1.PersistentVolumeClaim {
	return []corev1.PersistentVolumeClaim{
		{
			ObjectMeta: metav1.ObjectMeta{
				Name: cacheVolumeName,
			},
			Spec: *nautilusSpec.CacheVolumeClaimTemplate,
		},
	}
}

func getTier2StorageOptions(nautilusSpec *api.NautilusSpec) map[string]string {
	if nautilusSpec.Tier2.FileSystem != nil {
		return map[string]string{
			"TIER2_STORAGE": "FILESYSTEM",
			"NFS_MOUNT":     tier2FileMountPoint,
		}
	}

	if nautilusSpec.Tier2.Ecs != nil {
		// EXTENDEDS3_ACCESS_KEY_ID & EXTENDEDS3_SECRET_KEY will come from secret storage
		return map[string]string{
			"TIER2_STORAGE":        "EXTENDEDS3",
			"EXTENDEDS3_BUCKET":    nautilusSpec.Tier2.Ecs.Bucket,
			"EXTENDEDS3_URI":       nautilusSpec.Tier2.Ecs.Uri,
			"EXTENDEDS3_ROOT":      nautilusSpec.Tier2.Ecs.Root,
			"EXTENDEDS3_NAMESPACE": nautilusSpec.Tier2.Ecs.Namespace,
		}
	}

	if nautilusSpec.Tier2.Hdfs != nil {
		return map[string]string{
			"TIER2_STORAGE": "HDFS",
			"HDFS_URL":      nautilusSpec.Tier2.Hdfs.Uri,
			"HDFS_ROOT":     nautilusSpec.Tier2.Hdfs.Root,
		}
	}

	return make(map[string]string)
}

func configureTier2Secrets(environment []corev1.EnvFromSource, nautilusSpec *api.NautilusSpec) []corev1.EnvFromSource {
	if nautilusSpec.Tier2.Ecs != nil {
		return append(environment, corev1.EnvFromSource{
			Prefix: "EXTENDEDS3_",
			SecretRef: &corev1.SecretEnvSource{
				LocalObjectReference: corev1.LocalObjectReference{
					Name: nautilusSpec.Tier2.Ecs.Credentials,
				},
			},
		})
	}

	return environment
}

func configureTier2Filesystem(podSpec *corev1.PodSpec, nautilusSpec *api.NautilusSpec) {

	if nautilusSpec.Tier2.FileSystem != nil {
		podSpec.Containers[0].VolumeMounts = append(podSpec.Containers[0].VolumeMounts, corev1.VolumeMount{
			Name:      tier2VolumeName,
			MountPath: tier2FileMountPoint,
		})

		podSpec.Volumes = append(podSpec.Volumes, corev1.Volume{
			Name: tier2VolumeName,
			VolumeSource: corev1.VolumeSource{
				PersistentVolumeClaim: nautilusSpec.Tier2.FileSystem.PersistentVolumeClaim,
			},
		})
	}
}

func MakeNodeHeadlessService(nautilusCluster *api.NautilusCluster) *corev1.Service {
	return &corev1.Service{
		TypeMeta: metav1.TypeMeta{
			Kind:       "Service",
			APIVersion: "v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      util.HeadlessServiceNameForNode(nautilusCluster.Name),
			Namespace: nautilusCluster.Namespace,
			Labels:    util.LabelsForNode(nautilusCluster),
		},
		Spec: corev1.ServiceSpec{
			Ports: []corev1.ServicePort{
				{
					Name:     "server",
					Port:     12345,
					Protocol: "TCP",
				},
			},
			Selector:  util.LabelsForNode(nautilusCluster),
			ClusterIP: corev1.ClusterIPNone,
		},
	}
}

func MakeNodeExternalServices(nautilusCluster *api.NautilusCluster) []*corev1.Service {
	var service *corev1.Service
	services := make([]*corev1.Service, nautilusCluster.Spec.Nautilus.NodeReplicas)

	for i := int32(0); i < nautilusCluster.Spec.Nautilus.NodeReplicas; i++ {
		service = &corev1.Service{
			TypeMeta: metav1.TypeMeta{
				Kind:       "Service",
				APIVersion: "v1",
			},
			ObjectMeta: metav1.ObjectMeta{
				Name:      util.ServiceNameForNode(nautilusCluster.Name, i),
				Namespace: nautilusCluster.Namespace,
				Labels:    util.LabelsForNode(nautilusCluster),
			},
			Spec: corev1.ServiceSpec{
				Type: nautilusCluster.Spec.ExternalAccess.Type,
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
					appsv1.StatefulSetPodNameLabel: fmt.Sprintf("%s-%d", util.StatefulSetNameForNode(nautilusCluster.Name), i),
				},
			},
		}
		services[i] = service
	}
	return services
}

func MakeNodePodDisruptionBudget(nautilusCluster *api.NautilusCluster) *policyv1beta1.PodDisruptionBudget {
	var maxUnavailable intstr.IntOrString

	if nautilusCluster.Spec.Nautilus.NodeReplicas == int32(1) {
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
			Name:      util.PdbNameForNode(nautilusCluster.Name),
			Namespace: nautilusCluster.Namespace,
		},
		Spec: policyv1beta1.PodDisruptionBudgetSpec{
			MaxUnavailable: &maxUnavailable,
			Selector: &metav1.LabelSelector{
				MatchLabels: util.LabelsForNode(nautilusCluster),
			},
		},
	}
}
