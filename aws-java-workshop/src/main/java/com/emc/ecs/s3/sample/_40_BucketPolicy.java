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
import com.amazonaws.services.s3.model.BucketPolicy;

public class _40_BucketPolicy {

    public static void main(String[] args) throws Exception {
        String invalidPolicyText = "invalidPolicyText";
        String policyText = "{" +
                "\"Version\":\"2012-10-17\"," +
                "\"Id\":\"StaticWebsitePolicy" + AWSS3Factory.S3_BUCKET + "\"," +
                "\"Statement\":[{" +
                "    \"Sid\":\"PublicReadGetObject\"," +
                "    \"Effect\":\"Allow\"," +
                "    \"Principal\": \"*\"," +
                "    \"Action\":[" +
                "        \"s3:GetObject\"" +
                "    ]," +
                "    \"Resource\":[" +
                "        \"" + AWSS3Factory.S3_BUCKET + "/*\"" +
                "    ]" +
                "}]" +
                "}";
        String policyText2 = "{" +
                "\"Version\":\"2012-10-17\"," +
                "\"Id\":\"StaticWebsitePolicy" + AWSS3Factory.S3_BUCKET_2 + "\"," +
                "\"Statement\":[{" +
                "    \"Sid\":\"PublicReadGetObject\"," +
                "    \"Effect\":\"Allow\"," +
                "    \"Principal\": \"*\"," +
                "    \"Action\":[" +
                "        \"s3:GetObject\"" +
                "    ]," +
                "    \"Resource\":[" +
                "        \"" + AWSS3Factory.S3_BUCKET_2 + "/*\"" +
                "    ]" +
                "}]" +
                "}";

        setBucketPolicy( AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, policyText, invalidPolicyText );
        setBucketPolicy( AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2, policyText2, policyText );
    }


    /**
     * @param s3Client
     * @param bucketName
     * @param policyText
     * @param invalidPolicyText
     */
    private static void setBucketPolicy(AmazonS3 s3Client, String bucketName, String policyText, String invalidPolicyText ) {
        try {
            checkBucketPolicy( s3Client, bucketName );

            setAndCheckInvalidBucketPolicy( s3Client, bucketName, invalidPolicyText );

            s3Client.setBucketPolicy(bucketName, policyText);

            checkBucketPolicy( s3Client, bucketName );

            setAndCheckInvalidBucketPolicy( s3Client, bucketName, invalidPolicyText );

            s3Client.deleteBucketPolicy(bucketName);

            checkBucketPolicy( s3Client, bucketName );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    /**
     * @param s3Client
     * @param bucketName
     * @param invalidPolicyText
     */
    private static void setAndCheckInvalidBucketPolicy(AmazonS3 s3Client, String bucketName, String invalidPolicyText) {
        try {
            s3Client.setBucketPolicy(bucketName, invalidPolicyText);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println();
        }

        checkBucketPolicy( s3Client, bucketName );
    }


    /**
     * @param s3Client
     * @param bucketName
     */
    private static void checkBucketPolicy(AmazonS3 s3Client, String bucketName) {
        try {
            BucketPolicy result = s3Client.getBucketPolicy(bucketName);
            if ( ( result == null ) || ( result.getPolicyText() == null ) ) {
                System.out.println("bucket policy for " + bucketName + ": none.");
            } else {
                System.out.println("bucket policy for " + bucketName + ":");
                System.out.println(result.getPolicyText());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        System.out.println();
    }

}
