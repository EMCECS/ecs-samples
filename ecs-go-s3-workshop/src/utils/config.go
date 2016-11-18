package utils

/*
 * Copyright 2016 EMC Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import (
	"log"

	"github.com/jacobstr/confer"
)

// LoadConfig loads default config.yaml file
func LoadConfig() *confer.Config {
	config := confer.NewConfig()
	err := config.ReadPaths("config.yaml")
	if err != nil {
		log.Fatal(err)
	}
	return config
}
