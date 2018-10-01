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

import java.io.File;

public class _09_CreateStaticWebsites {
    private static final String[] STATIC_FILES = {
            "main.html",
            "pages/page1.html",
            "pages/page2.html",
            "css/main.css",
            "js/main.js"
    };

    public static void main(String[] args) throws Exception {
        uploadStaticWebsite(AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET);
        uploadStaticWebsite(AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET_2);
    }

    /**
     * @param s3Client
     * @param bucketName
     */
    private static void uploadStaticWebsite(AmazonS3 s3Client, String bucketName) {
        try {
            // upload static content
            for (String key : STATIC_FILES) {
    
                // load resource as file
                File file = new File(_09_CreateStaticWebsites.class.getResource("/" + key).toURI());
    
                // upload to bucket in the same path
                s3Client.putObject(bucketName, key, file);
            }
    
            // set bucket policy for public read
            s3Client.setBucketPolicy(bucketName,
                    "{" +
                            "\"Version\":\"2012-10-17\"," +
                            "\"Id\":\"StaticWebsitePolicy" + bucketName + "\"," +
                            "\"Statement\":[{" +
                            "    \"Sid\":\"PublicReadGetObject\"," +
                            "    \"Effect\":\"Allow\"," +
                            "    \"Principal\": \"*\"," +
                            "    \"Action\":[" +
                            "        \"s3:GetObject\"" +
                            "    ]," +
                            "    \"Resource\":[" +
                            "        \"" + bucketName + "/*\"" +
                            "    ]" +
                            "}]" +
                            "}");
    
            // here is the link for the landing page
            // i.e. https://[namespace].baseURL/[bucket]/[main-html]
            String landingPage = AWSS3Factory.PUBLIC_ENDPOINT + "/" + bucketName + "/main.html";
    
            System.out.println("The URL for your website is:\n" + landingPage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }
}
