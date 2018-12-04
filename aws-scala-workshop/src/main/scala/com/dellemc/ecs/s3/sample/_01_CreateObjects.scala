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

object _01_CreateObjects extends BucketAndObjectValidator {

    /**
     * Run the class.
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        val content: String = "initial object content"

        createObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, content)
    }

    /**
     * Verify that the object does not exist, create it, then verify that it does exist with the correct content.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     * @param key the object to create
     * @param content the content to insert
     */
    def createObject(s3Client: AmazonS3, bucketName: String, key: String, content: String) = {
        try {
            checkObjectExistence(s3Client, bucketName, key)

            val metadata: ObjectMetadata = new ObjectMetadata()
            metadata.setContentLength(content.length())
            s3Client.putObject(bucketName, key, new StringInputStream( content ), metadata)

            checkObjectContent(s3Client, bucketName, key)
        } catch { case e: Exception => outputException(e) }
        println()
    }

}