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
import com.emc.object.s3.bean.ListObjectsResult;
import com.emc.object.s3.bean.S3Object;
import com.emc.object.s3.request.ListObjectsRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This sample creates a hierarchy of objects and then allows the user to issue list bucket calls to examine
 * the hierarchy.
 */
public class _11_ListObjects {

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

    public static void main(String[] args) throws Exception {
        // create the ECS S3 Client
        S3Client s3 = ECSS3Factory.getS3Client();
        for(String key : KEY_LIST) {
            s3.putObject(ECSS3Factory.S3_BUCKET, key, key, "text/plain");
        }

        while(true) {
            System.out.println( "Enter the prefix (empty for none):" );
            String prefix = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
            System.out.println( "Enter the delimiter (e.g. /, empty for none)" );
            String delimiter = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
            System.out.println( "Enter the marker (empty for none)" );
            String marker = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
            System.out.println( "Enter the max keys (empty for default)" );
            String maxKeys = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

            ListObjectsRequest lor = new ListObjectsRequest(ECSS3Factory.S3_BUCKET);
            if(!prefix.isEmpty()) {
                lor.setPrefix(prefix);
            }
            if(!delimiter.isEmpty()) {
                lor.setDelimiter(delimiter);
            }
            if(!marker.isEmpty()) {
                lor.setMarker(marker);
            }
            if(!maxKeys.isEmpty()) {
                lor.setMaxKeys(new Integer(maxKeys));
            }

            ListObjectsResult res = s3.listObjects(lor);
            System.out.println("-----------------");
            System.out.println("Bucket: " + res.getBucketName());
            System.out.println("Prefix: " + res.getPrefix());
            System.out.println("Delimiter: " + res.getDelimiter());
            System.out.println("Marker: " + res.getMarker());
            System.out.println("IsTruncated? " + res.isTruncated());
            System.out.println("NextMarker: " + res.getNextMarker());
            System.out.println();
            if(res.getCommonPrefixes() != null) {
                for(String s : res.getCommonPrefixes()) {
                    System.out.println("CommonPrefix: " + s);
                }
            }
            System.out.printf("%30s %10s %s\n", "LastModified", "Size", "Key");
            System.out.println("------------------------------ ---------- ------------------------------------------");
            for(S3Object obj : res.getObjects()) {
                System.out.printf("%30s %10d %s\n", obj.getLastModified().toString(), obj.getSize(), obj.getKey());
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


    }
}