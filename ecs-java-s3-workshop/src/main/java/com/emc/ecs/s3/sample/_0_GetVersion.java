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
import com.emc.object.s3.bean.ListDataNode;

public class _0_GetVersion {

	public static void main(String[] args) throws Exception {
    	// create the ECS S3 Client
    	S3Client s3 = ECSS3Factory.getS3Client();

    	// Get the list of data nodes.  This will also contain the version information.
 		ListDataNode nodes = s3.listDataNodes();

    	// print out the service name and endpoint for validation
    	System.out.println( String.format("Successfully connected to ECS using the [%s] service at [%s:%d]",
    			nodes.getVersionInfo(), ECSS3Factory.S3_HOST, ECSS3Factory.S3_PORT));
    }
}
