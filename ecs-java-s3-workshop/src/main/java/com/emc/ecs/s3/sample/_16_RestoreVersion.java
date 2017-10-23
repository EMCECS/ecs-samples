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
import com.emc.object.s3.bean.AbstractVersion;
import com.emc.object.s3.bean.CopyObjectResult;
import com.emc.object.s3.bean.ListVersionsResult;
import com.emc.object.s3.request.CopyObjectRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class _16_RestoreVersion {

	public static void main(String[] args) throws Exception {
	// create the ECS S3 Client
	S3Client s3 = ECSS3Factory.getS3Client();

        // retrieve the object key and version values from user
        System.out.println( "Enter the object key:" );
        String key = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        System.out.println( "Enter the object version ID:" );
        String versionId = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        // create a request to restore the object version
        CopyObjectRequest request = new CopyObjectRequest(
                ECSS3Factory.S3_BUCKET,
                key,
                ECSS3Factory.S3_BUCKET,
                key
        );
        request.setSourceVersionId(versionId);

        // restore the version
        CopyObjectResult result = s3.copyObject(request);

        // print object key and version
        System.out.println( String.format("version ID [%s] restored on object [%s]",
                result.getSourceVersionId(), key));
    }
}
