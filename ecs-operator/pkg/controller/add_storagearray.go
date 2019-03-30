package controller

import (
	"github.com/operator-framework/operator-sdk/storagearray-operator/pkg/controller/storagearray"
)

func init() {
	// AddToManagerFuncs is a list of functions to create controllers and add them to a manager.
	AddToManagerFuncs = append(AddToManagerFuncs, storagearray.Add)
}
