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
import com.amazonaws.services.s3.model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class _08_CreateLargeObject {

	public static void main(String[] args) throws Exception {
    	// create the AWS S3 Client
		AmazonS3 s3 = AWSS3Factory.getS3Client();

    	// retrieve object key/value from user
        System.out.println( "Enter the object key:" );
        String key = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        System.out.println( "Enter the file location (C:\\Users\\vandrk\\EMC\\NameSpaceList.zip) :" );
        String filePath = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        
        // part size for chunking in multi-parts
    	long partSize = 128 * 1024 * 1024; // Set part size to 5 MB.

    	// list of UploadPartResponse objects for each part that is uploaded
    	List<PartETag> partETags = new ArrayList<PartETag>();
    	
    	// Step 1: Initialize.
    	InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(
    	                                                    AWSS3Factory.S3_BUCKET, key);
    	InitiateMultipartUploadResult initResponse = 
    	                              s3.initiateMultipartUpload(initRequest);

    	// get the file and file length
    	File file = new File(filePath);
    	long contentLength = file.length();
    	
    	System.out.println( String.format("starting mulit-part upload for object [%s/%s] with file path [%s] and size [%d] in [%d] size chunks ",
    			AWSS3Factory.S3_BUCKET, key, filePath, contentLength, partSize / 1024 / 1024));

    	try {
    	    // Step 2: Upload parts.
    	    long filePosition = 0;
    	    for (int i = 1; filePosition < contentLength; i++) {
    	        // get the size of the chunk.  Note - the last part can be less than the chunk size
    	    	partSize = Math.min(partSize, (contentLength - filePosition));
    	    	
    	    	System.out.println( String.format("Sending chunk [%d] starting at position [%d]", i, filePosition));
    	    	
    	        // Create request to upload a part.
    	        UploadPartRequest uploadRequest = new UploadPartRequest()
    	            .withBucketName(AWSS3Factory.S3_BUCKET).withKey(key)
    	            .withUploadId(initResponse.getUploadId()).withPartNumber(i)
    	            .withFileOffset(filePosition)
    	            .withFile(file)
    	            .withPartSize(partSize);

    	        // Upload part and add response to our list.
    	        PartETag eTagPart = s3.uploadPart(uploadRequest).getPartETag();
    	        partETags.add(eTagPart);

    	        // set file position to the next part in the file
    	        filePosition += partSize;
    	    }

    	    // Step 3: Complete.
    	    System.out.println("Waiting for completion of multi-part upload");
    	    CompleteMultipartUploadRequest compRequest = new 
    	                CompleteMultipartUploadRequest(AWSS3Factory.S3_BUCKET,
    	                                               key, 
    	                                               initResponse.getUploadId(), 
    	                                               partETags);

    	    s3.completeMultipartUpload(compRequest);
    	} catch (Exception e) {
    	    s3.abortMultipartUpload(new AbortMultipartUploadRequest(
    	    		AWSS3Factory.S3_BUCKET, key, initResponse.getUploadId()));
    	}

        // print bucket key/value and content for validation
    	System.out.println( String.format("completed mulit-part upload for object [%s/%s] with file path: [%s]",
    			AWSS3Factory.S3_BUCKET, key, filePath));
    }
}
