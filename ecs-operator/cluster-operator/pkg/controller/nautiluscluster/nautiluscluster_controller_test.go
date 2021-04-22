/**
 * Copyright (c) 2018 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package ecscluster

import (
	"context"
	"testing"

	"k8s.io/apimachinery/pkg/api/resource"

	"github.com/ecs/ecs-operator/pkg/apis/ecs/v1alpha1"
	"github.com/ecs/ecs-operator/pkg/util"

	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/client-go/kubernetes/scheme"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/client/fake"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"

	. "github.com/onsi/ginkgo"
	. "github.com/onsi/gomega"
)

func TestBookie(t *testing.T) {
	RegisterFailHandler(Fail)
	RunSpecs(t, "ECS cluster")
}

var _ = Describe("ECSCluster Controller", func() {
	const (
		Name      = "example"
		Namespace = "default"
	)

	var (
		s = scheme.Scheme
		r *ReconcileECSCluster
	)

	Context("Reconcile", func() {
		var (
			req reconcile.Request
			p   *v1alpha1.ECSCluster
		)

		BeforeEach(func() {
			req = reconcile.Request{
				NamespacedName: types.NamespacedName{
					Name:      Name,
					Namespace: Namespace,
				},
			}
			p = &v1alpha1.ECSCluster{
				ObjectMeta: metav1.ObjectMeta{
					Name:      Name,
					Namespace: Namespace,
				},
			}
			s.AddKnownTypes(v1alpha1.SchemeGroupVersion, p)
		})

		Context("Default spec", func() {
			var (
				client client.Client
				err    error
			)

			BeforeEach(func() {
				p.WithDefaults()
				client = fake.NewFakeClient(p)
				r = &ReconcileECSCluster{client: client, scheme: s}
				_, err = r.Reconcile(req)
			})

			It("shouldn't error", func() {
				Ω(err).Should(BeNil())
			})

			Context("Default bookkeeper", func() {
				It("should have a default bookie resource", func() {
					foundBk := &appsv1.StatefulSet{}
					nn := types.NamespacedName{
						Name:      util.StatefulSetNameForBookie(p.Name),
						Namespace: Namespace,
					}
					err = client.Get(context.TODO(), nn, foundBk)
					Ω(err).Should(BeNil())
					Ω(foundBk.Spec.Template.Spec.Containers[0].Resources.Requests.Cpu().String()).Should(Equal("500m"))
					Ω(foundBk.Spec.Template.Spec.Containers[0].Resources.Requests.Memory().String()).Should(Equal("1Gi"))
					Ω(foundBk.Spec.Template.Spec.Containers[0].Resources.Limits.Cpu().String()).Should(Equal("1"))
					Ω(foundBk.Spec.Template.Spec.Containers[0].Resources.Limits.Memory().String()).Should(Equal("2Gi"))
				})
			})

			Context("Default ECS controller", func() {
				It("should have a default controller resource", func() {
					foundController := &appsv1.Deployment{}
					nn := types.NamespacedName{
						Name:      util.DeploymentNameForController(p.Name),
						Namespace: Namespace,
					}
					err = client.Get(context.TODO(), nn, foundController)
					Ω(err).Should(BeNil())
					Ω(foundController.Spec.Template.Spec.Containers[0].Resources.Requests.Cpu().String()).Should(Equal("250m"))
					Ω(foundController.Spec.Template.Spec.Containers[0].Resources.Requests.Memory().String()).Should(Equal("512Mi"))
					Ω(foundController.Spec.Template.Spec.Containers[0].Resources.Limits.Cpu().String()).Should(Equal("500m"))
					Ω(foundController.Spec.Template.Spec.Containers[0].Resources.Limits.Memory().String()).Should(Equal("1Gi"))
				})
			})

			Context("Default ECS node", func() {
				It("should have a default controller resource", func() {
					foundSS := &appsv1.StatefulSet{}
					nn := types.NamespacedName{
						Name:      util.StatefulSetNameForNode(p.Name),
						Namespace: Namespace,
					}
					err = client.Get(context.TODO(), nn, foundSS)
					Ω(err).Should(BeNil())
					Ω(foundSS.Spec.Template.Spec.Containers[0].Resources.Requests.Cpu().String()).Should(Equal("500m"))
					Ω(foundSS.Spec.Template.Spec.Containers[0].Resources.Requests.Memory().String()).Should(Equal("1Gi"))
					Ω(foundSS.Spec.Template.Spec.Containers[0].Resources.Limits.Cpu().String()).Should(Equal("1"))
					Ω(foundSS.Spec.Template.Spec.Containers[0].Resources.Limits.Memory().String()).Should(Equal("2Gi"))
				})
			})
		})

		Context("Custom spec", func() {
			var (
				client    client.Client
				err       error
				customReq *corev1.ResourceRequirements
			)

			BeforeEach(func() {
				customReq = &corev1.ResourceRequirements{
					Requests: corev1.ResourceList{
						corev1.ResourceCPU:    resource.MustParse("2"),
						corev1.ResourceMemory: resource.MustParse("4Gi"),
					},
					Limits: corev1.ResourceList{
						corev1.ResourceCPU:    resource.MustParse("4"),
						corev1.ResourceMemory: resource.MustParse("6Gi"),
					},
				}
				p.Spec = v1alpha1.ClusterSpec{
					Bookkeeper: &v1alpha1.BookkeeperSpec{
						Resources: customReq,
					},
					ECS: &v1alpha1.ECSSpec{
						ControllerResources:   customReq,
						NodeResources: customReq,
					},
				}
				p.WithDefaults()
				client = fake.NewFakeClient(p)
				r = &ReconcileECSCluster{client: client, scheme: s}
				_, err = r.Reconcile(req)
			})

			It("shouldn't error", func() {
				Ω(err).Should(BeNil())
			})

			Context("Custom bookkeeper", func() {
				It("should have a custom bookie resource", func() {
					foundBK := &appsv1.StatefulSet{}
					nn := types.NamespacedName{
						Name:      util.StatefulSetNameForBookie(p.Name),
						Namespace: Namespace,
					}
					err = client.Get(context.TODO(), nn, foundBK)
					Ω(err).Should(BeNil())
					Ω(foundBK.Spec.Template.Spec.Containers[0].Resources.Requests.Cpu().String()).Should(Equal("2"))
					Ω(foundBK.Spec.Template.Spec.Containers[0].Resources.Requests.Memory().String()).Should(Equal("4Gi"))
					Ω(foundBK.Spec.Template.Spec.Containers[0].Resources.Limits.Cpu().String()).Should(Equal("4"))
					Ω(foundBK.Spec.Template.Spec.Containers[0].Resources.Limits.Memory().String()).Should(Equal("6Gi"))
				})
			})

			Context("Custom ECS controller", func() {
				It("should have a custom controller resource", func() {
					foundController := &appsv1.Deployment{}
					nn := types.NamespacedName{
						Name:      util.DeploymentNameForController(p.Name),
						Namespace: Namespace,
					}
					err = client.Get(context.TODO(), nn, foundController)
					Ω(err).Should(BeNil())
					Ω(foundController.Spec.Template.Spec.Containers[0].Resources.Requests.Cpu().String()).Should(Equal("2"))
					Ω(foundController.Spec.Template.Spec.Containers[0].Resources.Requests.Memory().String()).Should(Equal("4Gi"))
					Ω(foundController.Spec.Template.Spec.Containers[0].Resources.Limits.Cpu().String()).Should(Equal("4"))
					Ω(foundController.Spec.Template.Spec.Containers[0].Resources.Limits.Memory().String()).Should(Equal("6Gi"))
				})
			})

			Context("Custom ECS node", func() {
				It("should have a custom node resource", func() {
					foundSS := &appsv1.StatefulSet{}
					nn := types.NamespacedName{
						Name:      util.StatefulSetNameForNode(p.Name),
						Namespace: Namespace,
					}
					err = client.Get(context.TODO(), nn, foundSS)
					Ω(err).Should(BeNil())
					Ω(foundSS.Spec.Template.Spec.Containers[0].Resources.Requests.Cpu().String()).Should(Equal("2"))
					Ω(foundSS.Spec.Template.Spec.Containers[0].Resources.Requests.Memory().String()).Should(Equal("4Gi"))
					Ω(foundSS.Spec.Template.Spec.Containers[0].Resources.Limits.Cpu().String()).Should(Equal("4"))
					Ω(foundSS.Spec.Template.Spec.Containers[0].Resources.Limits.Memory().String()).Should(Equal("6Gi"))
				})
			})
		})
	})
})
