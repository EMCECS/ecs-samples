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
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.S3Object

object _09_ReadLargeObjectsByRange extends BucketAndObjectValidator {

    /**
     * Run the class.
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        val start: Long = 1000
        val end: Long = 1300

        readLargeObjectRange(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, start, end)
    }

    /**
     * Read and output part of the content from a large object.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     * @param key the object to read
     * @param start the start byte
     * @param end the end byte
     */
    def readLargeObjectRange(s3Client: AmazonS3, bucketName: String, key: String, start: Long, end: Long) = {
        try {
            val getObjectRequest: GetObjectRequest = new GetObjectRequest(bucketName, key).withRange(start, end)
            val myObject: S3Object = s3Client.getObject(getObjectRequest)
            val content: String = scala.io.Source.fromInputStream(myObject.getObjectContent(), "UTF-8").mkString

            println( s"object [${myObject.getBucketName()}/${myObject.getKey()}] content:\n$content" )
        } catch { case e: Exception => outputException(e) }
        println()
    }

}