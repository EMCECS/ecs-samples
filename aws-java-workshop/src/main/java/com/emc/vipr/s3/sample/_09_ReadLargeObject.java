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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Date;

public class _09_ReadLargeObject {

    public static void main(String[] args) throws Exception {
        // create the AWS S3 Client
        AmazonS3 s3 = AWSS3Factory.getS3Client();

        // retrieve the key value from user
        System.out.println( "Enter the object key:" );
        String key = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        // print start time
        Date start_date = new Date();
        System.out.println(start_date.toString());

        // file will be placed in temp dir with .tmp extension
        File file = File.createTempFile("read-large-object", null);

        LargeFileDownloader downloader = new LargeFileDownloader(s3, AWSS3Factory.S3_BUCKET, key, file);
        downloader.setThreads(8);
        downloader.setPartSize(128 * 1024 * 1024); // 64MiB
        downloader.run();

        byte[] readData = new byte[(int)downloader.getObjectSize()];
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.read(readData);
        raf.close();

        //print end time
        Date end_date = new Date();
        System.out.println(end_date.toString());
    }
}


