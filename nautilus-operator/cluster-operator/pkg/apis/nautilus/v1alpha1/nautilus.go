/**
 * Copyright (c) 2018 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package v1alpha1

import (
	"fmt"

	"github.com/nautilus/nautilus-operator/pkg/controller/config"
	"k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/resource"
)

const (
	// DefaultNautilusImageRepository is the default Docker repository for
	// the Nautilus image
	DefaultNautilusImageRepository = "nautilus/nautilus"

	// DefaultNautilusImageTag is the default tag used for for the Nautilus
	// Docker image
	DefaultNautilusImageTag = "latest"

	// DefaultNautilusImagePullPolicy is the default image pull policy used
	// for the Nautilus Docker image
	DefaultNautilusImagePullPolicy = v1.PullAlways

	// DefaultNautilusCacheVolumeSize is the default volume size for the
	// Nautilus Node cache volume
	DefaultNautilusCacheVolumeSize = "20Gi"

	// DefaultNautilusTier2ClaimName is the default volume claim name used as Tier 2
	DefaultNautilusTier2ClaimName = "nautilus-tier2"

	// DefaultControllerReplicas is the default number of replicas for the Nautilus
	// Controller component
	DefaultControllerReplicas = 1

	// DefaultNodeReplicas is the default number of replicas for the Nautilus
	// Segment Store component
	DefaultNodeReplicas = 1

	// DefaultControllerRequestCPU is the default CPU request for Nautilus
	DefaultControllerRequestCPU = "250m"

	// DefaultControllerLimitCPU is the default CPU limit for Nautilus
	DefaultControllerLimitCPU = "500m"

	// DefaultControllerRequestMemory is the default memory request for Nautilus
	DefaultControllerRequestMemory = "512Mi"

	// DefaultControllerLimitMemory is the default memory limit for Nautilus
	DefaultControllerLimitMemory = "1Gi"

	// DefaultNodeRequestCPU is the default CPU request for Nautilus
	DefaultNodeRequestCPU = "500m"

	// DefaultNodeLimitCPU is the default CPU limit for Nautilus
	DefaultNodeLimitCPU = "1"

	// DefaultNodeRequestMemory is the default memory request for Nautilus
	DefaultNodeRequestMemory = "1Gi"

	// DefaultNodeLimitMemory is the default memory limit for Nautilus
	DefaultNodeLimitMemory = "2Gi"
)

// NautilusSpec defines the configuration of Nautilus
type NautilusSpec struct {
	// ControllerReplicas defines the number of Controller replicas.
	// Defaults to 1.
	ControllerReplicas int32 `json:"controllerReplicas"`

	// NodeReplicas defines the number of Segment Store replicas.
	// Defaults to 1.
	NodeReplicas int32 `json:"nodeReplicas"`

	// DebugLogging indicates whether or not debug level logging is enabled.
	// Defaults to false.
	DebugLogging bool `json:"debugLogging"`

	// Image defines the Nautilus Docker image to use.
	// By default, "nautilus/nautilus:latest" will be used.
	Image *NautilusImageSpec `json:"image"`

	// Options is the Nautilus configuration that is passed to the Nautilus processes
	// as JAVA_OPTS. See the following file for a complete list of options:
	// https://github.com/nautilus/nautilus/blob/master/config/config.properties
	Options map[string]string `json:"options"`

	// CacheVolumeClaimTemplate is the spec to describe PVC for the Nautilus cache.
	// This field is optional. If no PVC spec, stateful containers will use
	// emptyDir as volume
	CacheVolumeClaimTemplate *v1.PersistentVolumeClaimSpec `json:"cacheVolumeClaimTemplate"`

	// Tier2 is the configuration of Nautilus's tier 2 storage. If no configuration
	// is provided, it will assume that a PersistentVolumeClaim called "nautilus-tier2"
	// is present and it will use it as Tier 2
	Tier2 *Tier2Spec `json:"tier2"`

	// ControllerServiceAccountName configures the service account used on controller instances.
	// If not specified, Kubernetes will automatically assign the default service account in the namespace
	ControllerServiceAccountName string `json:"controllerServiceAccountName,omitempty"`

	// NodeServiceAccountName configures the service account used on segment store instances.
	// If not specified, Kubernetes will automatically assign the default service account in the namespace
	NodeServiceAccountName string `json:"nodeServiceAccountName,omitempty"`

	// ControllerResources specifies the request and limit of resources that controller can have.
	// ControllerResources includes CPU and memory resources
	ControllerResources *v1.ResourceRequirements `json:"controllerResources,omitempty"`

	// NodeResources specifies the request and limit of resources that node can have.
	// NodeResources includes CPU and memory resources
	NodeResources *v1.ResourceRequirements `json:"nodeResources,omitempty"`
}

func (s *NautilusSpec) withDefaults() (changed bool) {
	if !config.TestMode && s.ControllerReplicas < 1 {
		changed = true
		s.ControllerReplicas = 1
	}

	if !config.TestMode && s.NodeReplicas < 1 {
		changed = true
		s.NodeReplicas = 1
	}

	if s.Image == nil {
		changed = true
		s.Image = &NautilusImageSpec{}
	}
	if s.Image.withDefaults() {
		changed = true
	}

	if s.Options == nil {
		changed = true
		s.Options = map[string]string{}
	}

	if s.CacheVolumeClaimTemplate == nil {
		changed = true
		s.CacheVolumeClaimTemplate = &v1.PersistentVolumeClaimSpec{
			AccessModes: []v1.PersistentVolumeAccessMode{v1.ReadWriteOnce},
			Resources: v1.ResourceRequirements{
				Requests: v1.ResourceList{
					v1.ResourceStorage: resource.MustParse(DefaultNautilusCacheVolumeSize),
				},
			},
		}
	}

	if s.Tier2 == nil {
		changed = true
		s.Tier2 = &Tier2Spec{}
	}

	if s.Tier2.withDefaults() {
		changed = true
	}

	if s.ControllerResources == nil {
		changed = true
		s.ControllerResources = &v1.ResourceRequirements{
			Requests: v1.ResourceList{
				v1.ResourceCPU:    resource.MustParse(DefaultControllerRequestCPU),
				v1.ResourceMemory: resource.MustParse(DefaultControllerRequestMemory),
			},
			Limits: v1.ResourceList{
				v1.ResourceCPU:    resource.MustParse(DefaultControllerLimitCPU),
				v1.ResourceMemory: resource.MustParse(DefaultControllerLimitMemory),
			},
		}
	}

	if s.NodeResources == nil {
		changed = true
		s.NodeResources = &v1.ResourceRequirements{
			Requests: v1.ResourceList{
				v1.ResourceCPU:    resource.MustParse(DefaultNodeRequestCPU),
				v1.ResourceMemory: resource.MustParse(DefaultNodeRequestMemory),
			},
			Limits: v1.ResourceList{
				v1.ResourceCPU:    resource.MustParse(DefaultNodeLimitCPU),
				v1.ResourceMemory: resource.MustParse(DefaultNodeLimitMemory),
			},
		}
	}

	return changed
}

// NautilusImageSpec defines the fields needed for a Nautilus Docker image
type NautilusImageSpec struct {
	ImageSpec
}

// String formats a container image struct as a Docker compatible repository string
func (s *NautilusImageSpec) String() string {
	return fmt.Sprintf("%s:%s", s.Repository, s.Tag)
}

func (s *NautilusImageSpec) withDefaults() (changed bool) {
	if s.Repository == "" {
		changed = true
		s.Repository = DefaultNautilusImageRepository
	}

	if s.Tag == "" {
		changed = true
		s.Tag = DefaultNautilusImageTag
	}

	if s.PullPolicy == "" {
		changed = true
		s.PullPolicy = DefaultNautilusImagePullPolicy
	}

	return changed
}

// Tier2Spec configures the Tier 2 storage type to use with Nautilus.
// If not specified, Tier 2 will be configured in filesystem mode and will try
// to use a PersistentVolumeClaim with the name "nautilus-tier2"
type Tier2Spec struct {
	// FileSystem is used to configure a pre-created Persistent Volume Claim
	// as Tier 2 backend.
	// It is default Tier 2 mode.
	FileSystem *FileSystemSpec `json:"filesystem,omitempty"`

	// Ecs is used to configure a Dell EMC ECS system as a Tier 2 backend
	Ecs *ECSSpec `json:"ecs,omitempty"`

	// Hdfs is used to configure an HDFS system as a Tier 2 backend
	Hdfs *HDFSSpec `json:"hdfs,omitempty"`
}

func (s *Tier2Spec) withDefaults() (changed bool) {
	if s.FileSystem == nil && s.Ecs == nil && s.Hdfs == nil {
		changed = true
		fs := &FileSystemSpec{
			PersistentVolumeClaim: &v1.PersistentVolumeClaimVolumeSource{
				ClaimName: DefaultNautilusTier2ClaimName,
			},
		}
		s.FileSystem = fs
	}

	return changed
}

// FileSystemSpec contains the reference to a PVC.
type FileSystemSpec struct {
	PersistentVolumeClaim *v1.PersistentVolumeClaimVolumeSource `json:"persistentVolumeClaim"`
}

// ECSSpec contains the connection details to a Dell EMC ECS system
type ECSSpec struct {
	Uri         string `json:"uri"`
	Bucket      string `json:"bucket"`
	Root        string `json:"root"`
	Namespace   string `json:"namespace"`
	Credentials string `json:"credentials"`
}

// HDFSSpec contains the connection details to an HDFS system
type HDFSSpec struct {
	Uri               string `json:"uri"`
	Root              string `json:"root"`
	ReplicationFactor int32  `json:"replicationFactor"`
}
