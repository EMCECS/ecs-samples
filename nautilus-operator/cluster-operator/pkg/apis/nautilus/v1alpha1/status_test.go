/**
 * Copyright (c) 2018 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package v1alpha1_test

import (
	"testing"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"

	. "github.com/onsi/ginkgo"
	. "github.com/onsi/gomega"
	"github.com/nautilus/nautilus-operator/pkg/apis/nautilus/v1alpha1"
)

func TestV1alpha1(t *testing.T) {
	RegisterFailHandler(Fail)
	RunSpecs(t, "NautilusCluster Status")
}

var _ = Describe("NautilusCluster Status", func() {

	var p v1alpha1.NautilusCluster

	BeforeEach(func() {
		p = v1alpha1.NautilusCluster{
			ObjectMeta: metav1.ObjectMeta{
				Name: "default",
			},
		}
	})

	Context("manually set pods ready condition to be true", func() {
		BeforeEach(func() {
			condition := v1alpha1.ClusterCondition{
				Type:               v1alpha1.ClusterConditionPodsReady,
				Status:             corev1.ConditionTrue,
				Reason:             "",
				Message:            "",
				LastUpdateTime:     "",
				LastTransitionTime: "",
			}
			p.Status.Conditions = append(p.Status.Conditions, condition)
		})

		It("should contains pods ready condition and it is true status", func() {
			_, condition := p.Status.GetClusterCondition(v1alpha1.ClusterConditionPodsReady)
			Ω(condition.Status).To(Equal(corev1.ConditionTrue))
		})
	})

	Context("set conditions", func() {
		Context("set pods ready condition to be true", func() {
			BeforeEach(func() {
				p.Status.SetPodsReadyConditionFalse()
				p.Status.SetPodsReadyConditionTrue()
			})
			It("should have pods ready condition with true status", func() {
				_, condition := p.Status.GetClusterCondition(v1alpha1.ClusterConditionPodsReady)
				Ω(condition.Status).To(Equal(corev1.ConditionTrue))
			})
		})

		Context("set pod ready condition to be false", func() {
			BeforeEach(func() {
				p.Status.SetPodsReadyConditionTrue()
				p.Status.SetPodsReadyConditionFalse()
			})

			It("should have ready condition with false status", func() {
				_, condition := p.Status.GetClusterCondition(v1alpha1.ClusterConditionPodsReady)
				Ω(condition.Status).To(Equal(corev1.ConditionFalse))
			})

			It("should have updated timestamps", func() {
				_, condition := p.Status.GetClusterCondition(v1alpha1.ClusterConditionPodsReady)
				// TODO: check the timestamps
				Ω(condition.LastUpdateTime).NotTo(Equal(""))
				Ω(condition.LastTransitionTime).NotTo(Equal(""))
			})
		})
	})
})
