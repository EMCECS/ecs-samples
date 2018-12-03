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

object _04_DeleteObjects extends BucketAndObjectValidator {

    def main(args: Array[String]): Unit = {
        deleteObject(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT)
        deleteObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2, AWSS3Factory.S3_OBJECT)
    }

    def deleteObject(s3Client: AmazonS3, bucketName: String, key: String) = {
        try {
            checkObjectExistence(s3Client, bucketName, key)

            s3Client.deleteObject(bucketName, key)

            checkObjectContent(s3Client, bucketName, key)
        } catch { case e: Exception => outputException(e) }
        System.out.println()
    }

}