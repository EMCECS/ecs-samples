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

import java.io.File

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import com.amazonaws.services.s3.transfer.Upload

object _07_CreateLargeObjects extends BucketAndObjectValidator {

    /**
     * Run the class.
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        val filePath: String = "/Users/seibed/Downloads/formatter.xml"

        createLargeObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, filePath)
    }

    /**
     * Check the initial object metadata, upload a large amount of content from a file, then recheck the metadata.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     * @param key the object to modify
     * @param filePath the file to use
     */
    def createLargeObject(s3Client: AmazonS3, bucketName: String, key: String, filePath: String) = {
        try {
            checkObjectMetadata(s3Client, bucketName, key)

            val transferManager: TransferManager = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build()
            val upload: Upload = transferManager.upload(bucketName, key, new File(filePath))
            while (!upload.isDone()) {
                println("Upload state: " + upload.getState().toString())
                println("Percent transferred: " + upload.getProgress().getPercentTransferred())
                Thread.sleep(1000)
            }

            checkObjectMetadata(s3Client, bucketName, key)
        } catch { case e: Exception => outputException(e) }
        println()
    }

}