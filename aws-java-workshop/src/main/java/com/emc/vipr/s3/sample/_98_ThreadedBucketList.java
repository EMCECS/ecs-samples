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
package com.emc.vipr.s3.sample;

import com.amazonaws.services.s3.AmazonS3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;


public class _98_ThreadedBucketList {
    public static void main(String[] args) throws Exception {
        AmazonS3 s3 = AWSS3Factory.getS3ClientWithV2Signatures();
        try {
            // retrieve object key/value from user
            System.out.println( "Enter the prefix key:" );
            String prefix = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
            System.out.println( "Enter the delimiter:" );
            String delimiter = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
            System.out.println( "Enter the number of threads:" );
            String threads = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

            String theBucket = AWSS3Factory.S3_BUCKET;
            BucketList bucketList = new BucketList(s3, theBucket, prefix, delimiter, Integer.valueOf(threads));

            // update the user
            final AtomicBoolean monitorRunning = new AtomicBoolean(true);
            final BucketList fBucketList = bucketList;
            Thread statusThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (monitorRunning.get()) {
                        try {
                            System.out.println("Objects listed: " + fBucketList.getListedObjects() +
                                    " Prefixes listed: " + fBucketList.getListedPrefixes() + "\r");
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                }
            });
            statusThread.setDaemon(true);
            statusThread.start();

            long startTime = System.currentTimeMillis();

            bucketList.run();

            long duration = System.currentTimeMillis() - startTime;
            double xput = (double) bucketList.getListedObjects() / duration * 1000;

            monitorRunning.set(false);
            System.out.println();

            System.out.println(String.format("Duration: %d secs (%.2f/s)", duration / 1000, xput));

            for (String error : bucketList.getErrors()) {
                System.out.println("Error: " + error);
            }

        } catch (Throwable t) {
            System.out.println("Error: " + t.getMessage());
            System.exit(2);
        }
    }
}
