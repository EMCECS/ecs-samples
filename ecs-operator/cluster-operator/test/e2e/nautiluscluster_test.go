/**
 * Copyright (c) 2018 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package e2e

import (
	"testing"

	framework "github.com/operator-framework/operator-sdk/pkg/test"
	"github.com/operator-framework/operator-sdk/pkg/test/e2eutil"
	apis "github.com/ecs/ecs-operator/pkg/apis"
	operator "github.com/ecs/ecs-operator/pkg/apis/ecs/v1alpha1"
	ecs_e2eutil "github.com/ecs/ecs-operator/pkg/test/e2e/e2eutil"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func TestECSCluster(t *testing.T) {
	ecsClusterList := &operator.ECSClusterList{
		TypeMeta: metav1.TypeMeta{
			Kind:       "ECSCluster",
			APIVersion: "ecs.ecs.io/v1alpha1",
		},
	}
	err := framework.AddToFrameworkScheme(apis.AddToScheme, ecsClusterList)
	if err != nil {
		t.Fatalf("failed to add custom resource scheme to framework: %v", err)
	}
	// run subtests
	t.Run("x", testECSCluster)
}

func testECSCluster(t *testing.T) {
	ctx := framework.NewTestCtx(t)
	defer ctx.Cleanup()
	err := ctx.InitializeClusterResources(&framework.CleanupOptions{TestContext: ctx, Timeout: ecs_e2eutil.CleanupTimeout, RetryInterval: ecs_e2eutil.CleanupRetryInterval})
	if err != nil {
		t.Fatalf("failed to initialize cluster resources: %v", err)
	}
	t.Log("Initialized cluster resources")
	namespace, err := ctx.GetNamespace()
	if err != nil {
		t.Fatal(err)
	}
	// get global framework variables
	f := framework.Global
	// wait for ecs-operator to be ready
	err = e2eutil.WaitForOperatorDeployment(t, f.KubeClient, namespace, "ecs-operator", 1, ecs_e2eutil.RetryInterval, ecs_e2eutil.Timeout)
	if err != nil {
		t.Fatal(err)
	}

	testFuncs := map[string]func(t *testing.T){
		"testCreateDefaultCluster":   testCreateDefaultCluster,
		"testRecreateDefaultCluster": testRecreateDefaultCluster,
		"testScaleCluster":           testScaleCluster,
	}

	for name, f := range testFuncs {
		t.Run(name, f)
	}
}
