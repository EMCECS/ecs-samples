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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Date;

public class _10_ReadLargeObjectTM {

    public static void main(String[] args) throws Exception {
        // create the AWS S3 Client
        AmazonS3 s3 = AWSS3Factory.getS3ClientWithV2Signatures();

        // retrieve the key value from user
        System.out.println( "Enter the object key:" );
        String key = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        // print start time
        Date start_date = new Date();
        System.out.println(start_date.toString());

        // file will be placed in temp dir with .tmp extension
        File file = File.createTempFile("read-large-object-tm", null);

        TransferManager tm = TransferManagerBuilder.standard()
                .withS3Client(s3)
                .build();

        // download the object to file
        Download download = tm.download(AWSS3Factory.S3_BUCKET, key, file);

        // block until download finished
        download.waitForCompletion();

        tm.shutdownNow();

        // print end time
        Date end_date = new Date();
        System.out.println(end_date.toString());
    }
}
