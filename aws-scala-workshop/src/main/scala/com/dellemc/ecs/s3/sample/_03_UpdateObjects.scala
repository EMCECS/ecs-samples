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

object _03_UpdateObjects extends BucketAndObjectValidator {

    def main(args: Array[String]): Unit = {
        val newContent: String = "new object content"

        updateObject(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, newContent)
        updateObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2, AWSS3Factory.S3_OBJECT, newContent)
    }

    def updateObject(s3Client: AmazonS3, bucketName: String, key: String, newContent: String) = {
        try {
            checkObjectContent(s3Client, bucketName, key)

            val metadata: ObjectMetadata = new ObjectMetadata()
            metadata.setContentLength(newContent.length())
            s3Client.putObject(bucketName, key, new StringInputStream( newContent ), metadata)

            checkObjectContent(s3Client, bucketName, key)
        } catch { case e: Exception => outputException(e) }
        System.out.println()
    }

}