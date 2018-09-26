/*
 * Copyright 2013-2018 EMC Corporation. All Rights Reserved.
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
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.StringInputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class _05_CreateObjectWithMetadata {

    public static void main(String[] args) throws Exception {
        // create the AWS S3 Client
        AmazonS3 s3 = AWSS3Factory.getS3ClientWithV2Signatures();

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
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata(metaKey, metaValue);

        // Set the content type for streaming back to a browser.
        metadata.setContentType("text/plain");

        // create the object with the metadata in the demo bucket
        s3.putObject(AWSS3Factory.S3_BUCKET, key, new StringInputStream(content), metadata);

        // print out object key/value and metadata key/value for validation
        System.out.println( String.format("created object [%s/%s] with metadata [%s=%s] and content: [%s]",
                AWSS3Factory.S3_BUCKET, key, metaKey, metaValue, content));
    }
}
