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

install_ecs_operator() {
    echo "Install ECS Operator via OLM"

    # Install ecs catalog configmap.
    kubectl create -f deploy/ecs-operators.configmap.yaml
    # Install ecs catalog source.
    kubectl create -f deploy/ecs-operators.catalogsource.yaml
    # Install ecs operator by creating a subscription.
    kubectl create -f deploy/ecs-operators.subscription.yaml

    # Wait for ecs operator to be ready.
    until kubectl -n olm get deployment ecs-operator --no-headers -o go-template='{{.status.readyReplicas}}' | grep -q 1; do sleep 3; done

    echo
}

install_ecs() {
    echo "Install ECS"

    kubectl apply -f deploy/ecs-operators.olm.cr.yaml
    sleep 5

    kubectl -n ecs get all

    echo "Waiting for ecs daemonset to be ready"
    until kubectl -n ecs get daemonset ecs-daemonset --no-headers -o go-template='{{.status.numberReady}}' | grep -q 1; do sleep 5; done
    echo "Daemonset ready!"

    echo "Waiting for ecs statefulset to be ready"
    until kubectl -n ecs get statefulset ecs-statefulset --no-headers -o go-template='{{.status.readyReplicas}}' | grep -q 1; do sleep 5; done
    echo "Statefulset ready!"
}

uninstall_ecs() {
    echo "Uninstalling ECS"
    kubectl delete -f deploy/ecs-operators.olm.cr.yaml
    echo
}

uninstall_ecs_operator() {
    echo "Uninstall ECS Operator"

    # Delete the current operator instance.
    kubectl -n olm delete csv ecsoperator.0.0.0

    # Delete all the related resources.
    kubectl delete -f deploy/ecs-operators.subscription.yaml
    kubectl delete -f deploy/ecs-operators.catalogsource.yaml
    kubectl delete -f deploy/ecs-operators.configmap.yaml

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
