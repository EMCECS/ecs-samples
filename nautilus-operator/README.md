# Overview
This is a Kubernetes operator for DELLEMC Nautilus Stream Storage
https://asd-nautilus-jenkins.isus.emc.com/view/Nautilus%20Kubernetes/job/nautilus-docs-master/Docs-Site/

A guide to writing a Kubernetes operator for DellEMC Nautilus storage now follows:
This Nautilus operator manages Nautilus storage deployed to Kubernetes and automates tasks related to Nautilus.

# Introduction: 
This document strives to curate the information needed in one place to write an operator for hosting Nautilus on Kubernetes. Nautilus is an a Stream Storage software defined stack and Kubernetes is a containerization technology. Kubernetes is well-known for isolating applications and making them portable with a rich and extensible framework. This framework allows declaring all the resources and awareness that the application needs. This document is intended for a software developer audience.

# Description: 
A Kubernetes operator is a controller that takes its resource definitions for an application and reconciles the container configuration with the definitions. For example, an upgrade would be defined by the source and destination version numbers and associated locations to permit the controller to take the necessary actions. Definitions are for resources specific to the applications and they are called custom resource definitions. They are written in Yaml – a language for definitions and maintained in a folder called deploy under the root project folder. The other folder is for the logic in the controller and written in Go language.  The operator logic has code for apis and controller. As with all logic, there is also code for the entry point invocation. Moreover, there is a Dockerfile created by packaging the operator into a Docker image that deploys the operator and an associated account to run it.
Writing the operator is facilitated with the help of an operator software development kit which is available as a command line tool that generates the scaffolds necessary for the api and the controller. The custom resource definitions have to be edited by hand. The operators have a naming convention of <name>-operator. Definitions are broken out to their own yaml files and each definition in the file has a version, metadata and specification to help differentiate the changes made to the definitions.
The generated controller code allows structures, a primitive of the Go language for declarations, and functions for invocation. There is a Reconcile function that is primary to the controller. When invoked, the controller fetches the definition and matches what is available from the existing deployment. Each time the resource changes, Kubernetes invokes the reconcile corresponding to the operator for the resource definition.  The logic is therefore state driven. The state, its handlers and ownership of activities are elaborated as detailed as possible. A controllerUtil.SetControllerReference function is used to describe the primary ownership of a resource.
The operator sdk tool also helps build the docker image and push it to Docker Hub.

# Conclusion: 

Writing an operator for Kubernetes is made easy with the help of scaffoldings generated from the operator sdk tool for definitions and controller. The next step involves determining the custom definitions for the application.
The application would specify a list of resources, apis and controllers based on the operations it wants to support:
The typical operations for the application deployment involve
1) upgrading
2) scaling
3) backups and other jobs
Each controller implements the reconcile function described earlier.   Each of the operations mentioned above may be part of a controller. Each controller is specific to a resource that it reconciles. A cluster controller, for example, will read the state of an Nautilus cluster object and make changes according to the state.  The reconcile function is executed periodically.  The Nautilus cluster instance is fetched.  If it is not found, then the associated resource is cleaned up.  By marking the cluster as reset, it is automatically garbage collected. If a cluster is found, it is treated as the current cluster. This helps prevent the cluster from being reconciled if it is not the current cluster in case there is a race condition. 
The reconcile function itself ensures that the default values are applied when the fields are not set in the spec. It deploys the current cluster using the deploy interface on the cluster However, it performs a few checks before it does this. For example, it checks to see that the operator is not paused, there is no resource finalization occurring, and that the resource versions match. After the deployment, the cluster is reconciled, and the cluster is reset. It is easy to make web requests from the operator so the actions itself can be performed remotely by targeting the management ip address.
All the controllers are registered with the application manager. This is specifically called out per controller.  The base controller merely calls all the controllers one by one and checks to see if the controller executed successfully or not.
The upgrade controller reconciles the upgrade object where the object defines the upgrade from and to images. Resume and reset are part of the upgrade reconciler. The upgrade itself might have other actions such as enabling, disabling, recording events, adding roles, service accounts and performing new image pull jobs.
There can be a generic job controller that assigns individual jobs to their encapsulations.  The reconcile function in this case can provide consistency across the jobs. Validations, health checks and logs are also maintained with the clusters. 
The APIs are merely a registry where the different resource types are registered.
The deploy folder contains the declarations in Yaml. 
The cmd folder contains the invocations specific to the workflow. It contains the manager cmd which registers the components and starts them.  It registers the namespaces, the schemes for the resources,  and all controllers.
Reference:
https://coreos.com/blog/introducing-operator-framework




