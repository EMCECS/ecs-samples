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
import java.util.Map.Entry;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

public class BucketAndObjectValidator {

    /**
     * @param s3Client
     * @param bucketName
     */
    protected static void checkBucketExistence(AmazonS3 s3Client, String bucketName) {
        try {
            String state = s3Client.doesBucketExistV2(bucketName) ? "exists" : "does not exist";
            System.out.println( String.format("Bucket [%s] %s.", 
                    bucketName, state));
            System.out.println();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    /**
     * @param s3Client
     * @param bucketName
     * @param key
     */
    protected static void checkObjectExistence(AmazonS3 s3Client, String bucketName, String key) {
        try {
            String state = s3Client.doesObjectExist(bucketName, key) ? "exists" : "does not exist";
            System.out.println( String.format("Object [%s/%s] %s",
                    bucketName, key, state));
            System.out.println();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    /**
     * @param s3Client
     * @param bucketName
     * @param key
     */
    protected static void checkObjectContent(AmazonS3 s3Client, String bucketName, String key) {
        try {
            S3Object object = s3Client.getObject(bucketName, key);
            BufferedReader reader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
            String returnedContent = reader.readLine();
            System.out.println( String.format("Object [%s/%s] exists with content: [%s]",
                    object.getBucketName(), object.getKey(), returnedContent));
        } catch (Exception e) {
            System.out.println( String.format("Object [%s/%s] does not exist",
                    bucketName, key));
        }
        System.out.println();
    }

    /**
     * @param s3Client
     * @param bucketName
     * @param key
     */
    protected static void checkObjectMetadata(AmazonS3 s3Client, String bucketName, String key) {
        try {
            ObjectMetadata metadata = s3Client.getObjectMetadata(bucketName, key);
            System.out.println( String.format("Object [%s/%s] exists with system metadata:",
                    bucketName, key));
            for (Entry<String, Object> metaEntry : metadata.getRawMetadata().entrySet()) {
                System.out.println( "    " + metaEntry.getKey() + " = " + metaEntry.getValue() );
            }
            System.out.println( "and user metadata:" );
            for (Entry<String, String> metaEntry : metadata.getUserMetadata().entrySet()) {
                System.out.println( "    " + metaEntry.getKey() + " = " + metaEntry.getValue() );
            }
        } catch (Exception e) {
            System.out.println( String.format("Object [%s/%s] does not exist",
                    bucketName, key));
        }
        System.out.println();
    }

}
