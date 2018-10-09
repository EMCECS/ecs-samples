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

public class _11_CopyObject extends BucketAndObjectValidator {

    public static void main(String[] args) throws Exception {
        String newKey = "new-key";

        copyObject( AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2, newKey, AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT );
        copyObject( AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, newKey, AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT );
    }

    /**
     * @param s3Client
     * @param newBucketName
     * @param newKey
     * @param bucketName
     * @param key
     */
    private static void copyObject(AmazonS3 s3Client, String newBucketName, String newKey, String bucketName,
            String key) {
        try {
            BucketAndObjectValidator.checkObjectMetadata(s3Client, newBucketName, newKey);
    
            s3Client.copyObject(bucketName, key, newBucketName, newKey);
    
            BucketAndObjectValidator.checkObjectMetadata(s3Client, bucketName, key);
            BucketAndObjectValidator.checkObjectMetadata(s3Client, newBucketName, newKey);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

}
