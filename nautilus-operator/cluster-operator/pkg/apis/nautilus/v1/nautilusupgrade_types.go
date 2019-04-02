package v1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!
// NOTE: json tags are required.  Any new fields you add must have json tags for the fields to be serialized.

// NautilusUpgradeSpec defines the desired state of NautilusUpgrade
type NautilusUpgradeSpec struct {
	// INSERT ADDITIONAL SPEC FIELDS - desired state of cluster
	// Important: Run "operator-sdk generate k8s" to regenerate code after modifying this file

	NewImage string `json:"newImage"`
}

// NautilusUpgradeStatus defines the observed state of NautilusUpgrade
type NautilusUpgradeStatus struct {
	// INSERT ADDITIONAL STATUS FIELD - define observed state of cluster
	// Important: Run "operator-sdk generate k8s" to regenerate code after modifying this file
	Completed bool `json:"completed"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// NautilusUpgrade is the Schema for the nautilusupgrades API
// +k8s:openapi-gen=true
type NautilusUpgrade struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   NautilusUpgradeSpec   `json:"spec,omitempty"`
	Status NautilusUpgradeStatus `json:"status,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// NautilusUpgradeList contains a list of NautilusUpgrade
type NautilusUpgradeList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []NautilusUpgrade `json:"items"`
}

func init() {
	SchemeBuilder.Register(&NautilusUpgrade{}, &NautilusUpgradeList{})
}
