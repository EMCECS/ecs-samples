# This can be used to install a specific version of OLM.
install_olm() {
    echo "Install OLM"

    git clone --depth 1 https://github.com/operator-framework/operator-lifecycle-manager ./test/olm

    # Create the following one at a time to avoid resource not found error.
    kubectl create -f ./test/olm/deploy/upstream/manifests/0.8.1/0000_50_olm_00-namespace.yaml
    kubectl create -f ./test/olm/deploy/upstream/manifests/0.8.1/0000_50_olm_01-olm-operator.serviceaccount.yaml
    for num in {02..05}; do kubectl create -f ./test/olm/deploy/upstream/manifests/0.8.1/0000_50_olm_$num*; done

    # TODO: Remove this once the default olm operator container is fixed, and
    # use the default manifest.
    # Error: flag provided but not defined: -writeStatusName
    kubectl create -f ./deploy/0000_50_olm_06-olm-operator.deployment.yaml
    for num in {07..12}; do kubectl create -f ./test/olm/deploy/upstream/manifests/0.8.1/0000_50_olm_$num*; done

    # Wait for OLM to be ready.
    sleep 10
    kubectl create -f ./test/olm/deploy/upstream/manifests/0.8.1/0000_50_olm_13-packageserver.subscription.yaml

    # Create this cluster role binding to grant permissions to the olm web console.
    kubectl create clusterrolebinding cluster-admin-binding --clusterrole=cluster-admin --user=system:serviceaccount:kube-system:default

    echo
}

olm_quick_install() {
    echo "Quick Install OLM"
    kubectl create -f https://raw.githubusercontent.com/operator-framework/operator-lifecycle-manager/master/deploy/upstream/quickstart/olm.yaml

    # Create this cluster role binding to grant permissions to the olm web console.
    kubectl create clusterrolebinding cluster-admin-binding --clusterrole=cluster-admin --user=system:serviceaccount:kube-system:default

    echo
}

install_nautilus_operator() {
    echo "Install Nautilus Operator via OLM"

    # Install nautilus catalog configmap.
    kubectl create -f deploy/nautilus-operators.configmap.yaml
    # Install nautilus catalog source.
    kubectl create -f deploy/nautilus-operators.catalogsource.yaml
    # Install nautilus operator by creating a subscription.
    kubectl create -f deploy/nautilus-operators.subscription.yaml

    # Wait for nautilus operator to be ready.
    until kubectl -n olm get deployment nautilus-operator --no-headers -o go-template='{{.status.readyReplicas}}' | grep -q 1; do sleep 3; done

    echo
}

install_nautilus() {
    echo "Install Nautilus"

    kubectl apply -f deploy/nautilus-operators.olm.cr.yaml
    sleep 5

    kubectl -n nautilus get all

    echo "Waiting for nautilus daemonset to be ready"
    until kubectl -n nautilus get daemonset nautilus-daemonset --no-headers -o go-template='{{.status.numberReady}}' | grep -q 1; do sleep 5; done
    echo "Daemonset ready!"

    echo "Waiting for nautilus statefulset to be ready"
    until kubectl -n nautilus get statefulset nautilus-statefulset --no-headers -o go-template='{{.status.readyReplicas}}' | grep -q 1; do sleep 5; done
    echo "Statefulset ready!"
}

uninstall_nautilus() {
    echo "Uninstalling Nautilus"
    kubectl delete -f deploy/nautilus-operators.olm.cr.yaml
    echo
}

uninstall_nautilus_operator() {
    echo "Uninstall Nautilus Operator"

    # Delete the current operator instance.
    kubectl -n olm delete csv nautilusoperator.0.0.0

    # Delete all the related resources.
    kubectl delete -f deploy/nautilus-operators.subscription.yaml
    kubectl delete -f deploy/nautilus-operators.catalogsource.yaml
    kubectl delete -f deploy/nautilus-operators.configmap.yaml

    echo
}

uninstall_olm_quick() {
    echo "Uninstalling OLM"
    kubectl delete -f https://raw.githubusercontent.com/operator-framework/operator-lifecycle-manager/master/deploy/upstream/quickstart/olm.yaml
    echo
}

uninstall_olm() {
    for num in {13..00}; do kubectl delete -f ./test/olm/deploy/upstream/manifests/0.8.1/0000_50_olm_$num*; done
}
