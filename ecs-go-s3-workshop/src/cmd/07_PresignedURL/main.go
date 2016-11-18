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
	"strconv"
	"time"
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

	// Read key and valid hours
	reader := utils.NewInputReader()
	key := reader.GetInputStr("Enter the object key:")
	hoursStr := reader.GetInputStr("How many hours should this tag be valid? ")
	hours, err := strconv.ParseInt(hoursStr, 10, 64)
	utils.Check(err)

	// Generate Get Request
	req, _ := s3client.GetObjectRequest(
		&s3.GetObjectInput{
			Bucket: aws.String(bucket),
			Key:    aws.String(key),
		})

	// Presign it with expire time
	url, err := req.Presign(time.Duration(hours) * time.Hour)
	utils.Check(err)

	// print object's pre-signed URL
	fmt.Printf("object [%s/%s] pre-signed URL: [%s]\n", bucket, key, url)
}
