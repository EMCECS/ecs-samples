package controller

import (
	"github.com/ecs/cluster-operator/pkg/controller/job"
)

func init() {
	// AddToManagerFuncs is a list of functions to create controllers and add them to a manager.
	AddToManagerFuncs = append(AddToManagerFuncs, job.Add)
}
