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
import com.amazonaws.services.s3.transfer.Download
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.TransferManagerBuilder

object _08_ReadLargeObjects extends BucketAndObjectValidator {

    /**
     * Run the class.
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        val fileNamePrefix: String = "demo-file-"

        readLargeObject(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, fileNamePrefix)
    }

    /**
     * Check the object metadata, then download the content to a temporary file and output the location.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     * @param key the object to download
     * @param fileNamePrefix the temporary file prefix to use
     */
    def readLargeObject(s3Client: AmazonS3, bucketName: String, key: String, fileNamePrefix: String) = {
        try {
            checkObjectMetadata(s3Client, bucketName, key)

            // file will be placed in temp dir with .tmp extension
            val file: File = File.createTempFile(fileNamePrefix, null)
    
            val transferManager: TransferManager = TransferManagerBuilder.standard()
                    .withS3Client(s3Client)
                    .build()
    
            // download the object to file
            val download: Download = transferManager.download(bucketName, key, file)

            while (!download.isDone()) {
                println("Download state: " + download.getState().toString())
                println("Percent transferred: " + download.getProgress().getPercentTransferred())
                Thread.sleep(1000)
            }

            println("Download is finished, content is in the following file.")
            println(file.getAbsolutePath())
        } catch { case e: Exception => outputException(e) }
        println()
    }

}