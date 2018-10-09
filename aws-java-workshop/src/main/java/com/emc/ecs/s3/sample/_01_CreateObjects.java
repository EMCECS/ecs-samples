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
import com.amazonaws.util.StringInputStream;

public class _01_CreateObjects extends BucketAndObjectValidator {

    public static void main(String[] args) throws Exception {
        String content = "initial object content";

        createObject(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, content);
        createObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2, AWSS3Factory.S3_OBJECT, content);
    }

    /**
     * @param s3Client
     * @param bucketName
     * @param key
     * @param content
     */
    private static void createObject(AmazonS3 s3Client, String bucketName, String key, final String content) {
        try {
            checkObjectExistence(s3Client, bucketName, key);

            s3Client.putObject(bucketName, key, new StringInputStream( content ), null);

            checkObjectContent(s3Client, bucketName, key);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

}
