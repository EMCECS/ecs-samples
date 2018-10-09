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
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class _09_ReadLargeObjectsByRange extends BucketAndObjectValidator {

    public static void main(String[] args) throws Exception {
        long start = 1000;
        long end = 1300;
        readLargeObjectRange(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, start, end);
        readLargeObjectRange(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2, AWSS3Factory.S3_OBJECT, start, end);
    }

    /**
     * @param s3Client
     * @param bucketName
     * @param key
     * @param start
     * @param end
     */
    private static void readLargeObjectRange(AmazonS3 s3Client, String bucketName, String key, long start,
            long end) {
        try {
            GetObjectRequest getObjectRequest = new GetObjectRequest( bucketName, key ).withRange( start, end );
            S3Object object = s3Client.getObject(getObjectRequest);

            BufferedReader reader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
            String line = reader.readLine();
            String separator = "";
            StringBuilder content = new StringBuilder();
            while (line != null) {
                content.append(separator).append(line);
                separator = "\n";
                line = reader.readLine();
            }

            System.out.println( String.format("object [%s/%s] content:\n%s",
                    object.getBucketName(), object.getKey(), content));
            System.out.println();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

}
