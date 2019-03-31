package apis

import (
	ecsv1 "github.com/ecs/cluster-operator/pkg/apis/ecs/v1"
)

func init() {
	// Register the types with the Scheme so the components can map objects to GroupVersionKinds and back
	AddToSchemes = append(AddToSchemes, ecsv1.SchemeBuilder.AddToScheme)
}
