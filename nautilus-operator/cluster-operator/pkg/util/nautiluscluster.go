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

	"github.com/nautilus/nautilus-operator/pkg/apis/nautilus/v1alpha1"
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
	return fmt.Sprintf("%s-nautilus-controller", clusterName)
}

func ConfigMapNameForController(clusterName string) string {
	return fmt.Sprintf("%s-nautilus-controller", clusterName)
}

func ServiceNameForController(clusterName string) string {
	return fmt.Sprintf("%s-nautilus-controller", clusterName)
}

func ServiceNameForNode(clusterName string, index int32) string {
	return fmt.Sprintf("%s-nautilus-node-%d", clusterName, index)
}

func HeadlessServiceNameForNode(clusterName string) string {
	return fmt.Sprintf("%s-nautilus-node-headless", clusterName)
}

func HeadlessServiceNameForBookie(clusterName string) string {
	return fmt.Sprintf("%s-bookie-headless", clusterName)
}

func DeploymentNameForController(clusterName string) string {
	return fmt.Sprintf("%s-nautilus-controller", clusterName)
}

func PdbNameForNode(clusterName string) string {
	return fmt.Sprintf("%s-node", clusterName)
}

func ConfigMapNameForNode(clusterName string) string {
	return fmt.Sprintf("%s-nautilus-node", clusterName)
}

func StatefulSetNameForNode(clusterName string) string {
	return fmt.Sprintf("%s-nautilus-node", clusterName)
}

func LabelsForBookie(nautilusCluster *v1alpha1.NautilusCluster) map[string]string {
	labels := LabelsForNautilusCluster(nautilusCluster)
	labels["component"] = "bookie"
	return labels
}

func LabelsForController(nautilusCluster *v1alpha1.NautilusCluster) map[string]string {
	labels := LabelsForNautilusCluster(nautilusCluster)
	labels["component"] = "nautilus-controller"
	return labels
}

func LabelsForNode(nautilusCluster *v1alpha1.NautilusCluster) map[string]string {
	labels := LabelsForNautilusCluster(nautilusCluster)
	labels["component"] = "nautilus-node"
	return labels
}

func LabelsForNautilusCluster(nautilusCluster *v1alpha1.NautilusCluster) map[string]string {
	return map[string]string{
		"app":             "nautilus-cluster",
		"nautilus_cluster": nautilusCluster.Name,
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

func NautilusControllerServiceURL(nautilusCluster v1alpha1.NautilusCluster) string {
	return fmt.Sprintf("tcp://%v.%v:%v", ServiceNameForController(nautilusCluster.Name), nautilusCluster.Namespace, "9090")
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

func GetClusterExpectedSize(p *v1alpha1.NautilusCluster) (size int) {
	return int(p.Spec.Nautilus.ControllerReplicas + p.Spec.Nautilus.NodeReplicas + p.Spec.Bookkeeper.Replicas)
}
