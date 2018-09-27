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

public class _99_DeleteBuckets {

    public static void main(String[] args) throws Exception {
        emptyAndDeleteBucket(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET);
        emptyAndDeleteBucket(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_V4);
    }

    /**
     * @param s3Client
     * @param s3Bucket
     */
    private static void emptyAndDeleteBucket(AmazonS3 s3Client, String s3Bucket) {
        // delete the demo bucket and all its content
        if (BucketVersioningConfiguration.OFF.equals(s3Client.getBucketVersioningConfiguration(s3Bucket).getStatus())) {
            // no versioning, so list objects
            for (S3ObjectSummary summary : s3Client.listObjects(s3Bucket).getObjectSummaries()) {
                s3Client.deleteObject(s3Bucket, summary.getKey());
                System.out.println(String.format("Deleted [%s/%s]", s3Bucket, summary.getKey()));
            }
        } else {
            // versioning was enabled, list versions
            for (S3VersionSummary summary : s3Client.listVersions(s3Bucket, null).getVersionSummaries()) {
                s3Client.deleteVersion(s3Bucket, summary.getKey(), summary.getVersionId());
                System.out.println(String.format("Deleted [%s/%s] (vId: %s)", s3Bucket, summary.getKey(), summary.getVersionId()));
            }
        }

        s3Client.deleteBucket(s3Bucket);

        // print bucket key/value and content for validation
        System.out.println(String.format("deleted bucket [%s]", s3Bucket));
    }
}
