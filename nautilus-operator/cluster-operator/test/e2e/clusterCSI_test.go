// +build csi

package e2e

import (
	goctx "context"
	"strings"
	"testing"

	framework "github.com/operator-framework/operator-sdk/pkg/test"
	nautilus "github.com/nautilus/cluster-operator/pkg/apis/nautilus/v1"
	deploy "github.com/nautilus/cluster-operator/pkg/nautilus"
	testutil "github.com/nautilus/cluster-operator/test/e2e/util"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
)

func TestClusterCSI(t *testing.T) {
	ctx := framework.NewTestCtx(t)
	defer ctx.Cleanup()
	resourceNS := "nautilus"

	namespace, err := ctx.GetNamespace()
	if err != nil {
		t.Fatalf("could not get namespace: %v", err)
	}

	clusterSpec := nautilus.NautilusClusterSpec{
		SecretRefName:      "nautilus-api",
		SecretRefNamespace: "default",
		ResourceNS:         resourceNS,
		CSI: nautilus.NautilusClusterCSI{
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

	testNautilus := testutil.NewNautilusCluster(namespace, clusterSpec)

	testutil.SetupOperator(t, ctx)
	err = testutil.DeployCluster(t, ctx, testNautilus)
	if err != nil {
		t.Fatal(err)
	}

	f := framework.Global

	err = f.Client.Get(goctx.TODO(), types.NamespacedName{Name: "example-nautilus", Namespace: namespace}, testNautilus)
	if err != nil {
		t.Fatal(err)
	}

	testutil.ClusterStatusCheck(t, testNautilus.Status, 1)

	daemonset, err := f.KubeClient.AppsV1().DaemonSets(resourceNS).Get("nautilus-daemonset", metav1.GetOptions{IncludeUninitialized: true})
	if err != nil {
		t.Fatalf("failed to get nautilus-daemonset: %v", err)
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
