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
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.util.StringInputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class _01_CreateObjects {

    public static void main(String[] args) throws Exception {
        // retrieve object key/value from user
        System.out.println( "Enter the object key:" );
        String key = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        System.out.println( "Enter the object content:" );
        String content = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        createObject(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, key, content);
        createObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_V4, key, content);
    }

    /**
     * @param s3Client
     * @param theBucket
     * @param key
     * @param content
     */
    private static void createObject(AmazonS3 s3Client, String theBucket, String key, final String content) {
        try {
            PutObjectResult por = s3Client.putObject(theBucket, key, new StringInputStream( content ), null);
    
            //_01_CreateObject example = new _01_CreateObject();
    
            //PutObjectResult por = example.putObjectViaRequest(s3Client, theBucket, key, content);
    
            //PutObjectResult por = example.putManyObjectsWithPrefix(s3Client, key, content, "demo-obj/", 9);
    
            System.out.println(String.format("created object [%s/%s (ETag: %s)] with content: [%s]",
                    theBucket, key, por.getETag(), content));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    public PutObjectResult putObjectViaRequest(AmazonS3 s3, String bucket, String key, String content) throws Exception {
        PutObjectRequest poReq = new PutObjectRequest(bucket, key, new StringInputStream(content), null);
        PutObjectResult por = s3.putObject(poReq);
        return por;
    }

    //will probably want to pass in a prefix ending in "/"
    public void putManyObjectsWithPrefix(AmazonS3 s3, String key, String content, String prefix, int numberOfObjects)
            throws java.io.IOException {

        for (int i=0; i<numberOfObjects;i++) {
            // create the object in the demo bucket
            s3.putObject(AWSS3Factory.S3_BUCKET, prefix + key + "_" + i, new StringInputStream(content), null);
            // print bucket key/value and content for validation
            System.out.println(String.format("created object [%s/%s] with content: [%s]",
                    AWSS3Factory.S3_BUCKET, key, content));
        }
    }
}
