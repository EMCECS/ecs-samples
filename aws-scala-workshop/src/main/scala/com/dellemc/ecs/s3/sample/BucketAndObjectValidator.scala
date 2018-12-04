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
import com.amazonaws.services.s3.model.S3Object

/**
 * Base class for demo operations, provides various common checks on buckets and objects
 */
class BucketAndObjectValidator {

    /**
     * Checks for bucket existence.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket name to check
     */
    protected def checkBucketExistence(s3Client: AmazonS3, bucketName: String) = {
        try {
            def state = if (s3Client.doesBucketExistV2(bucketName)) "exists" else "does not exist"
            println(String.format("Bucket [%s] %s.", bucketName, state))
        } catch { case e: Exception => outputException(e) }
        println()
    }

    /**
     * Checks for object existence.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket name to check
     * @param key the object to check
     */
    protected def checkObjectExistence(s3Client: AmazonS3, bucketName: String, key: String) = {
        try {
            val state: String = if (s3Client.doesObjectExist(bucketName, key)) "exists" else "does not exist"
            println(String.format("Object [%s/%s] %s", bucketName, key, state))
        } catch { case e: Exception => outputException(e) }
        println()
    }

    /**
     * Checks for object content.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket name to check
     * @param key the object to check
     */
    protected def checkObjectContent(s3Client: AmazonS3, bucketName: String, key: String) = {
        try {
            val s3Object: S3Object = s3Client.getObject(bucketName, key)
            val returnedContent: String = scala.io.Source.fromInputStream(s3Object.getObjectContent(), "UTF-8").mkString
            println(String.format("Object [%s/%s] exists with content: [%s]", s3Object.getBucketName(), s3Object.getKey(), returnedContent))
        } catch {
            case e: Exception =>
                println(String.format("Object [%s/%s] does not exist", bucketName, key))
        }
        println()
    }

    /**
     * Checks for object metadata.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket name to check
     * @param key the object to check
     */
    protected def checkObjectMetadata(s3Client: AmazonS3, bucketName: String, key: String) = {
        try {
            val metadata: ObjectMetadata = s3Client.getObjectMetadata(bucketName, key)
            println(String.format("Object [%s/%s] exists with system metadata:", bucketName, key))
            println(metadata.getRawMetadata())
            println("and user metadata:")
            println(metadata.getUserMetadata())
        } catch {
            case e: Exception =>
                println(String.format("Object [%s/%s] does not exist", bucketName, key))
        }
        println()
    }

    /**
     * Standard exception output
     * 
     * @param e the exception
     */
    protected def outputException(e: Exception) = {
        println(e.getMessage())
        e.printStackTrace()
    }

}