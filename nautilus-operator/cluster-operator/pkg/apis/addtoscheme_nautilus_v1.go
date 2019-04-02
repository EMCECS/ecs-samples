package apis

import (
	nautilusv1 "github.com/nautilus/cluster-operator/pkg/apis/nautilus/v1"
)

func init() {
	// Register the types with the Scheme so the components can map objects to GroupVersionKinds and back
	AddToSchemes = append(AddToSchemes, nautilusv1.SchemeBuilder.AddToScheme)
}
