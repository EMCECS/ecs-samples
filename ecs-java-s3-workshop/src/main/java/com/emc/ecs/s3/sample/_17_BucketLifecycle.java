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

import com.emc.object.s3.S3Client;
import com.emc.object.s3.bean.LifecycleConfiguration;
import com.emc.object.s3.bean.LifecycleRule;


public class _17_BucketLifecycle {

    public static void main(String[] args) throws Exception {
        // create the ECS S3 Client
        S3Client s3 = ECSS3Factory.getS3Client();


        LifecycleRule rule = new LifecycleRule("rule-a", "abc", LifecycleRule.Status.Enabled)
                .withExpirationDays(1);
        LifecycleConfiguration lc = new LifecycleConfiguration()
                .withRules(rule);

        s3.setBucketLifecycle(ECSS3Factory.S3_BUCKET, lc);
        System.out.println(String.format("bucket [%s] lifecycle set",
                ECSS3Factory.S3_BUCKET));

        // print bucket key/value and content for validation
        System.out.println(String.format("obtaining rules for bucket [%s]",
                ECSS3Factory.S3_BUCKET));

        LifecycleConfiguration lc2 = s3.getBucketLifecycle(ECSS3Factory.S3_BUCKET);

        for (LifecycleRule lr : lc2.getRules()) {
            System.out.println(String.format("\t- rule: [id: %s, prefix: %s, status: %s, expirationDays: %s",
                    lr.getId(),
                    lr.getPrefix(),
                    lr.getStatus(),
                    lr.getExpirationDays()));
        }
    }
}
