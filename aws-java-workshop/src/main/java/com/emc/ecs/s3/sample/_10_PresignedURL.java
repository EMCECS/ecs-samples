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

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;

import java.net.URL;
import java.util.Date;

public class _10_PresignedURL {

    public static void main(String[] args) throws Exception {
        long hours = 1;
        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + 60 * 60 * 1000 * hours); 

        createPresignedURL(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, expiration);
        createPresignedURL(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, expiration);
    }

    /**
     * @param s3Client
     * @param bucketName
     * @param key
     * @param expiration
     */
    private static void createPresignedURL(AmazonS3 s3Client, String bucketName, String key,
            Date expiration) {
        try {
            URL url = s3Client.generatePresignedUrl(bucketName, key, expiration, HttpMethod.GET);

            // print object's pre-signed URL
            System.out.println( String.format("object [%s/%s] pre-signed URL:",
                    bucketName, key, url.toString()));
            System.out.println( url.toString() );
            System.out.println();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
