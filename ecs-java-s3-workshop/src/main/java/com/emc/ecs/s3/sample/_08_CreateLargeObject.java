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
package com.emc.ecs.s3.sample;

import com.emc.object.s3.LargeFileUploader;
import com.emc.object.s3.S3Client;
import com.emc.object.s3.bean.InitiateMultipartUploadResult;
import com.emc.object.s3.bean.MultipartPartETag;
import com.emc.object.s3.request.AbortMultipartUploadRequest;
import com.emc.object.s3.request.CompleteMultipartUploadRequest;
import com.emc.object.s3.request.InitiateMultipartUploadRequest;
import com.emc.object.s3.request.UploadPartRequest;
import com.emc.object.util.InputStreamSegment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.SortedSet;
import java.util.TreeSet;

public class _08_CreateLargeObject {

	public static void main(String[] args) throws Exception {
        theEasyWay();
    }

    public static void theHardWay() throws Exception {

    	// create the ECS S3 Client
    	S3Client s3 = ECSS3Factory.getS3Client();

    	// retrieve object key/value from user
        System.out.println( "Enter the object key:" );
        String key = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        System.out.println("Enter the file location:");
        String filePath = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        
        // part size for chunking in multi-parts
    	long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.

        // list of MultipartPartETag objects for each part that is uploaded
        SortedSet<MultipartPartETag> eTags = new TreeSet<>();

        // Step 1: Initialize.
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(ECSS3Factory.S3_BUCKET, key);
        InitiateMultipartUploadResult initResponse = s3.initiateMultipartUpload(initRequest);

    	// get the file and file length
    	File file = new File(filePath);
    	long contentLength = file.length();
    	
    	System.out.println( String.format("starting mulit-part upload for object [%s/%s] with file path [%s] and size [%d] in [%d] MB size chunks ",
    			ECSS3Factory.S3_BUCKET, key, filePath, contentLength, partSize / 1024 / 1024));

        // NOTE: this isn't even threaded (you would have to add that)
        try (FileInputStream fis = new FileInputStream(file)) {

            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // get the size of the chunk.  Note - the last part can be less than the chunk size
                partSize = Math.min(partSize, (contentLength - filePosition));

                System.out.println(String.format("Sending chunk [%d] starting at position [%d]", i, filePosition));

                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest(ECSS3Factory.S3_BUCKET,
                        key, initResponse.getUploadId(), i, new InputStreamSegment(fis, filePosition, partSize));

                // Upload part and add response to our list.
                MultipartPartETag eTag = s3.uploadPart(uploadRequest);
                eTags.add(eTag);

                // set file position to the next part in the file
                filePosition += partSize;
            }

            // Step 3: Complete.
            System.out.println("Waiting for completion of multi-part upload");
            CompleteMultipartUploadRequest compRequest = new
                    CompleteMultipartUploadRequest(ECSS3Factory.S3_BUCKET, key, initResponse.getUploadId()).withParts(eTags);

            s3.completeMultipartUpload(compRequest);

            // print bucket key/value and content for validation
            System.out.println(String.format("completed mulit-part upload for object [%s/%s] with file path: [%s]",
                    ECSS3Factory.S3_BUCKET, key, filePath));
        } catch (Exception e) {
            e.printStackTrace();
            s3.abortMultipartUpload(new AbortMultipartUploadRequest(
                    ECSS3Factory.S3_BUCKET, key, initResponse.getUploadId()));
        }
    }

    public static void theEasyWay() throws Exception {
        // create the ECS S3 Client
        S3Client s3 = ECSS3Factory.getS3Client();

        // retrieve object key/value from user
        System.out.println("Enter the object key:");
        String key = new BufferedReader(new InputStreamReader(System.in)).readLine();
        System.out.println("Enter the file location (C:\\Users\\vandrk\\EMC\\NameSpaceList.zip) :");
        String filePath = new BufferedReader(new InputStreamReader(System.in)).readLine();

        LargeFileUploader uploader = new LargeFileUploader(s3, ECSS3Factory.S3_BUCKET, key, new File(filePath));

        // default part size, thread count, etc. can be overridden
//        uploader.setPartSize(4 * 1024 * 1024);
//        uploader.setThreads(8);
//        uploader.setObjectMetadata(s3ObjectMetadata);
//        uploader.setAcl(acl);

        uploader.doMultipartUpload();

        // or use parallel byte-range updates (EMC proprietary S3 extension)
        //uploader.doByteRangeUpload();

        // done!
    }
}
