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

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class _03_UpdateObjects extends BucketAndObjectValidator {

    public static void main(String[] args) throws Exception {
        // get new object content from user
        System.out.println( "Enter new object content:" );
        String newContent = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        updateObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, newContent);
        updateObject(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2, AWSS3Factory.S3_OBJECT, newContent);
    }

    /**
     * @param s3Client
     * @param s3Bucket
     * @param s3Object
     * @param newContent
     */
    private static void updateObject(AmazonS3 s3Client, String s3Bucket, String s3Object, final String newContent) {
        try {
            validateObjectExists(s3Client, s3Bucket, s3Object);

            s3Client.putObject(s3Bucket, s3Object, new StringInputStream( newContent ), null);

            validateObjectExists(s3Client, s3Bucket, s3Object);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

}
