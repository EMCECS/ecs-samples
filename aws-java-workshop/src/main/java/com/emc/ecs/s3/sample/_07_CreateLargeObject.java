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
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class _07_CreateLargeObject extends BucketAndObjectValidator {

    public static void main(String[] args) throws Exception {
        // retrieve object value from user
        System.out.println( "Enter the file location (C:\\Users\\EMC\\NameSpaceList.zip) :" );
        String filePath = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        createLargeObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, filePath);
        createLargeObject(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2, AWSS3Factory.S3_OBJECT, filePath);
    }

    /**
     * @param s3Client
     * @param bucketName
     * @param key
     * @param filePath
     */
    private static void createLargeObject(AmazonS3 s3Client, String bucketName, String key, String filePath) {
        try {
            checkObjectMetadata(s3Client, bucketName, key);

            TransferManagerBuilder builder = TransferManagerBuilder.standard();
            TransferManager transferManager = builder.withS3Client(s3Client).build();
            Upload upload = transferManager.upload(bucketName, key, new File( filePath ));
            while (!upload.isDone()) {
                System.out.println("Upload state: " + upload.getState().toString());
                System.out.println("Percent transferred: " + upload.getProgress().getPercentTransferred());
                Thread.sleep(1000);
            }

            checkObjectMetadata(s3Client, bucketName, key);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

}
