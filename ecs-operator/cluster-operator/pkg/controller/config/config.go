/**
 * Copyright (c) 2018 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package config

// TestMode enables test mode in the operator and applies
// the following changes:
// - Disables BookKeeper minimum number of replicas
// - Disables ECS Controller minimum number of replicas
// - Disables Segment Store minimum number of replicas
var TestMode bool
