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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class _14_LifecyclePolicy {

    public static void main(String[] args) throws Exception {

        System.out.println( "Enter the number of days to keep objects before delete:" );
        String days = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        System.out.println( "Enter the prefix (i.e. folder1/) to identify specific objects to delete:" );
        String prefix = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        // create the AWS S3 Client
        AmazonS3 s3 = AWSS3Factory.getS3ClientWithV2Signatures();

        // create the new rule
        BucketLifecycleConfiguration.Rule rule = new BucketLifecycleConfiguration.Rule()
                .withExpirationInDays(Integer.valueOf(days))
                .withId("rule-1")
                .withPrefix(prefix.trim())
                .withStatus(BucketLifecycleConfiguration.ENABLED.toString());

        // build the rule into configuration type
        BucketLifecycleConfiguration configuration = new BucketLifecycleConfiguration(Arrays.asList(rule));

        // save the lifecycle policy
        s3.setBucketLifecycleConfiguration(AWSS3Factory.S3_BUCKET, configuration);

        // Retrieve configuration.
        BucketLifecycleConfiguration result = s3.getBucketLifecycleConfiguration(AWSS3Factory.S3_BUCKET);
        System.out.println(String.format("bucket lifecycle configuration: prefix is %s and days is %s.",
                result.getRules().get(0).getPrefix(), result.getRules().get(0).getExpirationInDays()));

        //s3.deleteBucketLifecycleConfiguration(AWSS3Factory.S3_BUCKET);
    }

}
