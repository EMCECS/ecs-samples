/*
 * Copyright 2013-2018 EMC Corporation. All Rights Reserved.
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
import com.amazonaws.services.s3.model.S3Object;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class _02_ReadObjects {

    public static void main(String[] args) throws Exception {
        // retrieve the key value from user
        System.out.println( "Enter the object key:" );
        String key = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        readObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, key);
        readObject(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_V4, key);
    }

    /**
     * @param s3Client
     * @param s3Bucket
     * @param key
     */
    private static void readObject(AmazonS3 s3Client, String s3Bucket, String key) {
        try {
            // read the object from the demo bucket
            S3Object object = s3Client.getObject(s3Bucket, key);
    
            // convert object to a text string
            BufferedReader reader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
            String content = reader.readLine();
    
            // print object key/value and content for validation
            System.out.println( String.format("object [%s/%s] content: [%s]",
                    object.getBucketName(), object.getKey(), content));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
