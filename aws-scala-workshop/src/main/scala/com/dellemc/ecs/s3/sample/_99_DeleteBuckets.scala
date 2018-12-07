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

import scala.collection.JavaConversions._

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.BucketVersioningConfiguration
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.model.S3VersionSummary

object _99_DeleteBuckets extends BucketAndObjectValidator {

    /**
     * Delete all demo buckets
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        emptyAndDeleteBucket(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET)
        emptyAndDeleteBucket(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2)
        emptyAndDeleteBucket(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_VERSIONED_BUCKET)
    }

    /**
     * Check that the bucket exists, delete all objects or versopns in the bucket, delete the bucket, then check again for existence.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to delete
     */
    def emptyAndDeleteBucket(s3Client: AmazonS3, bucketName: String) = {
        try {
            checkBucketExistence(s3Client, bucketName)

            // delete all bucket content
            if (BucketVersioningConfiguration.OFF.equals(s3Client.getBucketVersioningConfiguration(bucketName).getStatus())) {
                // no versioning, so delete all objects
                s3Client.listObjects(bucketName).getObjectSummaries().foreach((summary:S3ObjectSummary) => {
                    println( s"Deleting object [${bucketName}/${summary.getKey()}]" )
                    s3Client.deleteObject(bucketName, summary.getKey())
                })
            } else {
                // versioning was enabled, so delete all versions
                s3Client.listVersions(bucketName, null).getVersionSummaries().foreach((summary:S3VersionSummary) => {
                    println( s"Deleting version ${bucketName}/${summary.getKey()}/${summary.getVersionId()}]" )
                    s3Client.deleteVersion(bucketName, summary.getKey(), summary.getVersionId())
                })
            }
    
            // delete the bucket
            s3Client.deleteBucket(bucketName)

            checkBucketExistence(s3Client, bucketName)
        } catch { case e: Exception => outputException(e) }
        println()
    }

}