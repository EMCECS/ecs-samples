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
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.util.StringInputStream

object _31_UpdateVersions extends BucketAndObjectValidator {

    /**
     * Run the class.
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        val newContent: String = "new object content"
        val otherNewContent: String = "other new object content"

        // update on a bucket that was never versioned
        updateObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, newContent, otherNewContent)
        // update on a bucket that is currently versioned, but originally was not versioned
        updateObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2, AWSS3Factory.S3_OBJECT, newContent, otherNewContent)
        // update on a bucket that was always versioned
        updateObject(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_VERSIONED_BUCKET, AWSS3Factory.S3_OBJECT, newContent, otherNewContent)
    }

    /**
     * Check whether the object exists, create it, then verify that the current version has the correct content.
     * Modify it again, and then check the content again.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     * @param key the object to update
     * @param newContent the first content to insert
     * @param otherNewContent the second content to insert
     */
    def updateObject(s3Client: AmazonS3, bucketName: String, key: String, newContent: String, otherNewContent: String) = {
        try {
            checkObjectExistence(s3Client, bucketName, key)

            val metadata: ObjectMetadata = new ObjectMetadata()
            metadata.setContentLength(newContent.length())
            s3Client.putObject(bucketName, key, new StringInputStream( newContent ), metadata)

            checkObjectContent(s3Client, bucketName, key)

            val otherMetadata: ObjectMetadata = new ObjectMetadata()
            otherMetadata.setContentLength(otherNewContent.length())
            s3Client.putObject(bucketName, key, new StringInputStream( otherNewContent ), otherMetadata)

            checkObjectContent(s3Client, bucketName, key)
        } catch { case e: Exception => outputException(e) }
        println()
    }

}