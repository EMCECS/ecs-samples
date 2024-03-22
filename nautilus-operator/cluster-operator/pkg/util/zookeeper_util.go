/**
 * Copyright (c) 2018 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package util

import (
	"container/list"
	"fmt"
	"time"

	"github.com/nautilus/nautilus-operator/pkg/apis/nautilus/v1alpha1"
	"github.com/samuel/go-zookeeper/zk"
)

const (
	// Set in https://github.com/nautilus/nautilus/blob/master/docker/bookkeeper/entrypoint.sh#L21
	NautilusPath = "nautilus"
	ZkFinalizer = "cleanUpZookeeper"
)

// Delete all znodes related to a specific Nautilus cluster
func DeleteAllZnodes(p *v1alpha1.NautilusCluster) (err error) {
	host := []string{p.Spec.ZookeeperUri}
	conn, _, err := zk.Connect(host, time.Second*5)
	if err != nil {
		return fmt.Errorf("failed to connect to zookeeper: %v", err)
	}
	defer conn.Close()

	root := fmt.Sprintf("/%s/%s", NautilusPath, p.Name)
	exist, _, err := conn.Exists(root)
	if err != nil {
		return fmt.Errorf("failed to check if zookeeper path exists: %v", err)
	}

	if exist {
		// Construct BFS tree to delete all znodes recursively
		tree, err := ListSubTreeBFS(conn, root)
		if err != nil {
			return fmt.Errorf("failed to construct BFS tree: %v", err)
		}

		for tree.Len() != 0 {
			err := conn.Delete(tree.Back().Value.(string), -1)
			if err != nil {
				return fmt.Errorf("failed to delete znode (%s): %v", tree.Back().Value.(string), err)
			}
			tree.Remove(tree.Back())
		}
	}
	return nil
}

// Construct a BFS tree
func ListSubTreeBFS(conn *zk.Conn, root string) (*list.List, error) {
	queue := list.New()
	tree := list.New()
	queue.PushBack(root)
	tree.PushBack(root)

	for {
		if queue.Len() == 0 {
			break
		}
		node := queue.Front()
		children, _, err := conn.Children(node.Value.(string))
		if err != nil {
			return tree, err
		}

		for _, child := range children {
			childPath := fmt.Sprintf("%s/%s", node.Value.(string), child)
			queue.PushBack(childPath)
			tree.PushBack(childPath)
		}
		queue.Remove(node)
	}
	return tree, nil
}
