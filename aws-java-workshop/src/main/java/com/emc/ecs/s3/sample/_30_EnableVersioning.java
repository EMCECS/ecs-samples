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
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;

public class _30_EnableVersioning {

    public static void main(String[] args) throws Exception {
        createVersionedBucket(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_VERSIONED_BUCKET);
    }

    /**
     * @param s3Client
     * @param s3Versionbucket
     */
    private static void createVersionedBucket(AmazonS3 s3Client, String bucketName) {
        s3Client.createBucket(bucketName);
        BucketVersioningConfiguration bucketVersioningConfiguration = new BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED);
        SetBucketVersioningConfigurationRequest setBucketVersioningConfigurationRequest = new SetBucketVersioningConfigurationRequest(bucketName, bucketVersioningConfiguration);
        s3Client.setBucketVersioningConfiguration(setBucketVersioningConfigurationRequest);

        checkVersioningStatus(s3Client, bucketName);
    }

    /**
     * @param s3Client
     * @param bucketName
     */
    private static void checkVersioningStatus(AmazonS3 s3Client, String bucketName) {
        try {
            BucketVersioningConfiguration bucketVersioningConfiguration = s3Client.getBucketVersioningConfiguration(bucketName);
            System.out.println( String.format("Bucket [%s] has versioning configuration %s.", 
                    bucketName, bucketVersioningConfiguration.getStatus()));
            System.out.println();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

}
