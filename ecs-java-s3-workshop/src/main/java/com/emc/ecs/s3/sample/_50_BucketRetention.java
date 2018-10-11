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

import com.emc.object.s3.S3Client;
import com.emc.object.s3.bean.BucketInfo;
import com.emc.object.s3.request.CreateBucketRequest;

public class _50_BucketRetention {

    public static void main(String[] args) throws Exception {
        long retentionPeriod = 5;

        createBucket( ECSS3Factory.getS3Client(), ECSS3Factory.S3_BUCKET, ECSS3Factory.S3_OBJECT, retentionPeriod );
    }

    /**
     * @param s3Client
     * @param bucketName
     * @param key
     * @param retentionPeriod
     */
    private static void createBucket(S3Client s3Client, String bucketName, String key, long retentionPeriod) {
        try {
            CreateBucketRequest request = new CreateBucketRequest(bucketName);
            request.setRetentionPeriod(retentionPeriod);
            s3Client.createBucket(request);
            BucketInfo bucketInfo = s3Client.getBucketInfo(bucketName);
            System.out.println( "Created bucket " + bucketName + " with retention period " + bucketInfo.getRetentionPeriod() + " seconds." );

            s3Client.putObject(bucketName, key, "some content", null);
            try {
                s3Client.deleteObject(bucketName, key);
                System.out.println( "OOPS!!!! Successfully deleted!" );
            } catch (Exception e) {
                System.out.println( "Expected exception: " + e.getMessage() );
                System.out.println();
            }

            System.out.println("Waiting " + retentionPeriod + " seconds...");
            Thread.sleep(retentionPeriod * 1000);

            s3Client.deleteObject(bucketName, key);
            System.out.println( "Successfully deleted!" );
            s3Client.deleteBucket(bucketName);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        System.out.println();
    }
}
