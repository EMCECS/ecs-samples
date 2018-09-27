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

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import java.util.Iterator;
import java.util.List;

//https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketGET.html
//https://docs.aws.amazon.com/AmazonS3/latest/API/v2-RESTBucketGET.html
//http://docs.aws.amazon.com/AmazonS3/latest/dev/ListingObjectKeysUsingJava.html
public class _09_ListObjects {

    public static void main(String[] args) throws Exception {
        // create the AWS S3 Client
        AmazonS3 s3 = AWSS3Factory.getS3ClientWithV2Signatures();
        _09_ListObjects lo = new _09_ListObjects();
        //lo.listObjectsV2(s3);
        lo.listObjectsV1(s3);

    }//main


    //http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/ListObjectsRequest.html
    public void listObjectsV1(AmazonS3 s3) {
        try {
            System.out.println("Listing objects");
            final ListObjectsRequest req = new ListObjectsRequest().withBucketName(AWSS3Factory.S3_BUCKET).withMaxKeys(5);
            //req.withPrefix("testXDelim/fooXbar/");
            //req.withPrefix("testXDelim/");
            //req.withPrefix("iman");
            //req.withDelimiter("/");
            //req.withMaxKeys(3);
            //req.withPrefix("vz/");
            //req.withDelimiter("/");

            ObjectListing result;
            result = s3.listObjects(req);
            do {
                for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                    System.out.println(" - " + objectSummary.getKey() + "  " +
                            "(size = " + objectSummary.getSize() + ")");
                }

                List<String> commonPrefixes = result.getCommonPrefixes();
                Iterator<String> commPrefixesIter = commonPrefixes.iterator();
                String commPrefix;
                System.out.println("Common Prefixes: ");
                while (commPrefixesIter.hasNext()) {
                    commPrefix = commPrefixesIter.next();
                    System.out.println(" - " + commPrefix);
                }
                System.out.println("Next marker is : " + result.getNextMarker());
                result = s3.listNextBatchOfObjects(result);
            } while(result.isTruncated() == true );

            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                System.out.println(" - " + objectSummary.getKey() + "  " +
                        "(size = " + objectSummary.getSize() + ")");
            }


        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, " +
                    "which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, " +
                    "which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }//listObjectsV1

    public void listObjectsV2(AmazonS3 s3) {
        try {
            System.out.println("Listing objects");
            final ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(AWSS3Factory.S3_BUCKET).withMaxKeys(2);
            ListObjectsV2Result result;
            do {
                result = s3.listObjectsV2(req);

                for (S3ObjectSummary objectSummary :
                        result.getObjectSummaries()) {
                    System.out.println(" - " + objectSummary.getKey() + "  " +
                            "(size = " + objectSummary.getSize() +
                            ")");
                }
                System.out.println("Next Continuation Token : " + result.getNextContinuationToken());
                req.setContinuationToken(result.getNextContinuationToken());
            } while(result.isTruncated() == true );

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, " +
                    "which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, " +
                    "which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }//listObjectsV2


    public void listObjectsV2Alt(AmazonS3 s3) {
        try {
            System.out.println("Listing objects");
            final ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(AWSS3Factory.S3_BUCKET).withMaxKeys(2);
            ListObjectsV2Result result;
            do {
                result = s3.listObjectsV2(req);

                for (S3ObjectSummary objectSummary :
                        result.getObjectSummaries()) {
                    System.out.println(" - " + objectSummary.getKey() + "  " +
                            "(size = " + objectSummary.getSize() + ")");
                }
                System.out.println("Next Continuation Token : " + result.getNextContinuationToken());
                String contToken = result.getNextContinuationToken();

                //req.setContinuationToken(result.getNextContinuationToken());
            } while(result.isTruncated() == true );

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, " +
                    "which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, " +
                    "which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }//listObjectsV2Alt
}
