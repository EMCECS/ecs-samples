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

import java.net.URL
import java.util.Date

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3

object _10_PresignedUrls extends BucketAndObjectValidator {

    /**
     * Run the class.
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        val hours: Long = 1
        val expiration: Date = new Date()
        expiration.setTime(expiration.getTime() + 60 * 60 * 1000 * hours)
        val key: String = "new~key"

        // Create URLs with V2 signatures
        createPresignedURLs(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, key, expiration)
        // Create URLs with V4 signatures.
        createPresignedURLs(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, key, expiration)
    }

    /**
     * Generate GET and PUT URLs.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     * @param key the key to use
     * @param expiration when the URL should expire
     */
    def createPresignedURLs(s3Client: AmazonS3, bucketName: String, key: String, expiration: Date) = {
        try {
            val getUrl: URL = s3Client.generatePresignedUrl(bucketName, key, expiration, HttpMethod.GET)
            val putUrl: URL = s3Client.generatePresignedUrl(bucketName, key, expiration, HttpMethod.PUT)

            println( String.format("object [%s/%s] pre-signed GET URL:", bucketName, key))
            println( getUrl.toString() )
            println( String.format("object [%s/%s] pre-signed PUT URL:", bucketName, key))
            println( putUrl.toString() )
        } catch { case e: Exception => outputException(e) }
        println()
    }

}