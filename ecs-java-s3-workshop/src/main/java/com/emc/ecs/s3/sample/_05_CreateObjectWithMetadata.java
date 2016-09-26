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
import com.emc.object.s3.S3ObjectMetadata;
import com.emc.object.s3.request.PutObjectRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class _05_CreateObjectWithMetadata {

	public static void main(String[] args) throws Exception {
    	// create the ECS S3 Client
    	S3Client s3 = ECSS3Factory.getS3Client();

    	// retrieve the object key and value from user
        System.out.println( "Enter the object key:" );
        String key = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        System.out.println( "Enter the object content:" );
        String content = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        
        //retrieve the object metadata key and value from user
        System.out.println( "Enter the metadata key:" );
        String metaKey = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        System.out.println( "Enter the metadata content:" );
        String metaValue = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        // create the metadata
        S3ObjectMetadata metadata = new S3ObjectMetadata();
        metadata.addUserMetadata(metaKey, metaValue);
        
        // create the object with the metadata in the demo bucket
        PutObjectRequest req = new PutObjectRequest(ECSS3Factory.S3_BUCKET, key, content).withObjectMetadata(metadata);
        s3.putObject(req);

        // print out object key/value and metadata key/value for validation
    	System.out.println( String.format("created object [%s/%s] with metadata [%s=%s] and content: [%s]",
    			ECSS3Factory.S3_BUCKET, key, metaKey, metaValue, content));
    }
}
