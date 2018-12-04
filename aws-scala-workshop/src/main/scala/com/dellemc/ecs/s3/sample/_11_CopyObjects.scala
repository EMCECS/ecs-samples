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

object _11_CopyObjects extends BucketAndObjectValidator {

    /**
     * Run the class.
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        val newKey: String = "new~key"

        // Copy an object to a different bucket
        copyObject( AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2, newKey, AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT )

        // Copy an object to the same bucket
        copyObject( AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, newKey, AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT )
    }

    /**
     * Verify that the new object does not exist, create it by copying, then verify that it does exist and the old object has not changed.
     * 
     * @param s3Client the client to use
     * @param newBucketName the bucket to copy to
     * @param newKey the new object to create
     * @param bucketName the bucket to copy from
     * @param key the object to copy
     */
    def copyObject(s3Client: AmazonS3, newBucketName: String, newKey: String, bucketName: String, key: String) = {
        try {
            checkObjectMetadata(s3Client, newBucketName, newKey)

            s3Client.copyObject(bucketName, key, newBucketName, newKey)

            checkObjectMetadata(s3Client, bucketName, key)
            checkObjectMetadata(s3Client, newBucketName, newKey)
        } catch { case e: Exception => outputException(e) }
        println()
    }

}