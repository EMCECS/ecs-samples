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

import java.util.Map.Entry;

public class _06_ReadObjectsWithMetadata extends BucketAndObjectValidator {

    public static void main(String[] args) throws Exception {
        readObjectMetadata(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT);
        readObjectMetadata(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2, AWSS3Factory.S3_OBJECT);
    }

    /**
     * @param s3Client
     * @param bucketName
     * @param key
     */
    private static void readObjectMetadata(AmazonS3 s3Client, String bucketName, String key) {
        try {
            ObjectMetadata metadata = s3Client.getObjectMetadata(bucketName, key);

            System.out.println( String.format("Object [%s/%s] has system metadata:",
                    bucketName, key));
            for (Entry<String, Object> metaEntry : metadata.getRawMetadata().entrySet()) {
                System.out.println( "    " + metaEntry.getKey() + " = " + metaEntry.getValue() );
            }
            System.out.println( "and user metadata:" );
            for (Entry<String, String> metaEntry : metadata.getUserMetadata().entrySet()) {
                System.out.println( "    " + metaEntry.getKey() + " = " + metaEntry.getValue() );
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

}
