package v1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!
// NOTE: json tags are required.  Any new fields you add must have json tags for the fields to be serialized.

// ECSUpgradeSpec defines the desired state of ECSUpgrade
type ECSUpgradeSpec struct {
	// INSERT ADDITIONAL SPEC FIELDS - desired state of cluster
	// Important: Run "operator-sdk generate k8s" to regenerate code after modifying this file

	NewImage string `json:"newImage"`
}

// ECSUpgradeStatus defines the observed state of ECSUpgrade
type ECSUpgradeStatus struct {
	// INSERT ADDITIONAL STATUS FIELD - define observed state of cluster
	// Important: Run "operator-sdk generate k8s" to regenerate code after modifying this file
	Completed bool `json:"completed"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// ECSUpgrade is the Schema for the ecsupgrades API
// +k8s:openapi-gen=true
type ECSUpgrade struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   ECSUpgradeSpec   `json:"spec,omitempty"`
	Status ECSUpgradeStatus `json:"status,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// ECSUpgradeList contains a list of ECSUpgrade
type ECSUpgradeList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []ECSUpgrade `json:"items"`
}

func init() {
	SchemeBuilder.Register(&ECSUpgrade{}, &ECSUpgradeList{})
}
