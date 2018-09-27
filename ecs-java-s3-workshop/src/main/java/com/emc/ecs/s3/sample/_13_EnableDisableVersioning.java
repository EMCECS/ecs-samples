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
import com.emc.object.s3.bean.VersioningConfiguration;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class _13_EnableDisableVersioning {

    public static void main(String[] args) throws Exception {
        // create the ECS S3 Client
        S3Client s3 = ECSS3Factory.getS3Client();

        // create the versioning configuration
        VersioningConfiguration vc = new VersioningConfiguration();

        System.out.print("Select an option:\n\t1. Enable\n\t2. Disable\n\nOption: ");
        String option = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        if(option.equals("1")) {
            vc.setStatus(VersioningConfiguration.Status.Enabled);
        }else if (option.equals("2")){
            vc.setStatus(VersioningConfiguration.Status.Suspended);
        }else {
            System.exit(0);
        }

        // update the bucket versioning configuration
        s3.setBucketVersioning(ECSS3Factory.S3_BUCKET, vc);

        // print bucket key/value and content for validation
        System.out.println( String.format("bucket [%s] versioning status [%s]",
                ECSS3Factory.S3_BUCKET, vc.getStatus().toString()));
    }
}
