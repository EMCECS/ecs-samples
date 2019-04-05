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
	ecs_e2eutil "github.com/ecs/ecs-operator/pkg/test/e2e/e2eutil"
)

func testScaleCluster(t *testing.T) {
	doCleanup := true
	ctx := framework.NewTestCtx(t)
	defer func() {
		if doCleanup {
			ctx.Cleanup()
		}
	}()

	namespace, err := ctx.GetNamespace()
	if err != nil {
		t.Fatal(err)
	}
	f := framework.Global

	ecs, err := ecs_e2eutil.CreateCluster(t, f, ctx, ecs_e2eutil.NewDefaultCluster(namespace))
	if err != nil {
		t.Fatal(err)
	}

	podSize := 5
	err = ecs_e2eutil.WaitForClusterToBecomeReady(t, f, ctx, ecs, podSize)
	if err != nil {
		t.Fatal(err)
	}

	// This is to get the latest ECS cluster object
	ecs, err = ecs_e2eutil.GetCluster(t, f, ctx, ecs)
	if err != nil {
		t.Fatal(err)
	}

	ecs.Spec.Bookkeeper.Replicas = 4
	ecs.Spec.ECS.NodeReplicas = 2
	podSize = 7

	err = ecs_e2eutil.UpdateCluster(t, f, ctx, ecs)
	if err != nil {
		t.Fatal(err)
	}

	err = ecs_e2eutil.WaitForClusterToBecomeReady(t, f, ctx, ecs, podSize)
	if err != nil {
		t.Fatal(err)
	}

	// This is to get the latest ECS cluster object
	ecs, err = ecs_e2eutil.GetCluster(t, f, ctx, ecs)
	if err != nil {
		t.Fatal(err)
	}

	// Scale down ECS cluster back to default
	ecs.Spec.Bookkeeper.Replicas = 3
	ecs.Spec.ECS.NodeReplicas = 1
	podSize = 5

	err = ecs_e2eutil.UpdateCluster(t, f, ctx, ecs)
	if err != nil {
		t.Fatal(err)
	}

	err = ecs_e2eutil.WaitForClusterToBecomeReady(t, f, ctx, ecs, podSize)
	if err != nil {
		t.Fatal(err)
	}

	err = ecs_e2eutil.CheckPvcSanity(t, f, ctx, ecs)
	if err != nil {
		t.Fatal(err)
	}

	// Delete cluster
	err = ecs_e2eutil.DeleteCluster(t, f, ctx, ecs)
	if err != nil {
		t.Fatal(err)
	}

	// No need to do cleanup since the cluster CR has already been deleted
	doCleanup = false

	err = ecs_e2eutil.WaitForClusterToTerminate(t, f, ctx, ecs)
	if err != nil {
		t.Fatal(err)
	}

	// A workaround for issue 93
	err = ecs_e2eutil.RestartTier2(t, f, ctx, namespace)
	if err != nil {
		t.Fatal(err)
	}
}
