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

object _05_CreateObjectsWithMetadata extends BucketAndObjectValidator {

    /**
     * Run the class.
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        val metaKey: String = "myMetaKey"
        val metaValue: String = "myMetaValue"

        createObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, metaKey, metaValue)
    }

    /**
     * Create an object with user metadata and verify that the metadata is correct.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     * @param key the object to create
     * @param metaKey the custom metadata key to add
     * @param metaValue the metadata value for the custom key
     */
    def createObject(s3Client: AmazonS3, bucketName: String, key: String, metaKey: String, metaValue: String) = {
        try {
            checkObjectMetadata(s3Client, bucketName, key)

            // create the metadata
            val metadata: ObjectMetadata = new ObjectMetadata()
            metadata.addUserMetadata(metaKey, metaValue)
            metadata.setContentLength(0)
            // create the object with the metadata in the demo bucket
            s3Client.putObject(bucketName, key, new StringInputStream(""), metadata)

            checkObjectMetadata(s3Client, bucketName, key)
        } catch { case e: Exception => outputException(e) }
        println()
    }

}