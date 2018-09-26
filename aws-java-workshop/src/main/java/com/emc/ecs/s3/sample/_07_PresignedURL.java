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

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

public class _07_PresignedURL {

    public static void main(String[] args) throws Exception {
        // create the AWS S3 Client
        AmazonS3 s3 = AWSS3Factory.getS3ClientWithV2Signatures();

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
        GeneratePresignedUrlRequest presignedUrl = new GeneratePresignedUrlRequest(AWSS3Factory.S3_BUCKET, key);
        presignedUrl.setMethod(HttpMethod.GET);
        presignedUrl.setExpiration(expiration);

        URL url = s3.generatePresignedUrl(presignedUrl);

        // print object's pre-signed URL
        System.out.println( String.format("object [%s/%s] pre-signed URL: [%s]",
                AWSS3Factory.S3_BUCKET, key, url.toString()));
    }
}
