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
	"strings"
	"utils"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3"
)

// KEYLIST includes keys to demo list objects
var KEYLIST = []string{
	"20151102/account-12345/bill.xml",
	"20151102/account-12345/bill.pdf",
	"20151102/account-12345/bill.html",
	"20151102/account-55555/bill.xml",
	"20151102/account-55555/bill.pdf",
	"20151102/account-55555/bill.html",
	"20151102/account-77777/bill.xml",
	"20151102/account-77777/bill.pdf",
	"20151102/account-77777/bill.html",
	"20151103/account-11111/bill.xml",
	"20151103/account-11111/bill.pdf",
	"20151103/account-11111/bill.html",
	"20151103/account-11122/bill.xml",
	"20151103/account-11122/bill.pdf",
	"20151103/account-11122/bill.html",
	"20151103/account-11133/bill.xml",
	"20151103/account-11133/bill.pdf",
	"20151103/account-11133/bill.html",
	"20141103/account-11111/bill.xml",
	"20141103/account-11111/bill.pdf",
	"20141103/account-11111/bill.html",
	"20141103/account-11122/bill.xml",
	"20141103/account-11122/bill.pdf",
	"20141103/account-11122/bill.html",
	"20141103/account-11133/bill.xml",
	"20141103/account-11133/bill.pdf",
	"20141103/account-11133/bill.html",
}

func main() {

	// Load config.yaml
	config := utils.LoadConfig()

	// Get S3 client to server
	s3client, err := utils.GetS3Client(config)
	utils.Check(err)

	// Get bucket name from config
	bucket := config.GetString("s3.demo_bucket_name")

	// Create Objects
	for _, key := range KEYLIST {
		_, err = s3client.PutObject(
			&s3.PutObjectInput{
				Bucket: aws.String(bucket),
				Key:    aws.String(key),
				Body:   strings.NewReader(key),
			})
		utils.Check(err)
		fmt.Printf("Creating sample object with key %s\n", key)
	}
	fmt.Println()

	reader := utils.NewInputReader()
	for {
		prefix := reader.GetInputStr("Enter the prefix (empty for none):")
		delimiter := reader.GetInputStr("Enter the delimiter (e.g. /, empty for none):")
		marker := reader.GetInputStr("Enter the marker (empty for none):")
		maxKeysStr := reader.GetInputStr("Enter the maxKeys (empty for none):")

		// List Object Input Params
		listObjectInput := &s3.ListObjectsInput{
			Bucket:    aws.String(bucket),
			Prefix:    aws.String(prefix),
			Delimiter: aws.String(delimiter),
			Marker:    aws.String(marker),
		}
		if len(prefix) > 0 {
			listObjectInput.SetPrefix(prefix)
		}
		if len(delimiter) > 0 {
			listObjectInput.SetDelimiter(delimiter)
		}
		if len(marker) > 0 {
			listObjectInput.SetMarker(marker)
		}
		if len(maxKeysStr) > 0 {
			maxKeys, err := strconv.ParseInt(maxKeysStr, 10, 64)
			utils.Check(err)
			listObjectInput.SetMaxKeys(maxKeys)
		}

		// List Objects
		resp, err := s3client.ListObjects(listObjectInput)
		utils.Check(err)

		fmt.Printf("-----------------\n")
		fmt.Printf("Bucket: %s\n", bucket)
		fmt.Printf("Prefix: %s\n", *resp.Prefix)
		fmt.Printf("Delimiter: %s\n", *resp.Delimiter)
		fmt.Printf("Marker: %s\n", *resp.Marker)
		fmt.Printf("IsTruncated? %t\n", *resp.IsTruncated)
		if resp.NextMarker != nil {
			fmt.Printf("NextMarker: %s\n", *resp.NextMarker)
		}
		fmt.Printf("\n")

		for _, cPrefix := range resp.CommonPrefixes {
			fmt.Printf("CommonPrefix: %s\n", *cPrefix.Prefix)
		}

		fmt.Printf("%30s %10s %s\n", "LastModified", "Size", "Key")
		fmt.Printf("------------------------------ ---------- ------------------------------------------\n")
		for _, obj := range resp.Contents {
			fmt.Printf("%30s %10d %s\n", *obj.LastModified, *obj.Size, *obj.Key)
		}

		fmt.Println("Another? (Y/N) ")
		answer, _ := reader.ReadString('\n')
		answer = answer[:len(answer)-1]
		if strings.ToUpper(answer) != "Y" {
			break
		}
	}

	// Cleanup
	fmt.Println()
	fmt.Printf("Do clean up before exit...")
	for _, key := range KEYLIST {
		s3client.DeleteObject(
			&s3.DeleteObjectInput{
				Bucket: aws.String(bucket),
				Key:    aws.String(key),
			})
	}
	fmt.Println("Done")
}
