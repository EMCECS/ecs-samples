/*
 * Copyright 2015 EMC Corporation. All Rights Reserved.
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
import com.emc.object.s3.S3ObjectMetadata;
import com.emc.object.s3.bean.*;
import com.emc.object.s3.request.CreateBucketRequest;
import com.emc.object.s3.request.ListObjectsRequest;
import com.emc.object.s3.request.PutObjectRequest;
import com.emc.object.s3.request.QueryObjectsRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * This sample creates a hierarchy of objects and then allows the user to issue query bucket calls to examine
 * the objects.
 */
public class _12_SearchObjects {

    public static final String[] KEY_LIST = new String[] {
            "20151102/account-12345/bill.xml",
            "20151102/account-12345/bill.pdf",
            "20151102/account-12345/bill.html",
            "20151102/account-55555/bill.xml",
            "20151102/account-55555/bill.pdf",
            "20151102/account-55555/bill.html",
            "20151102/account-77777/bill.xml",
            "20151102/account-77777/bill.pdf",
            "20151102/account-77777/bill.html",
            "20151103/account-11111/bill.xml",
            "20151103/account-11111/bill.pdf",
            "20151103/account-11111/bill.html",
            "20151103/account-11122/bill.xml",
            "20151103/account-11122/bill.pdf",
            "20151103/account-11122/bill.html",
            "20151103/account-11133/bill.xml",
            "20151103/account-11133/bill.pdf",
            "20151103/account-11133/bill.html",
            "20141103/account-11111/bill.xml",
            "20141103/account-11111/bill.pdf",
            "20141103/account-11111/bill.html",
            "20141103/account-11122/bill.xml",
            "20141103/account-11122/bill.pdf",
            "20141103/account-11122/bill.html",
            "20141103/account-11133/bill.xml",
            "20141103/account-11133/bill.pdf",
            "20141103/account-11133/bill.html" };

    public static final String FIELD_ACCOUNT_ID = "account-id";
    public static final String FIELD_BILLING_DATE = "billing-date";
    public static final String FIELD_BILL_TYPE = "bill-type";
    public static final String BUCKET_NAME = "search-test";
    public static final String USER_PREFIX = "x-amz-meta-";

    public static void main(String[] args) throws Exception {
        // create the ECS S3 Client
        S3Client s3 = ECSS3Factory.getS3Client();

        // Create the bucket with indexed keys
        MetadataSearchKey accountKey = new MetadataSearchKey(USER_PREFIX+FIELD_ACCOUNT_ID, MetadataSearchDatatype.integer);
        MetadataSearchKey dateKey = new MetadataSearchKey(USER_PREFIX+FIELD_BILLING_DATE, MetadataSearchDatatype.datetime);
        MetadataSearchKey typeKey = new MetadataSearchKey(USER_PREFIX+FIELD_BILL_TYPE, MetadataSearchDatatype.string);
        CreateBucketRequest cbr = new CreateBucketRequest(BUCKET_NAME);
        cbr.withMetadataSearchKeys(Arrays.asList(accountKey, dateKey, typeKey));
        s3.createBucket(cbr);

        for(String key : KEY_LIST) {
            // Extract metadata from object name and apply as indexed metadata.
            S3ObjectMetadata om = new S3ObjectMetadata();
            om.setContentType("text/plain");
            om.addUserMetadata(FIELD_ACCOUNT_ID, extractAccountId(key));
            om.addUserMetadata(FIELD_BILLING_DATE, extractBillDate(key));
            om.addUserMetadata(FIELD_BILL_TYPE, extractBillType(key));
            PutObjectRequest por = new PutObjectRequest(BUCKET_NAME, key, key).withObjectMetadata(om);
            s3.putObject(por);
        }

        while(true) {
            System.out.println( "Enter the account id (empty for none):" );
            String accountId = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
            System.out.println( "Enter the billing date (e.g. 2016-09-22, empty for none)" );
            String billingDate = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
            System.out.println( "Enter the bill type (e.g. xml.  empty for none)" );
            String billType = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

            QueryObjectsRequest qor = new QueryObjectsRequest(BUCKET_NAME);
            StringBuilder query = new StringBuilder();
            //query.append("(");
            if(!accountId.isEmpty()) {
                query.append(USER_PREFIX + FIELD_ACCOUNT_ID + "==" +  accountId + "");
            }
            if(!billingDate.isEmpty()) {
                if(query.length() > 0) {
                    query.append(" and ");
                }
                query.append(USER_PREFIX + FIELD_BILLING_DATE + "==" +  billingDate + "T00:00:00Z");

            }
            if(!billType.isEmpty()) {
                if(query.length() > 0) {
                    query.append(" and ");
                }
                query.append(USER_PREFIX + FIELD_BILL_TYPE + "=='" +  billType + "'");
            }
            qor.setQuery(query.toString());

            QueryObjectsResult res = s3.queryObjects(qor);
            System.out.println("-----------------");
            System.out.println("Bucket: " + res.getBucketName());
            System.out.println("Query: " + res.getQuery());
            System.out.println();

            System.out.println("Key");
            System.out.println("------------------------------------------");
            for(QueryObject obj : res.getObjects()) {
                System.out.printf("%s\n", obj.getObjectName());
            }

            System.out.println( "Another? (Y/N) " );
            String another = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

            if(!another.toUpperCase().equals("Y")) {
                break;
            }
        }

        // Cleanup
        for(String key : KEY_LIST) {
            s3.deleteObject(ECSS3Factory.S3_BUCKET, key);
        }
        s3.deleteBucket(BUCKET_NAME);


    }

    private static String extractAccountId(String key) {
        return key.split("/")[1].split("-")[1];
    }

    private static String extractBillDate(String key) {
        String date = key.split("/")[0];
        // Make it a ISO-8601 TS
        return String.format("%s-%s-%sT00:00:00Z", date.substring(0,4), date.substring(4,6), date.substring(6));
    }

    private static String extractBillType(String key) {
        return key.substring(key.lastIndexOf(".")+1);
    }
}