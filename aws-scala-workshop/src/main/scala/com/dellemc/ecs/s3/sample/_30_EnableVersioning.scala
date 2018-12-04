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

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.BucketVersioningConfiguration
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest

object _30_EnableVersioning extends BucketAndObjectValidator {

    /**
     * Run the class.
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        createVersionedBucket(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_VERSIONED_BUCKET)
        enableVersioningOnAnExistingBucket(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2)
    }

    /**
     * Check that the bucket does not exist, create it, then set the versioning state.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     */
    def createVersionedBucket(s3Client: AmazonS3, bucketName: String) = {
        try {
            checkBucketExistence(s3Client, bucketName)

            s3Client.createBucket(bucketName)

            enableVersioningOnAnExistingBucket(s3Client, bucketName)
        } catch { case e: Exception => outputException(e) }
        println()
    }

    /**
     * Change the versioning state on a bucket.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     */
    def enableVersioningOnAnExistingBucket(s3Client: AmazonS3, bucketName: String) = {
        try {
            checkVersioningStatus(s3Client, bucketName)

            val bucketVersioningConfiguration: BucketVersioningConfiguration = new BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED)
            val request: SetBucketVersioningConfigurationRequest = new SetBucketVersioningConfigurationRequest(bucketName, bucketVersioningConfiguration)
            s3Client.setBucketVersioningConfiguration(request)

            checkVersioningStatus(s3Client, bucketName)
        } catch { case e: Exception => outputException(e) }
        println()
    }

    /**
     * Read and output the versioning status.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to check
     */
    def checkVersioningStatus(s3Client: AmazonS3, bucketName: String) = {
        try {
            val bucketVersioningConfiguration: BucketVersioningConfiguration = s3Client.getBucketVersioningConfiguration(bucketName)
            System.out.println( s"Bucket [$bucketName] has versioning configuration ${bucketVersioningConfiguration.getStatus()}." )
        } catch { case e: Exception => outputException(e) }
        println()
    }

}