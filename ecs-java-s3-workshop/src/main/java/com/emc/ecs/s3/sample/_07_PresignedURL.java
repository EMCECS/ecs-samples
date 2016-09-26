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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

public class _07_PresignedURL {

	public static void main(String[] args) throws Exception {
    	// create the ECS S3 Client
    	S3Client s3 = ECSS3Factory.getS3Client();

    	// retrieve the key value from user
        System.out.println( "Enter the object key:" );
        String key = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        
        // retrieve the expiration time for the object from user
        System.out.print( "How many hours should this tag be valid? " );
        String hours = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        // convert hours to a date
        Date expiration = new Date();
        long curTime_msec = expiration.getTime();
        long nHours = Long.valueOf(hours);
        curTime_msec += 60 * 60 * 1000 * nHours;
        expiration.setTime(curTime_msec); 
        
        // generate the object's pre-signed URL
        URL url = s3.getPresignedUrl(ECSS3Factory.S3_BUCKET, key, expiration);

        
        // print object's pre-signed URL
    	System.out.println( String.format("object [%s/%s] pre-signed URL: [%s]",
    			ECSS3Factory.S3_BUCKET, key, url.toString()));
    }
}
