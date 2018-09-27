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
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.util.StringInputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class _01_CreateObjects {

    public static void main(String[] args) throws Exception {
        // get object content from user
        System.out.println( "Enter the object content:" );
        String content = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        createObject(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, content);
        createObject(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2, content);
    }

    /**
     * @param s3Client
     * @param theBucket
     * @param content
     */
    private static void createObject(AmazonS3 s3Client, String theBucket, final String content) {
        try {
            PutObjectResult por = s3Client.putObject(theBucket, AWSS3Factory.S3_OBJECT, new StringInputStream( content ), null);
            System.out.println(String.format("created object [%s/%s (ETag: %s)] with content: [%s]",
                    theBucket, AWSS3Factory.S3_OBJECT, por.getETag(), content));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

}
