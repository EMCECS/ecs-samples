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

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

public class _22_ListObjectsByPages extends BucketAndObjectValidator {

    public static void main(String[] args) throws Exception {
        listObjectsByPages(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET);
        listObjectsByPages(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2);
    }

    /**
     * @param s3Client
     * @param bucketName
     */
    private static void listObjectsByPages(AmazonS3 s3Client, String bucketName) {
        try {
            List<String> keys = new ArrayList<String>();
            int pages = 1;

            ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
            listObjectsRequest.setBucketName(bucketName);
            listObjectsRequest.setMaxKeys(1);
            ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);
            addKeys( keys, objectListing.getObjectSummaries() );
            while (objectListing.isTruncated()) {
                ++pages;
                listObjectsRequest.setMarker( keys.get( keys.size() - 1 ) );
                objectListing = s3Client.listObjects(listObjectsRequest);
                addKeys( keys, objectListing.getObjectSummaries() );
            }

            checkKeyListing( "ListObjects", keys, pages, bucketName );
            keys.clear();
            pages = 1;

            ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
            listObjectsV2Request.setBucketName(bucketName);
            listObjectsV2Request.setMaxKeys(1);
            ListObjectsV2Result listing = s3Client.listObjectsV2(listObjectsV2Request);
            addKeys( keys, listing.getObjectSummaries() );
            while (listing.isTruncated()) {
                ++pages;
                listObjectsV2Request.setContinuationToken(listing.getNextContinuationToken());
                listing = s3Client.listObjectsV2(listObjectsV2Request);
                addKeys( keys, listing.getObjectSummaries() );
            }

            checkKeyListing( "ListObjectsV2", keys, pages, bucketName );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    /**
     * @param keys
     * @param objectSummaries
     */
    private static void addKeys(List<String> keys, List<S3ObjectSummary> objectSummaries) {
        for (S3ObjectSummary objectSummary : objectSummaries) {
            keys.add(objectSummary.getKey());
        }
    }

    /**
     * @param operation
     * @param keys
     * @param pages
     * @param bucketName
     */
    private static void checkKeyListing(String operation, List<String> keys, int pages, String bucketName) {
        System.out.println(operation + " found " + keys.size() + " objects on " + pages + " pages in " + bucketName);
        for (String key : keys) {
            System.out.println(key);
        }
        System.out.println();
    }

}
