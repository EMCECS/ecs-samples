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

import java.util.Map.Entry

import scala.collection.JavaConversions.`deprecated asScalaSet`
import scala.collection.JavaConversions.`deprecated mutableSetAsJavaSet`

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata

object _06_ReadObjectsWithMetadata extends BucketAndObjectValidator {

    def main(args: Array[String]): Unit = {
        readObjectMetadata(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT)
        readObjectMetadata(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2, AWSS3Factory.S3_OBJECT)
    }

    def readObjectMetadata(s3Client: AmazonS3, bucketName: String, key: String) = {
        try {
            val metadata: ObjectMetadata = s3Client.getObjectMetadata(bucketName, key)

            System.out.println(String.format(
                "Object [%s/%s] has system metadata:",
                bucketName, key))
            val rawEntries: scala.collection.mutable.Set[java.util.Map.Entry[String, Object]] = metadata.getRawMetadata().entrySet()
            rawEntries.forEach((i: Entry[String, Object]) => System.out.println("    " + i.getKey() + " = " + i.getValue()))
            System.out.println("and user metadata:")
            val userEntries: scala.collection.mutable.Set[java.util.Map.Entry[String, String]] = metadata.getUserMetadata().entrySet()
            userEntries.forEach((i: Entry[String, String]) => System.out.println("    " + i.getKey() + " = " + i.getValue()))
        } catch { case e: Exception => outputException(e) }
        System.out.println()
    }

}