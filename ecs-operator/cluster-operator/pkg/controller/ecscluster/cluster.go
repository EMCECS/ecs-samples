package ecscluster

import (
	ecsv1 "github.com/ecs/cluster-operator/pkg/apis/ecs/v1"
	"github.com/ecs/cluster-operator/pkg/ecs"
)

// ECSCluster stores the current cluster's information. It binds the
// cluster and the deployment together, ensuring deployment interacts with the
// right cluster resource.
type ECSCluster struct {
	cluster *ecsv1.ECSCluster
	// deployment implements ecscluster.Deployment interface. This is
	// cached for a cluster to avoid recreating it without any change to the
	// cluster object. Every new cluster will create its unique deployment.
	deployment Deployment
}

// NewECSCluster creates a new ECSCluster object.
func NewECSCluster(cluster *ecsv1.ECSCluster) *ECSCluster {
	return &ECSCluster{cluster: cluster}
}

// SetDeployment creates a new ECS Deployment and sets it for the current
// ECSCluster.
func (c *ECSCluster) SetDeployment(r *ReconcileECSCluster) {
	// updateIfExists is set to false because we don't update any existing
	// resources for now. May change in future.
	// TODO: Change this after resolving the conflict between two way
	// binding and upgrade.
	updateIfExists := false
	c.deployment = ecs.NewDeployment(r.client, c.cluster, r.recorder, r.scheme, r.k8sVersion, updateIfExists)
}

// IsCurrentCluster compares the cluster attributes to check if the given
// cluster is the same as the current cluster.
func (c *ECSCluster) IsCurrentCluster(cluster *ecsv1.ECSCluster) bool {
	if (c.cluster.GetName() == cluster.GetName()) &&
		(c.cluster.GetNamespace() == cluster.GetNamespace()) {
		return true
	}
	return false
}

// Deploy deploys the ECS cluster.
func (c *ECSCluster) Deploy(r *ReconcileECSCluster) error {
	if c.deployment == nil {
		c.SetDeployment(r)
	}
	return c.deployment.Deploy()
}

// DeleteDeployment deletes the ECS Cluster deployment.
func (c *ECSCluster) DeleteDeployment() error {
	return c.deployment.Delete()
}
