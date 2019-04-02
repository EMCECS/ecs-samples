package nautilus

import (
	nautilusv1 "github.com/nautilus/cluster-operator/pkg/apis/nautilus/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/client-go/tools/record"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

// Deployment stores all the resource configuration and performs
// resource creation and update.
type Deployment struct {
	client     client.Client
	stos       *nautilusv1.NautilusCluster
	recorder   record.EventRecorder
	k8sVersion string
	scheme     *runtime.Scheme
	update     bool
}

// NewDeployment creates a new Deployment given a k8c client, nautilus manifest
// and an event broadcast recorder.
func NewDeployment(client client.Client, stos *nautilusv1.NautilusCluster, recorder record.EventRecorder, scheme *runtime.Scheme, version string, update bool) *Deployment {
	return &Deployment{
		client:     client,
		stos:       stos,
		recorder:   recorder,
		k8sVersion: version,
		scheme:     scheme,
		update:     update,
	}
}
