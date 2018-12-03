/*
 * Copyright 2018 Dell Inc. or its subsidiaries. All Rights Reserved.
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
package com.dellemc.ecs.s3.sample

import scala.collection.JavaConversions.`deprecated asScalaBuffer`

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.BucketVersioningConfiguration
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.model.S3VersionSummary

object _99_DeleteBuckets extends BucketAndObjectValidator {

    def main(args: Array[String]): Unit = {
        emptyAndDeleteBucket(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET)
        emptyAndDeleteBucket(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2)
        emptyAndDeleteBucket(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_VERSIONED_BUCKET)
    }

    def emptyAndDeleteBucket(s3Client: AmazonS3, bucketName: String) = {
        try {
            checkBucketExistence(s3Client, bucketName)

            // delete all bucket content
            if (BucketVersioningConfiguration.OFF.equals(s3Client.getBucketVersioningConfiguration(bucketName).getStatus())) {
                // no versioning, so delete all objects
                val summaries: scala.collection.mutable.Iterable[S3ObjectSummary] = s3Client.listObjects(bucketName).getObjectSummaries()
                summaries.foreach((i:S3ObjectSummary) => {
                    System.out.println(String.format("Deleting object [%s/%s]", bucketName, i.getKey()))
                    s3Client.deleteObject(bucketName, i.getKey())
                })
            } else {
                // versioning was enabled, so delete all versions
                val summaries: scala.collection.mutable.Iterable[S3VersionSummary] = s3Client.listVersions(bucketName, null).getVersionSummaries()
                summaries.foreach((i:S3VersionSummary) => {
                    System.out.println(String.format("Deleting version [%s/%s/%s]", bucketName, i.getKey(), i.getVersionId()))
                    s3Client.deleteVersion(bucketName, i.getKey(), i.getVersionId())
                })
            }
    
            // delete the bucket
            s3Client.deleteBucket(bucketName)

            checkBucketExistence(s3Client, bucketName)
        } catch { case e: Exception => outputException(e) }
        System.out.println()
    }

}