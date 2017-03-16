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
package com.emc.vipr.s3.sample;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.StringInputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class _01_CreateObject {

	public static void main(String[] args) throws Exception {
    	// create the AWS S3 Client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

    	// retrieve object key/value from user
        System.out.println( "Enter the object key:" );
        String key = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        System.out.println( "Enter the object content:" );
        String content = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        
        // create the object in the demo bucket
        s3.putObject(AWSS3Factory.S3_BUCKET, key, new StringInputStream(content), null);

        // print bucket key/value and content for validation
    	System.out.println( String.format("created object [%s/%s] with content: [%s]",
    			AWSS3Factory.S3_BUCKET, key, content));
    }
}
