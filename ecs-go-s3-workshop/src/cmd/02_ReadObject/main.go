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
	"bytes"
	"fmt"
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

	// Read key
	reader := utils.NewInputReader()
	key := reader.GetInputStr("Enter the object key:")

	// Get Object
	resp, err := s3client.GetObject(
		&s3.GetObjectInput{
			Bucket: aws.String(bucket),
			Key:    aws.String(key),
		})
	utils.Check(err)

	// Read body in response
	buf := new(bytes.Buffer)
	buf.ReadFrom(resp.Body)

	fmt.Printf("object [%s/%s] content: [%s]\n", bucket, key, buf.String())
}
