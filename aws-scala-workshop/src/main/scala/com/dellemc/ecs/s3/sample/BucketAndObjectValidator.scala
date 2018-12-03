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

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Map.Entry

import scala.collection.JavaConversions._

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.S3Object

class BucketAndObjectValidator {

    protected def checkBucketExistence(s3Client: AmazonS3, bucketName: String) = {
        try {
            def state = if (s3Client.doesBucketExistV2(bucketName)) "exists" else "does not exist"
            System.out.println(String.format("Bucket [%s] %s.", bucketName, state))
        } catch { case e: Exception => outputException(e) }
        System.out.println()
    }

    protected def checkObjectExistence(s3Client: AmazonS3, bucketName: String, key: String) = {
        try {
            val state: String = if (s3Client.doesObjectExist(bucketName, key)) "exists" else "does not exist"
            System.out.println(String.format("Object [%s/%s] %s", bucketName, key, state))
        } catch { case e: Exception => outputException(e) }
        System.out.println()
    }

    protected def checkObjectContent(s3Client: AmazonS3, bucketName: String, key: String) = {
        try {
            val s3Object: S3Object = s3Client.getObject(bucketName, key)
            val reader: BufferedReader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()))
            val returnedContent: String = reader.readLine()
            System.out.println(String.format("Object [%s/%s] exists with content: [%s]", s3Object.getBucketName(), s3Object.getKey(), returnedContent))
        } catch {
            case e: Exception =>
                System.out.println(String.format("Object [%s/%s] does not exist", bucketName, key))
        }
        System.out.println()
    }

    protected def checkObjectMetadata(s3Client: AmazonS3, bucketName: String, key: String) = {
        try {
            val metadata: ObjectMetadata = s3Client.getObjectMetadata(bucketName, key)
            System.out.println(String.format("Object [%s/%s] exists with system metadata:", bucketName, key))
            val rawEntries: scala.collection.mutable.Set[java.util.Map.Entry[String, Object]] = metadata.getRawMetadata().entrySet()
            rawEntries.forEach((i: Entry[String, Object]) => System.out.println("    " + i.getKey() + " = " + i.getValue()))
            System.out.println("and user metadata:")
            val userEntries: scala.collection.mutable.Set[java.util.Map.Entry[String, String]] = metadata.getUserMetadata().entrySet()
            userEntries.forEach((i: Entry[String, String]) => System.out.println("    " + i.getKey() + " = " + i.getValue()))
        } catch {
            case e: Exception =>
                System.out.println(String.format("Object [%s/%s] does not exist", bucketName, key))
        }
        System.out.println()
    }

    protected def outputException(e: Exception) = {
        System.out.println(e.getMessage())
        e.printStackTrace(System.out)
    }

}