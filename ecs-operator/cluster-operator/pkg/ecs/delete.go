package ecs

// Delete deletes all the ecs resources.
// This explicit delete is implemented instead of depending on the garbage
// collector because sometimes the garbage collector deletes the resources
// with owner reference as a CRD without the parent being deleted. This happens
// especially when a cluster reboots. Althrough the operator re-creates the
// resources, we want to avoid this behavior by implementing an explcit delete.
func (s *Deployment) Delete() error {

	if err := s.deleteStorageClass("fast"); err != nil {
		return err
	}

	if err := s.deleteService(s.stos.Spec.GetServiceName()); err != nil {
		return err
	}

	if err := s.deleteDaemonSet(daemonsetName); err != nil {
		return err
	}

	if err := s.deleteSecret(initSecretName); err != nil {
		return err
	}

	if err := s.deleteRoleBinding(KeyManagementBindingName); err != nil {
		return err
	}

	if err := s.deleteRole(KeyManagementRoleName); err != nil {
		return err
	}

	if err := s.deleteServiceAccount(DaemonsetSA); err != nil {
		return err
	}

	if s.stos.Spec.CSI.Enable {
		if err := s.deleteStatefulSet(statefulsetName); err != nil {
			return err
		}

		if err := s.deleteClusterRoleBinding(CSIAttacherClusterBindingName); err != nil {
			return err
		}

		if err := s.deleteClusterRoleBinding(CSIProvisionerClusterBindingName); err != nil {
			return err
		}

		if err := s.deleteClusterRole(CSIAttacherClusterRoleName); err != nil {
			return err
		}

		if err := s.deleteClusterRole(CSIProvisionerClusterRoleName); err != nil {
			return err
		}

		if err := s.deleteServiceAccount(StatefulsetSA); err != nil {
			return err
		}

		if err := s.deleteClusterRoleBinding(CSIK8SDriverRegistrarClusterBindingName); err != nil {
			return err
		}

		if err := s.deleteClusterRoleBinding(CSIDriverRegistrarClusterBindingName); err != nil {
			return err
		}

		if err := s.deleteClusterRole(CSIDriverRegistrarClusterRoleName); err != nil {
			return err
		}

		if err := s.deleteCSISecrets(); err != nil {
			return err
		}
	}

	// Delete role for Pod Fencing.
	if !s.stos.Spec.DisableFencing {
		if err := s.deleteClusterRoleBinding(FencingClusterBindingName); err != nil {
			return err
		}

		if err := s.deleteClusterRole(FencingClusterRoleName); err != nil {
			return err
		}
	}

	// NOTE: Do not delete the namespace. Namespace can have some resources
	// created by the control plane. They must not be deleted.

	return nil
}
