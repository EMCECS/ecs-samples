/*
 * Copyright 2013 EMC Corporation. All Rights Reserved.
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
import com.emc.object.s3.bean.VersioningConfiguration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class _17_BucketLifecycle {

	public static void main(String[] args) throws Exception {
	// create the ECS S3 Client
	S3Client s3 = ECSS3Factory.getS3Client();


    LifecycleRule rule = new LifecycleRule("rule-a", "abc", LifecycleRule.Status.Enabled, 1);
    LifecycleConfiguration lc = new LifecycleConfiguration()
            .withRules(rule);

	s3.setBucketLifecycle(ECSS3Factory.S3_BUCKET, lc);
        // print bucket key/value and content for validation
        System.out.println( String.format("bucket [%s] lifecycle set",
                ECSS3Factory.S3_BUCKET));
    }
}
