// +build csi

package e2e

import (
	goctx "context"
	"strings"
	"testing"

	framework "github.com/operator-framework/operator-sdk/pkg/test"
	ecs "github.com/ecs/cluster-operator/pkg/apis/ecs/v1"
	deploy "github.com/ecs/cluster-operator/pkg/ecs"
	testutil "github.com/ecs/cluster-operator/test/e2e/util"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
)

func TestClusterCSI(t *testing.T) {
	ctx := framework.NewTestCtx(t)
	defer ctx.Cleanup()
	resourceNS := "ecs"

	namespace, err := ctx.GetNamespace()
	if err != nil {
		t.Fatalf("could not get namespace: %v", err)
	}

	clusterSpec := ecs.ECSClusterSpec{
		SecretRefName:      "ecs-api",
		SecretRefNamespace: "default",
		ResourceNS:         resourceNS,
		CSI: ecs.ECSClusterCSI{
			Enable: true,
		},
		Tolerations: []corev1.Toleration{
			{
				Key:      "key",
				Operator: corev1.TolerationOpEqual,
				Value:    "value",
				Effect:   corev1.TaintEffectNoSchedule,
			},
		},
	}

	testECS := testutil.NewECSCluster(namespace, clusterSpec)

	testutil.SetupOperator(t, ctx)
	err = testutil.DeployCluster(t, ctx, testECS)
	if err != nil {
		t.Fatal(err)
	}

	f := framework.Global

	err = f.Client.Get(goctx.TODO(), types.NamespacedName{Name: "example-ecs", Namespace: namespace}, testECS)
	if err != nil {
		t.Fatal(err)
	}

	testutil.ClusterStatusCheck(t, testECS.Status, 1)

	daemonset, err := f.KubeClient.AppsV1().DaemonSets(resourceNS).Get("ecs-daemonset", metav1.GetOptions{IncludeUninitialized: true})
	if err != nil {
		t.Fatalf("failed to get ecs-daemonset: %v", err)
	}

	info, err := f.KubeClient.Discovery().ServerVersion()
	if err != nil {
		t.Fatalf("failed to get version info: %v", err)
	}

	version := strings.TrimLeft(info.String(), "v")

	//Check the number of containers in daemonset pod spec.
	if deploy.CSIV1Supported(version) {
		if len(daemonset.Spec.Template.Spec.Containers) != 3 {
			t.Errorf("unexpected number of daemonset pod containers:\n\t(GOT) %d\n\t(WNT) %d", len(daemonset.Spec.Template.Spec.Containers), 2)
		}
	} else {
		if len(daemonset.Spec.Template.Spec.Containers) != 2 {
			t.Errorf("unexpected number of daemonset pod containers:\n\t(GOT) %d\n\t(WNT) %d", len(daemonset.Spec.Template.Spec.Containers), 2)
		}
	}
}
