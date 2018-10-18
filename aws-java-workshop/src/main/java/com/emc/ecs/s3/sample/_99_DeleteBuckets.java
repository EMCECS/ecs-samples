/*
 * Copyright 2013-2018 Dell Inc. or its subsidiaries. All Rights Reserved.
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
package com.emc.ecs.s3.sample;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;

public class _99_DeleteBuckets extends BucketAndObjectValidator {

    public static void main(String[] args) throws Exception {
        emptyAndDeleteBucket(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET);
        emptyAndDeleteBucket(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2);
        emptyAndDeleteBucket(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_VERSIONED_BUCKET);
    }

    /**
     * @param s3Client
     * @param bucketName
     */
    private static void emptyAndDeleteBucket(AmazonS3 s3Client, String bucketName) {
        try {
            checkBucketExistence(s3Client, bucketName);
    
            // delete all bucket content
            if (BucketVersioningConfiguration.OFF.equals(s3Client.getBucketVersioningConfiguration(bucketName).getStatus())) {
                // no versioning, so delete all objects
                for (S3ObjectSummary summary : s3Client.listObjects(bucketName).getObjectSummaries()) {
                    System.out.println(String.format("Deleting object [%s/%s]", bucketName, summary.getKey()));
                    s3Client.deleteObject(bucketName, summary.getKey());
                }
            } else {
                // versioning was enabled, so delete all versions
                for (S3VersionSummary summary : s3Client.listVersions(bucketName, null).getVersionSummaries()) {
                    System.out.println(String.format("Deleting version [%s/%s/%s]", bucketName, summary.getKey(), summary.getVersionId()));
                    s3Client.deleteVersion(bucketName, summary.getKey(), summary.getVersionId());
                }
            }
    
            // delete the bucket
            s3Client.deleteBucket(bucketName);
    
            checkBucketExistence(s3Client, bucketName);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

}
