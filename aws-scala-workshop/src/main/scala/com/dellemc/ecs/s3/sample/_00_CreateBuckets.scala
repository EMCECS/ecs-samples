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

object _00_CreateBuckets extends BucketAndObjectValidator {

    /**
     * Run the class.
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        createBucket(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET)
        createBucket(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2)
    }

    /**
     * Verify that the bucket does not exist, create it, and then verify that it does exist.
     * 
     * @param s3Client the client to use
     * @param bucketName the name to use
     */
    def createBucket(s3Client: AmazonS3, bucketName: String) = {
        try {
            checkBucketExistence(s3Client, bucketName)
            // create the bucket - used for subsequent demo operations
            s3Client.createBucket(bucketName)

            checkBucketExistence(s3Client, bucketName)
        } catch { case e: Exception => outputException(e) }
        println()
    }

}