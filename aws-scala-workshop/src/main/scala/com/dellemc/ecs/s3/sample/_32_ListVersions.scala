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

import scala.collection.JavaConversions._

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.S3VersionSummary

object _32_ListVersions extends BucketAndObjectValidator {

    /**
     * Run the class.
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        // list for a bucket that was never versioned.
        listVersions(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET)
        // list for a bucket that is currently versioned, but originally was not versioned.
        listVersions(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2)
        // list for a bucket that was always versioned
        listVersions(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_VERSIONED_BUCKET)
    }

    /**
     * List all versions stored in the bucket.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     */
    def listVersions(s3Client: AmazonS3, bucketName: String) = {
        try {
            val summaries: scala.collection.mutable.Iterable[S3VersionSummary] = s3Client.listVersions(bucketName, null).getVersionSummaries()
            println( s"Version listing found ${summaries.size()} versions in $bucketName" )
            summaries.foreach((i:S3VersionSummary) => {
                println( s"${i.getKey()} (${i.getVersionId()})")
            })
        } catch { case e: Exception => outputException(e) }
        println()
    }

}