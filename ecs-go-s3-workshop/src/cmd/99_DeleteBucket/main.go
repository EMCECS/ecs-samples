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

	var objIdentifierSlice []*s3.ObjectIdentifier

	// Check versioning status
	resp, err := s3client.GetBucketVersioning(&s3.GetBucketVersioningInput{Bucket: aws.String(bucket)})
	utils.Check(err)

	if resp.Status != nil && *resp.Status == s3.BucketVersioningStatusEnabled {
		// version enabled
		lovResp, err := s3client.ListObjectVersions(&s3.ListObjectVersionsInput{Bucket: aws.String(bucket)})
		utils.Check(err)
		for _, ver := range lovResp.Versions {
			objIdentifierSlice = append(objIdentifierSlice, &s3.ObjectIdentifier{Key: ver.Key, VersionId: ver.VersionId})

		}
	} else {
		// version disabled/suspended
		loResp, err := s3client.ListObjects(&s3.ListObjectsInput{Bucket: aws.String(bucket)})
		utils.Check(err)
		for _, obj := range loResp.Contents {
			objIdentifierSlice = append(objIdentifierSlice, &s3.ObjectIdentifier{Key: obj.Key})

		}
	}

	// Delete Objects/Versions
	_, err = s3client.DeleteObjects(
		&s3.DeleteObjectsInput{
			Bucket: aws.String(bucket),
			Delete: &s3.Delete{
				Objects: objIdentifierSlice,
			},
		})
	if err != nil {
		fmt.Println(err.Error())
	}

	// Delete Bucket
	_, err = s3client.DeleteBucket(&s3.DeleteBucketInput{Bucket: aws.String(bucket)})
	utils.Check(err)

	fmt.Printf("deleted bucket [%s]\n", bucket)
}
