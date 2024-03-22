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
	nautilus_e2eutil "github.com/nautilus/nautilus-operator/pkg/test/e2e/e2eutil"
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

	nautilus, err := nautilus_e2eutil.CreateCluster(t, f, ctx, nautilus_e2eutil.NewDefaultCluster(namespace))
	if err != nil {
		t.Fatal(err)
	}

	podSize := 5
	err = nautilus_e2eutil.WaitForClusterToBecomeReady(t, f, ctx, nautilus, podSize)
	if err != nil {
		t.Fatal(err)
	}

	// This is to get the latest Nautilus cluster object
	nautilus, err = nautilus_e2eutil.GetCluster(t, f, ctx, nautilus)
	if err != nil {
		t.Fatal(err)
	}

	nautilus.Spec.Bookkeeper.Replicas = 4
	nautilus.Spec.Nautilus.NodeReplicas = 2
	podSize = 7

	err = nautilus_e2eutil.UpdateCluster(t, f, ctx, nautilus)
	if err != nil {
		t.Fatal(err)
	}

	err = nautilus_e2eutil.WaitForClusterToBecomeReady(t, f, ctx, nautilus, podSize)
	if err != nil {
		t.Fatal(err)
	}

	// This is to get the latest Nautilus cluster object
	nautilus, err = nautilus_e2eutil.GetCluster(t, f, ctx, nautilus)
	if err != nil {
		t.Fatal(err)
	}

	// Scale down Nautilus cluster back to default
	nautilus.Spec.Bookkeeper.Replicas = 3
	nautilus.Spec.Nautilus.NodeReplicas = 1
	podSize = 5

	err = nautilus_e2eutil.UpdateCluster(t, f, ctx, nautilus)
	if err != nil {
		t.Fatal(err)
	}

	err = nautilus_e2eutil.WaitForClusterToBecomeReady(t, f, ctx, nautilus, podSize)
	if err != nil {
		t.Fatal(err)
	}

	err = nautilus_e2eutil.CheckPvcSanity(t, f, ctx, nautilus)
	if err != nil {
		t.Fatal(err)
	}

	// Delete cluster
	err = nautilus_e2eutil.DeleteCluster(t, f, ctx, nautilus)
	if err != nil {
		t.Fatal(err)
	}

	// No need to do cleanup since the cluster CR has already been deleted
	doCleanup = false

	err = nautilus_e2eutil.WaitForClusterToTerminate(t, f, ctx, nautilus)
	if err != nil {
		t.Fatal(err)
	}

	// A workaround for issue 93
	err = nautilus_e2eutil.RestartTier2(t, f, ctx, namespace)
	if err != nil {
		t.Fatal(err)
	}
}
