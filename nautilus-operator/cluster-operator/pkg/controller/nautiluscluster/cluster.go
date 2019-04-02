package nautiluscluster

import (
	nautilusv1 "github.com/nautilus/cluster-operator/pkg/apis/nautilus/v1"
	"github.com/nautilus/cluster-operator/pkg/nautilus"
)

// NautilusCluster stores the current cluster's information. It binds the
// cluster and the deployment together, ensuring deployment interacts with the
// right cluster resource.
type NautilusCluster struct {
	cluster *nautilusv1.NautilusCluster
	// deployment implements nautiluscluster.Deployment interface. This is
	// cached for a cluster to avoid recreating it without any change to the
	// cluster object. Every new cluster will create its unique deployment.
	deployment Deployment
}

// NewNautilusCluster creates a new NautilusCluster object.
func NewNautilusCluster(cluster *nautilusv1.NautilusCluster) *NautilusCluster {
	return &NautilusCluster{cluster: cluster}
}

// SetDeployment creates a new Nautilus Deployment and sets it for the current
// NautilusCluster.
func (c *NautilusCluster) SetDeployment(r *ReconcileNautilusCluster) {
	// updateIfExists is set to false because we don't update any existing
	// resources for now. May change in future.
	// TODO: Change this after resolving the conflict between two way
	// binding and upgrade.
	updateIfExists := false
	c.deployment = nautilus.NewDeployment(r.client, c.cluster, r.recorder, r.scheme, r.k8sVersion, updateIfExists)
}

// IsCurrentCluster compares the cluster attributes to check if the given
// cluster is the same as the current cluster.
func (c *NautilusCluster) IsCurrentCluster(cluster *nautilusv1.NautilusCluster) bool {
	if (c.cluster.GetName() == cluster.GetName()) &&
		(c.cluster.GetNamespace() == cluster.GetNamespace()) {
		return true
	}
	return false
}

// Deploy deploys the Nautilus cluster.
func (c *NautilusCluster) Deploy(r *ReconcileNautilusCluster) error {
	if c.deployment == nil {
		c.SetDeployment(r)
	}
	return c.deployment.Deploy()
}

// DeleteDeployment deletes the Nautilus Cluster deployment.
func (c *NautilusCluster) DeleteDeployment() error {
	return c.deployment.Delete()
}
