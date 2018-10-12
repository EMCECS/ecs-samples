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
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.CORSRule;
import com.amazonaws.services.s3.model.CORSRule.AllowedMethods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class _43_BucketCors {

    public static void main(String[] args) throws Exception {
        String id = "cors-rule-1";
        String origin = "*";
        int maxAgeSeconds = 60;

        setBucketCrossOriginConfiguration( AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, id, origin, maxAgeSeconds );
        setBucketCrossOriginConfiguration( AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2, id, origin, maxAgeSeconds );
    }


   /**
     * @param s3Client
     * @param bucketName
     * @param id
     * @param origin
     * @param maxAgeSeconds
     */
    private static void setBucketCrossOriginConfiguration(AmazonS3 s3Client, String bucketName, String id, String origin,
            int maxAgeSeconds) {
        try {
            checkBucketCrossOriginConfiguration( s3Client, bucketName );

            List<CORSRule.AllowedMethods> allowedMethods = new ArrayList<CORSRule.AllowedMethods>();
            allowedMethods.add(CORSRule.AllowedMethods.PUT);
            allowedMethods.add(CORSRule.AllowedMethods.GET);
            allowedMethods.add(CORSRule.AllowedMethods.HEAD);

            CORSRule rule = new CORSRule()
                    .withId(id)
                    .withMaxAgeSeconds(maxAgeSeconds)
                    .withAllowedOrigins(Arrays.asList(origin))
                    .withAllowedMethods(allowedMethods);
    
            // add the rule to a configuration
            BucketCrossOriginConfiguration configuration = new BucketCrossOriginConfiguration(Arrays.asList(rule));

            // save the lifecycle configuration
            s3Client.setBucketCrossOriginConfiguration(bucketName, configuration);;

            checkBucketCrossOriginConfiguration( s3Client, bucketName );

            s3Client.deleteBucketCrossOriginConfiguration(bucketName);

            checkBucketCrossOriginConfiguration( s3Client, bucketName );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    /**
     * @param s3Client
     * @param bucketName
     */
    private static void checkBucketCrossOriginConfiguration(AmazonS3 s3Client, String bucketName) {
        try {
            BucketCrossOriginConfiguration result = s3Client.getBucketCrossOriginConfiguration(bucketName);
            if ( result == null ) {
                System.out.println("bucket CORS configuration for " + bucketName + ": none.");
            } else if ( ( result.getRules() == null ) || ( result.getRules().size() == 0 ) ) {
                    System.out.println("bucket CORS configuration for " + bucketName + ": no rules.");
            } else {
                CORSRule rule = result.getRules().get(0);
                String allowedMethods = "";
                for (AllowedMethods allowedMethod : rule.getAllowedMethods()) {
                    if (allowedMethods.length() > 0) {
                        allowedMethods = allowedMethods + ",";
                    }
                    allowedMethods = allowedMethods + allowedMethod.toString();
                }
                System.out.println(String.format("bucket CORS configuration rule 0 for " + bucketName + ": id %s, first allowed origin %s, allowedMethods [%s], and max age seconds %s.",
                        rule.getId(), rule.getAllowedOrigins().get(0), allowedMethods, rule.getMaxAgeSeconds()));
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

}
