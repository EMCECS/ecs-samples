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
	"fmt"
	"strconv"
	"strings"

	"github.com/ecs/ecs-operator/pkg/apis/ecs/v1alpha1"
)

func PdbNameForBookie(clusterName string) string {
	return fmt.Sprintf("%s-bookie", clusterName)
}

func ConfigMapNameForBookie(clusterName string) string {
	return fmt.Sprintf("%s-bookie", clusterName)
}

func StatefulSetNameForBookie(clusterName string) string {
	return fmt.Sprintf("%s-bookie", clusterName)
}

func PdbNameForController(clusterName string) string {
	return fmt.Sprintf("%s-ecs-controller", clusterName)
}

func ConfigMapNameForController(clusterName string) string {
	return fmt.Sprintf("%s-ecs-controller", clusterName)
}

func ServiceNameForController(clusterName string) string {
	return fmt.Sprintf("%s-ecs-controller", clusterName)
}

func ServiceNameForNode(clusterName string, index int32) string {
	return fmt.Sprintf("%s-ecs-node-%d", clusterName, index)
}

func HeadlessServiceNameForNode(clusterName string) string {
	return fmt.Sprintf("%s-ecs-node-headless", clusterName)
}

func HeadlessServiceNameForBookie(clusterName string) string {
	return fmt.Sprintf("%s-bookie-headless", clusterName)
}

func DeploymentNameForController(clusterName string) string {
	return fmt.Sprintf("%s-ecs-controller", clusterName)
}

func PdbNameForNode(clusterName string) string {
	return fmt.Sprintf("%s-node", clusterName)
}

func ConfigMapNameForNode(clusterName string) string {
	return fmt.Sprintf("%s-ecs-node", clusterName)
}

func StatefulSetNameForNode(clusterName string) string {
	return fmt.Sprintf("%s-ecs-node", clusterName)
}

func LabelsForBookie(ecsCluster *v1alpha1.ECSCluster) map[string]string {
	labels := LabelsForECSCluster(ecsCluster)
	labels["component"] = "bookie"
	return labels
}

func LabelsForController(ecsCluster *v1alpha1.ECSCluster) map[string]string {
	labels := LabelsForECSCluster(ecsCluster)
	labels["component"] = "ecs-controller"
	return labels
}

func LabelsForNode(ecsCluster *v1alpha1.ECSCluster) map[string]string {
	labels := LabelsForECSCluster(ecsCluster)
	labels["component"] = "ecs-node"
	return labels
}

func LabelsForECSCluster(ecsCluster *v1alpha1.ECSCluster) map[string]string {
	return map[string]string{
		"app":             "ecs-cluster",
		"ecs_cluster": ecsCluster.Name,
	}
}

func PvcIsOrphan(stsPvcName string, replicas int32) bool {
	index := strings.LastIndexAny(stsPvcName, "-")
	if index == -1 {
		return false
	}

	ordinal, err := strconv.Atoi(stsPvcName[index+1:])
	if err != nil {
		return false
	}

	return int32(ordinal) >= replicas
}

func ECSControllerServiceURL(ecsCluster v1alpha1.ECSCluster) string {
	return fmt.Sprintf("tcp://%v.%v:%v", ServiceNameForController(ecsCluster.Name), ecsCluster.Namespace, "9090")
}

func HealthcheckCommand(port int32) []string {
	return []string{"/bin/sh", "-c", fmt.Sprintf("netstat -ltn 2> /dev/null | grep %d || ss -ltn 2> /dev/null | grep %d", port, port)}
}

// Min returns the smaller of x or y.
func Min(x, y int32) int32 {
	if x > y {
		return y
	}
	return x
}

func ContainsString(slice []string, str string) bool {
	for _, item := range slice {
		if item == str {
			return true
		}
	}
	return false
}

func RemoveString(slice []string, str string) (result []string) {
	for _, item := range slice {
		if item == str {
			continue
		}
		result = append(result, item)
	}
	return result
}

func GetClusterExpectedSize(p *v1alpha1.ECSCluster) (size int) {
	return int(p.Spec.ECS.ControllerReplicas + p.Spec.ECS.NodeReplicas + p.Spec.Bookkeeper.Replicas)
}
