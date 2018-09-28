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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;

public class BucketAndObjectValidator {

    /**
     * @param s3Client
     * @param s3Bucket
     */
    protected static void validateBucketExists(AmazonS3 s3Client, String s3Bucket) {
        ObjectListing objects = s3Client.listObjects(s3Bucket);
        System.out.println( String.format("Bucket [%s] exists.", 
                objects.getBucketName()));
    }

    /**
     * @param s3Client
     * @param s3Bucket
     */
    protected static void validateBucketDoesNotExist(AmazonS3 s3Client, String s3Bucket) {
        try {
            s3Client.listObjects(s3Bucket);
        } catch (Exception e) {
            System.out.println( String.format("Bucket [%s] does not exist.", 
                    s3Bucket));
        }
    }

    /**
     * @param s3Client
     * @param s3Bucket
     * @param s3Object
     */
    protected static void validateObjectDoesNotExist(AmazonS3 s3Client, String s3Bucket, String s3Object) {
        try {
            s3Client.getObjectMetadata(s3Bucket, s3Object);
        } catch (Exception e) {
            System.out.println( String.format("Object [%s/%s] does not exist",
                    s3Bucket, s3Object));
        }
    }

    /**
     * @param s3Client
     * @param s3Bucket
     * @param s3Object
     * @throws Exception 
     */
    protected static void validateObjectExists(AmazonS3 s3Client, String s3Bucket, String s3Object) throws Exception {
        S3Object object = s3Client.getObject(s3Bucket, s3Object);
        BufferedReader reader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
        String returnedContent = reader.readLine();
        System.out.println( String.format("Object [%s/%s] exists with content: [%s]",
                object.getBucketName(), object.getKey(), returnedContent));
    }

}
