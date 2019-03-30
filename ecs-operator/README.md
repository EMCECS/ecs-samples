# Overview
This is a Kubernetes operator for DELLEMC ECS Object Storage
https://www.dellemc.com/en-us/storage/ecs/index.htm

This ECS operator manages ECS clusters deployed to Kubernetes and automates tasks related to an ECS cluster.

#Requirements
Kubernetes 1.8+
An existing Apache Zookeeper 3.5 cluster. This can be easily deployed using our Zookeeper operatorRequirements

# Description
A guide to writing a Kubernetes operator for DellEMC Elastic Cloud Storage

# Introduction: 

This document strives to curate the information needed in one place to get started with an operator for hosting ECS on Kubernetes. ECS is an on-premise Object Storage cloud provider and Kubernetes is a containerization technology most notable for isolating applications and making them portable with rich and extensible framework for all the resources and awareness that the application needs. This document is intended for a software developer audience.

# Description: 
A Kubernetes operator is a controller that takes its resource definitions for an application and reconciles the container configuration with the definitions. For example, an upgrade would be defined by the source and destination version numbers and associated locations to permit the controller to take the necessary actions. Definitions are for resources specific to the applications and they are called custom resource definitions. They are written in Yaml – a language for definitions and maintained in a folder called deploy under the root project folder. The other folder is for the logic in the controller and written in Go language.  The operator logic has code for apis and controller. As with all logic, there is also code for the entry point invocation. Most notably, there is a Dockerfile created by packaging the operator into a Docker image that deploys the operator and an associated account to run it.
Writing the operator is facilitated with the help of an operator software development kit which is available as a command line tool that generates the scaffolds necessary for the api and the controller. The custom resource definitions have to be edited by hand. The operators have a naming convention of <name>-operator. Definitions are broken out to their own yaml files and each definition in the file has a version, metadata and specification to help differentiate the changes made to the definitions.
The generated controller code allows structures, a primitive of the Go language for declarations, and functions for invocation. There is a Reconcile function that is primary to the controller. When invoked, the controller fetches the definition and matches what is available from the existing deployment. Each time the resource changes, Kubernetes invokes the reconcile corresponding to the operator for the resource definition.  The logic is therefore state driven. The state, its handlers and ownership of activities are elaborated as detailed as possible. A controllerUtil.SetControllerReference function is used to describe the primary ownership of a resource.
The operator sdk tool also helps build the docker image and push it to Docker Hub.

# Conclusion: 

Writing an operator for Kubernetes is made easy with the help of scaffoldings generated from the operator sdk tool for definitions and controller. The next step involves determining the custom definitions for the application.



