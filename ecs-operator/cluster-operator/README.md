# ECS cluster-operator

[![Build Status](https://travis-ci.org/ecs/cluster-operator.svg?branch=master)](https://travis-ci.org/ecs/cluster-operator)
[![CircleCI](https://circleci.com/gh/ecs/cluster-operator.svg?style=svg)](https://circleci.com/gh/ecs/cluster-operator)

The ECS Cluster Operator deploys and configures a ECS cluster on
Kubernetes.

For quick installation of the cluster operator, use the [cluster operator helm
chart](https://github.com/ecs/charts/tree/master/stable/ecscluster-operator).

## Pre-requisites

* Kubernetes 1.9+
* Kubernetes must be configured to allow (configured by default in 1.10+):
  * Privileged mode containers (enabled by default)
  * Feature gate: MountPropagation=true.  This can be done by appending
    `--feature-gates MountPropagation=true` to the kube-apiserver and kubelet
    services.

Refer to the [ECS prerequisites docs](https://www.dellemc.com/en-us/collaterals/unauth/data-sheets/products/storage/h13117-emc-ecs-appliance-ss.pdf)
for more information.

## Setup/Development

1. Install [operator-sdk](https://github.com/operator-framework/operator-sdk/tree/master#quick-start).
2. Run `operator-sdk generate k8s` if there's a change in api type.
3. Build operator container with `operator-sdk build ecs/cluster-operator:<tag>`
4. Apply the manifests in `deploy/` to install the operator
   * Apply `namespace.yaml` to create the `ecs-operator` namespace.
   * Apply `service_account.yaml`, `role.yaml` and `role_binding.yaml` to create
    a service account and to grant all the permissions.
   * Apply `crds/*_crd.yaml` to define the custom resources.
   * Apply `operator.yaml` to install the operator. Change the container image
     in this file when installing a new operator.
   * Apply `crds/*_ecscluster_cr.yaml` to create a `ECSCluster`
     custom resource.

**NOTE**: Installing ECS on Minikube is not currently supported due to
missing [kernel prerequisites](https://www.dellemc.com/en-us/collaterals/unauth/data-sheets/products/storage/h13117-emc-ecs-appliance-ss.pdf).

For development, run the operator outside of the k8s cluster by running:

```bash
make local-run
```

Build operator container image:

```bash
make image/cluster-operator OPERATOR_IMAGE=ecs/cluster-operator:test
```

This builds all the components and copies the binaries into the same container.

After creating a resource, query the resource:

```bash
$ kubectl get ecscluster
NAME                READY     STATUS    AGE
example-ecs   3/3       Running   4m
```

## Inspect a ECSCluster Resource

Get all the details about the cluster:

```bash
$ kubectl describe ecscluster/example-ecs
Name:         example-ecs
Namespace:    default
Labels:       <none>
Annotations:  kubectl.kubernetes.io/last-applied-configuration={"apiVersion":"dellemc.com/v1","kind":"ECSCluster","metadata":{"annotations":{},"name":"example-ecs","namespace":"default"},"spec":{"...
API Version:  dellemc.com/v1
Kind:         ECSCluster
Metadata:
  Creation Timestamp:  2018-07-21T12:57:11Z
  Generation:          1
  Resource Version:    10939030
  Self Link:           /apis/dellemc.com/v1/namespaces/default/ecsclusters/example-ecs
  UID:                 955b24a4-8ce5-11e8-956a-1866da35eee2
Spec:
  Join:  test07
Status:
  Node Health Status:
  ...
  ...
  Nodes:
    test09
    test08
    test07
  Phase:  Running
  Ready:  3/3
Events:   <none>
```

## ECSCluster Resource Configuration

Once the ECS operator is running, a ECS cluster can be deployed by
creating a Cluster Configuration. The parameters specified in the configuration
will define how ECS is deployed, the rest of the installation details are
handled by the operator.

The following tables lists the configurable spec
parameters of the ECSCluster custom resource and their default values.

Parameter | Description | Default
--------- | ----------- | -------
`secretRefName` | Reference name of ecs secret |
`secretRefNamespace` | Namespace of ecs secret |
`namespace` | Namespace where ecs cluster resources are created | `ecs`
`images.nodeContainer` | ECS node container image | `ecs/node:1.1.0`
`images.initContainer` | ECS init container image | `ecs/init:0.1`
`images.csiNodeDriverRegistrarContainer` | CSI Node Driver Registrar Container image | `quay.io/k8scsi/csi-node-driver-registrar:v1.0.1`
`images.csiClusterDriverRegistrarContainer` | CSI Cluster Driver Registrar Container image | `quay.io/k8scsi/csi-cluster-driver-registrar:v1.0.1`
`images.csiExternalProvisionerContainer` | CSI External Provisioner Container image | `ecs/csi-provisioner:v1.0.1`
`images.csiExternalAttacherContainer` | CSI External Attacher Container image | `quay.io/k8scsi/csi-attacher:v1.0.1`
`csi.enable` | Enable CSI setup | `false`
`csi.enableProvisionCreds` | Enable CSI provision credentials | `false`
`csi.enableControllerPublishCreds` | Enable CSI controller publish credentials | `false`
`csi.enableNodePublishCreds` | Enable CSI node publish credentials | `false`
`service.name` | Name of the Service used by the cluster | `ecs`
`service.type` | Type of the Service used by the cluster | `ClusterIP`
`service.externalPort` | External port of the Service used by the cluster | `5705`
`service.internalPort` | Internal port of the Service used by the cluster | `5705`
`service.annotations` | Annotations of the Service used by the cluster |
`ingress.enable` | Enable ingress for the cluster | `false`
`ingress.hostname` | Hostname to be used in cluster ingress | `ecs.local`
`ingress.tls` | Enable TLS for the ingress | `false`
`ingress.annotations` | Annotations of the ingress used by the cluster |
`sharedDir` | Path to be shared with kubelet container when deployed as a pod | `/var/lib/kubelet/plugins/kubernetes.io~ecs`
`kvBackend.address` | Comma-separated list of addresses of external key-value store. (`1.2.3.4:2379,2.3.4.5:2379`) |
`kvBackend.backend` | Name of the key-value store to use. Set to `etcd` for external key-value store. | `embedded`
`pause` | Pause the operator for cluster maintenance | `false`
`debug` | Enable debug mode for all the cluster nodes | `false`
`disableFencing` | Disable Pod fencing | `false`
`disableTelemetry` | Disable telemetry reports | `false`
`nodeSelectorTerms` | Set node selector for ecs pod placement |
`tolerations` | Set pod tolerations for ecs pod placement |
`resources` | Set resource requirements for the containers |

## Upgrading a ECS Cluster

An existing ECS cluster can be upgraded to a new version of ECS by
creating an Upgrade Configuration. The cluster-operator takes care of
downloading the new container image and updating all the nodes with new version
of ECS.
An example of `ECSUpgrade` resource is [ecs_v1_ecsupgrade_cr.yaml](/deploy/crds/ecs_v1_ecsupgrade_cr.yaml).

Only offline upgrade is supported for now by cluster-operator. During the
upgrade, ECS maintenance mode is enabled, the applications that use
ECS volumes are scaled down and the whole ECS cluster is restarted
with a new version. Once the ECS cluster becomes usable, the applications
are scaled up to their previous configuration. Once the update is complete, make
sure to delete the upgrade resource to put the ECS cluster in normal mode.
This will disable the maintenance mode.

Once an upgrade resource is created, events related to the upgrade can be
viewed in the upgrade object description. All the status and errors, if any,
encountered during the upgrade are posted as events.

```bash
$ kubectl describe ecsupgrades example-ecsupgrade
Name:         example-ecsupgrade
Namespace:    default
Labels:       <none>
Annotations:  kubectl.kubernetes.io/last-applied-configuration={"apiVersion":"dellemc.com/v1","kind":"ECSUpgrade","metadata":{"annotations":{},"name":"example-ecsupgrade","namespace":"default"},...
API Version:  dellemc.com/v1
Kind:         ECSUpgrade
...
Spec:
  New Image:  ecs/node:1.0.0
Events:
  Type    Reason           Age   From                Message
  ----    ------           ----  ----                -------
  Normal  PullImage         4m    ecs-upgrader  Pulling the new container image
  Normal  PauseClusterCtrl  2m    ecs-upgrader  Pausing the cluster controller and enabling cluster maintenance mode
  Normal  UpgradeInit       2m    ecs-upgrader  ECS upgrade of cluster example-ecs started
  Normal  UpgradeComplete   0s    ecs-upgrader  ECS upgraded to ecs/node:1.0.0. Delete upgrade object to disable cluster maintenance mode
```

## ECSUpgrade Resource Configuration

The following table lists the configurable spec parameters of the
ECSUpgrade custom resource and their default values.

Parameter | Description | Default
--------- | ----------- | -------
`newImage` | ECS node container image to upgrade to |

## Cleanup Old Configurations

ECS creates and saves its files at `/var/lib/ecs` on the hosts. This
also contains some configurations of the cluster. To do a fresh install of
ECS, these files need to be deleted.

__WARNING__: This will delete any existing data and won't be recoverable.

__NOTE__: When using an external etcd, the data related to ecs should also
be removed.

```bash
ETCDCTL_API=3 /usr/local/bin/etcdctl --endpoints http://ecs-etcd-server:2379 del --prefix ecs
```

The cluster-operator provides a `Job`resource that can execute certain tasks on
all nodes or on selected nodes. This can be used to easily perform cleanup
task. An example would be to create a `Job` resource:

```yaml
apiVersion: dellemc.com/v1
kind: Job
metadata:
  name: cleanup-job
spec:
  image: ecs/cleanup:v0.0.2
  args: ["/var/lib/ecs"]
  mountPath: "/var/lib"
  hostPath: "/var/lib"
  completionWord: "done"
  nodeSelectorTerms:
    - matchExpressions:
      - key: node-role.kubernetes.io/worker
        operator: In
        values:
        - "true"
```

When applied, this job will run `ecs/cleanup` container on the nodes that
have label `node-role.kubernetes.io/worker` with value `"true"`, mounting
`/var/lib` and passing the argument `/var/lib/ecs`. This will run
`rm -rf /var/lib/ecs` in the selected nodes and cleanup all the ecs
files. To run it on all the nodes, remove the `nodeSelectorTerms` attribute.
On completion, the resource description shows that the task is completed and
can be deleted.

```bash
$ kubectl describe jobs.dellemc.com cleanup-job
Name:         cleanup-job
Namespace:    default
...
...
Spec:
  Completion Word:  
  Args:
    /var/lib/ecs
  Host Path:            /var/lib
  Image:                ecs/cleanup:v0.0.2
  ...
Status:
  Completed:  true
Events:
  Type    Reason        Age   From                       Message
  ----    ------        ----  ----                       -------
  Normal  JobCompleted  39s   ecscluster-operator  Job Completed. Safe to delete.
```

Deleting the resource, will terminate all the pods that were created to run the
task.

Internally, this `Job` is backed by a controller that creates pods using a
DaemonSet. Job containers have to be built in a specific way to achieve this
behavior.

In the above example, the cleanup container runs a shell script(`script.sh`):

```bash
#!/bin/ash

set -euo pipefail

# Gracefully handle the TERM signal sent when deleting the daemonset
trap 'exit' TERM

# This is the main command that's run by this script on
# all the nodes.
rm -rf $1

# Let the monitoring script know we're done.
echo "done"

# this is a workaround to prevent the container from exiting
# and k8s restarting the daemonset pod
while true; do sleep 1; done
```

And the container image is made with Dockerfile:

```dockerfile
FROM alpine:3.6
COPY script.sh .
RUN chmod u+x script.sh
ENTRYPOINT ["./script.sh"]
```

The script, after running the main command, enters into a sleep state, instead
of exiting. This is needed because we don't want the container to exit and start
again and again. Once completed, it echos "done". This is read by the Job
controller to figure out when the task is completed. Once all the pods have
completed the task, the Job status is completed and it can be deleted.

This can be extended to do other similar cluster management operations. This is
also used internally in the cluster upgrade process.

## Job (jobs.dellemc.com) Resource Configuration

The following table lists the configurable spec parameters of the
Job custom resource and their default values.

Parameter | Description | Default
--------- | ----------- | -------
`image` | Container image that the job runs |
`args` | Any arguments to be passed when the container is run |
`hostPath` | Path on the host that is mounted on the job container |
`mountPath` | Path on the job container where the hostPath is mounted |
`completionWord` | The word that job controller looks for in the pod logs to determine if the task is completed |
`labelSelector` | Labels that are added to the job pods and are used to select them. |
`nodeSelectorTerms` | This can be used to select the nodes where the job runs. |

## TLS Support

To enable TLS, ensure that an ingress controller is installed in the cluster.
Set `ingress.enable` and `ingress.tls` to `true`.
Store the TLS cert and key as part of the ecs secret as:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: "ecs-api"
...
...
data:
  # echo -n '<secret>' | base64
  ...
  ...
  # Add base64 encoded TLS cert and key.
  tls.crt:
  tls.key:
```

## CSI

ECS also supports the [Container Storage Interface (CSI)](https://github.com/container-storage-interface/spec)
to communicate with Kubernetes.

Only versions 1.10+ are supported. CSI ensures forward compatibility with
future releases of Kubernetes, as vendor-specific drivers will soon be
deprecated from Kubernetes. However, some functionality is not yet supported.

To enable CSI, set `csi.enable` to `true` in the `ECSCluster` resource
config.

```yaml
apiVersion: "dellemc.com/v1"
kind: "ECSCluster"
metadata:
  name: "example-ecs"
  namespace: "default"
spec:
  secretRefName: "ecs-api"
  secretRefNamespace: "default"
  csi:
    enable: true
```

### CSI Credentials

To enable CSI Credentials, ensure that CSI is enabled by setting `csi.enable` to
`true`. Based on the type of credentials to enable, set the csi fields to
`true`:

```yaml
apiVersion: "dellemc.com/v1"
kind: "ECSCluster"
metadata:
  name: "example-ecs"
  namespace: "default"
spec:
  ...
  ...
  csi:
    enable: true
    enableProvisionCreds: true
    enableControllerPublishCreds: true
    enableNodePublishCreds: true
  ...
```

Specify the CSI credentials as part of the ecs secret object as:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: "ecs-api"
...
...
data:
  # echo -n '<secret>' | base64
  ...
  ...
  csiProvisionUsername:
  csiProvisionPassword:
  csiControllerPublishUsername:
  csiControllerPublishPassword:
  csiNodePublishUsername:
  csiNodePublishPassword:
```
