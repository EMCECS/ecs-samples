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

import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

public class _24_ListObjectsByPrefix extends BucketAndObjectValidator {

    public static void main(String[] args) throws Exception {
        listObjectsByPrefix(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET);
        listObjectsByPrefix(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2);
    }

    /**
     * @param s3Client
     * @param bucketName
     */
    private static void listObjectsByPrefix(AmazonS3 s3Client, String bucketName) {
        try {
            String prefix = "pages/";

            ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
            listObjectsRequest.setBucketName(bucketName);
            listObjectsRequest.setPrefix(prefix);
            ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);

            checkPrefixListing("ListObjects", bucketName, prefix, objectListing.getObjectSummaries(), objectListing.getCommonPrefixes() );
    
            ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
            listObjectsV2Request.setBucketName(bucketName);
            listObjectsV2Request.setPrefix(prefix);
            ListObjectsV2Result objectListingV2 = s3Client.listObjectsV2(listObjectsV2Request);

            checkPrefixListing("ListObjectsV2", bucketName, prefix, objectListingV2.getObjectSummaries(), objectListingV2.getCommonPrefixes() );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    /**
     * @param operation
     * @param bucketName
     * @param prefix
     * @param objectSummaries
     * @param prefixes 
     */
    private static void checkPrefixListing(String operation, String bucketName, String prefix,
            List<S3ObjectSummary> objectSummaries, List<String> prefixes ) {
        System.out.println( operation + " found " + objectSummaries.size() + " objects and " + prefixes.size() + " prefixes in " + bucketName + " with prefix " + prefix);
        for (S3ObjectSummary objectSummary : objectSummaries) {
            System.out.println(objectSummary.getKey());
        }
        for (String commonPrefix : prefixes) {
            System.out.println(commonPrefix);
        }
        System.out.println();
    }

}
