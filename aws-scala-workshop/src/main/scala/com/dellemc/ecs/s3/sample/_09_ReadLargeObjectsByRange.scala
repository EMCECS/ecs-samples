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

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.S3Object

object _09_ReadLargeObjectsByRange extends BucketAndObjectValidator {

    def main(args: Array[String]): Unit = {
        val start: Long = 1000
        val end: Long = 1300

        readLargeObjectRange(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, start, end)
        readLargeObjectRange(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2, AWSS3Factory.S3_OBJECT, start, end)
    }

    def readLargeObjectRange(s3Client: AmazonS3, bucketName: String, key: String, start: Long, end: Long) = {
        try {
            val getObjectRequest: GetObjectRequest = new GetObjectRequest(bucketName, key).withRange(start, end)
            val myObject: S3Object = s3Client.getObject(getObjectRequest)

            val reader: BufferedReader = new BufferedReader(new InputStreamReader(myObject.getObjectContent()))
            var line: String = reader.readLine()
            var separator: String = ""
            val content: StringBuilder = new StringBuilder()
            while (line != null) {
                content.append(separator).append(line)
                separator = "\n"
                line = reader.readLine()
            }

            System.out.println(String.format("object [%s/%s] content:\n%s", myObject.getBucketName(), myObject.getKey(), content))
        } catch { case e: Exception => outputException(e) }
        System.out.println()
    }

}