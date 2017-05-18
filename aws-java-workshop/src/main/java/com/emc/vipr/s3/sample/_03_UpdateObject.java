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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.StringInputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class _03_UpdateObject {

	public static void main(String[] args) throws Exception {
    	// create the AWS S3 Client
        AmazonS3 s3 = AWSS3Factory.getS3Client();

    	// retrieve the object key and new object value from user
        System.out.println( "Enter the object key:" );
        String key = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        System.out.println( "Enter new object content:" );
        String content = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        
        // update the object in the demo bucket
        PutObjectRequest updateRequest = new PutObjectRequest(AWSS3Factory.S3_BUCKET, key,
        		new StringInputStream(content), null);

        s3.putObject(updateRequest);

        // print out object key/value for validation
    	System.out.println( String.format("update object [%s/%s] with new content: [%s]",
    			AWSS3Factory.S3_BUCKET, key, content));
    }
}
