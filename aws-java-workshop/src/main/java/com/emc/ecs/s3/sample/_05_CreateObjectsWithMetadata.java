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
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.StringInputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class _05_CreateObjectsWithMetadata extends BucketAndObjectValidator {

    public static void main(String[] args) throws Exception {
        //get custom metadata key and value from the user
        System.out.println( "Enter the user metadata key:" );
        String metaKey = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        System.out.println( "Enter the user metadata value:" );
        String metaValue = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        createObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, metaKey, metaValue);
        createObject(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2, AWSS3Factory.S3_OBJECT, metaKey, metaValue);
    }

    /**
     * @param s3Client
     * @param bucketName
     * @param key
     * @param metaKey
     * @param metaValue
     */
    private static void createObject(AmazonS3 s3Client, String bucketName, String key,
            String metaKey, String metaValue) {
        try {
            checkObjectMetadata(s3Client, bucketName, key);
    
            // create the metadata
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata(metaKey, metaValue);
            // create the object with the metadata in the demo bucket
            s3Client.putObject(bucketName, key, new StringInputStream( "" ), metadata);
    
            checkObjectMetadata(s3Client, bucketName, key);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

}
