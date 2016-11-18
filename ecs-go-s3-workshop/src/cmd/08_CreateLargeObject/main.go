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
	"io"
	"os"
	"utils"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"
)

func main() {
	// theEasyWay()
	theHardWay()
}

func theHardWay() {
	// Load config.yaml
	config := utils.LoadConfig()

	// Get S3 client to server
	s3client, err := utils.GetS3Client(config)
	utils.Check(err)

	// Get bucket name from config
	bucket := config.GetString("s3.demo_bucket_name")

	// Read key and file path
	reader := utils.NewInputReader()
	key := reader.GetInputStr("Enter the object key:")
	path := reader.GetInputStr("Enter the file path:")

	file, err := os.Open(path)
	utils.Check(err)
	defer file.Close()

	// 1. Init MPU and Get UploadId
	initResp, err := s3client.CreateMultipartUpload(
		&s3.CreateMultipartUploadInput{
			Bucket: aws.String(bucket),
			Key:    aws.String(key),
		})
	utils.Check(err)
	uploadID := *initResp.UploadId

	const PartSize = 5 << 20 // 5MB
	var (
		parts      []*s3.CompletedPart
		partNumber int64
		curPos     int64
		readLen    int64
	)
	// Get File Size
	fStat, err := file.Stat()
	utils.Check(err)

	// 2. Upload Parts
	for remainingLength := fStat.Size(); remainingLength > 0; {
		partNumber++

		if remainingLength > PartSize {
			readLen = PartSize
		} else {
			readLen = remainingLength
		}
		remainingLength -= readLen

		fmt.Printf("Sending chunk [%d] starting at position [%d]\n", partNumber, curPos)
		uploadPartResp, err := s3client.UploadPart(
			&s3.UploadPartInput{
				Bucket:     aws.String(bucket),
				Key:        aws.String(key),
				UploadId:   aws.String(uploadID),
				PartNumber: aws.Int64(partNumber),
				Body:       io.NewSectionReader(file, curPos, readLen),
			})
		utils.Check(err)
		n := partNumber
		parts = append(parts, &s3.CompletedPart{
			ETag:       aws.String(*uploadPartResp.ETag),
			PartNumber: aws.Int64(n),
		})

		curPos += readLen
	}

	// 3. Complete MPU
	fmt.Println("Waiting for completion of multi-part upload")
	_, err = s3client.CompleteMultipartUpload(
		&s3.CompleteMultipartUploadInput{
			Bucket:          aws.String(bucket),
			Key:             aws.String(key),
			UploadId:        aws.String(uploadID),
			MultipartUpload: &s3.CompletedMultipartUpload{Parts: parts},
		})
	utils.Check(err)

	fmt.Printf("completed mulit-part upload for object [%s/%s] with file path: [%s]\n", bucket, key, path)
}

func theEasyWay() {
	// Load config.yaml
	config := utils.LoadConfig()

	// Get S3 client to server
	s3client, err := utils.GetS3Client(config)
	utils.Check(err)
	// Create an uploader with S3 client and default options
	uploader := s3manager.NewUploaderWithClient(s3client)

	// Get bucket name from config
	bucket := config.GetString("s3.demo_bucket_name")

	// Read key and file path
	reader := utils.NewInputReader()
	key := reader.GetInputStr("Enter the object key:")
	path := reader.GetInputStr("Enter the file path:")

	file, err := os.Open(path)
	utils.Check(err)
	defer file.Close()

	// Perform an upload.
	_, err = uploader.Upload(&s3manager.UploadInput{
		Bucket: aws.String(bucket),
		Key:    aws.String(key),
		Body:   file,
	})
	utils.Check(err)

	fmt.Printf("completed mulit-part upload for object [%s/%s] with file path: [%s]\n", bucket, key, path)
}
