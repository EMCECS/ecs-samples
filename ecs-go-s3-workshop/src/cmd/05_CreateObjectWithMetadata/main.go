package main

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
	"fmt"
	"strings"
	"utils"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3"
)

func main() {
	// Load config.yaml
	config := utils.LoadConfig()

	// Get S3 client to server
	s3client, err := utils.GetS3Client(config)
	utils.Check(err)

	// Get bucket name from config
	bucket := config.GetString("s3.demo_bucket_name")

	// Read key and content
	reader := utils.NewInputReader()
	key := reader.GetInputStr("Enter the object key:")
	content := reader.GetInputStr("Enter the object content:")

	// Put Object Parameters
	params := &s3.PutObjectInput{
		Bucket: aws.String(bucket),
		Key:    aws.String(key),
		Body:   strings.NewReader(content),
	}

	// Read metadata key and value
	metaKey := reader.GetInputStr("Enter the metadata key:")
	metaValue := reader.GetInputStr("Enter the metadata content:")
	// Can set one or more Metadatas
	params.SetMetadata(map[string]*string{metaKey: aws.String(metaValue)})

	// Create Object
	_, err = s3client.PutObject(params)
	utils.Check(err)

	fmt.Printf("created object [%s/%s] with metadata [%s=%s] and content: [%s]\n",
		bucket, key, metaKey, metaValue, content)
}
