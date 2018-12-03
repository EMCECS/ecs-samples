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
import com.amazonaws.services.s3.model.S3Object

object _02_ReadObjects extends BucketAndObjectValidator {

    def main(args: Array[String]): Unit = {
        readObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT)
        readObject(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2, AWSS3Factory.S3_OBJECT)
    }

    def readObject(s3Client: AmazonS3, bucketName: String, key: String) = {
        try {
            // read the object from the demo bucket
            val myObject: S3Object = s3Client.getObject(bucketName, key)
            // convert object to a text string
            val reader: BufferedReader = new BufferedReader(new InputStreamReader(myObject.getObjectContent()))
            val content: String = reader.readLine()

            // print object key/value and content for validation
            System.out.println( String.format("object [%s/%s] content: [%s]", myObject.getBucketName(), myObject.getKey(), content))
        } catch { case e: Exception => outputException(e) }
        System.out.println()
    }

}