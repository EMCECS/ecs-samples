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
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration.Rule;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilter;
import com.amazonaws.services.s3.model.lifecycle.LifecyclePrefixPredicate;

import java.util.Arrays;

public class _42_LifecycleConfiguration {

    public static void main(String[] args) throws Exception {
        int days = 1;
        int nonCurrentDays = 2;
        String prefix = "main/";

        setBucketLifecycleConfiguration( AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, prefix, days, nonCurrentDays );
        setBucketLifecycleConfiguration( AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2, prefix, days, nonCurrentDays );
    }


   /**
     * @param s3Client
     * @param bucketName
     * @param prefix
     * @param days
     * @param nonCurrentDays
     */
    private static void setBucketLifecycleConfiguration(AmazonS3 s3Client, String bucketName, String prefix,
            int days, int nonCurrentDays) {
        try {
            checkBucketLifecycleConfiguration( s3Client, bucketName );

            // create the new rule
            Rule rule = new BucketLifecycleConfiguration.Rule()
                    .withExpirationInDays(days)
                    .withNoncurrentVersionExpirationInDays(nonCurrentDays)
                    .withId("rule-1")
                    .withFilter(new LifecycleFilter().withPredicate(new LifecyclePrefixPredicate(prefix)))
                    .withStatus(BucketLifecycleConfiguration.ENABLED.toString());
    
            // add the rule to a configuration
            BucketLifecycleConfiguration configuration = new BucketLifecycleConfiguration(Arrays.asList(rule));

            // save the lifecycle configuration
            s3Client.setBucketLifecycleConfiguration(bucketName, configuration);

            checkBucketLifecycleConfiguration( s3Client, bucketName );

            s3Client.deleteBucketLifecycleConfiguration(bucketName);

            checkBucketLifecycleConfiguration( s3Client, bucketName );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    /**
     * @param s3Client
     * @param bucketName
     */
    private static void checkBucketLifecycleConfiguration(AmazonS3 s3Client, String bucketName) {
        try {
            BucketLifecycleConfiguration result = s3Client.getBucketLifecycleConfiguration(bucketName);
            if ( result == null ) {
                System.out.println("bucket lifecycle configuration for " + bucketName + ": none.");
            } else if ( ( result.getRules() == null ) || ( result.getRules().size() == 0 ) ) {
                    System.out.println("bucket lifecycle configuration for " + bucketName + ": no rules.");
            } else {
                Rule rule = result.getRules().get(0);
                String filterInfo = "does not exist";
                LifecycleFilter filter = rule.getFilter();
                if ( filter != null ) {
                    if  ( ! ( filter.getPredicate() instanceof LifecyclePrefixPredicate ) ) {
                        filterInfo = "is not a prefix filter";
                    } else {
                        filterInfo = "covers prefix \"" + ( ( LifecyclePrefixPredicate) filter.getPredicate() ).getPrefix() + "\"";
                    }
                }
                System.out.println(String.format("bucket lifecycle configuration rule 0 for " + bucketName + ": filter %s, days %s, and non-current days %s.",
                        filterInfo, rule.getExpirationInDays(), rule.getNoncurrentVersionExpirationInDays()));
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

}
