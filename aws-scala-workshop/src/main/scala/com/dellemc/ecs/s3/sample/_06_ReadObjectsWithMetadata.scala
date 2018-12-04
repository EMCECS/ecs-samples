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

object _06_ReadObjectsWithMetadata extends BucketAndObjectValidator {

    /**
     * Run the class.
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        readObjectMetadata(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT)
    }

    /**
     * Read and output the object metadata.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     * @param key the object to use
     */
    def readObjectMetadata(s3Client: AmazonS3, bucketName: String, key: String) = {
        try {
            val metadata: ObjectMetadata = s3Client.getObjectMetadata(bucketName, key)

            println(String.format(
                "Object [%s/%s] has system metadata:",
                bucketName, key))
            println(metadata.getRawMetadata())
            println("and user metadata:")
            println(metadata.getUserMetadata())
        } catch { case e: Exception => outputException(e) }
        println()
    }

}