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
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.*;

public class _08_CreateLargeObject {
	protected final AmazonS3 client = AWSS3Factory.getS3Client();

	public static void main(String[] args) throws Exception {
    	// create the AWS S3 Client
		AmazonS3 s3 = AWSS3Factory.getS3Client();

    	// retrieve object key/value from user
        System.out.println( "Enter the object key:" );
        String key = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        // /Users/conerj/Downloads/wn3.1.dict.tar.gz
		// /Users/conerj/Downloads/apidocs_22.zip
        System.out.println( "Enter the file location (C:\\Users\\vandrk\\EMC\\NameSpaceList.zip) :" );
        String filePath = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

	_08_CreateLargeObject clo = new _08_CreateLargeObject();
	clo.mpuSingleThreaded(key, filePath);
	//clo.testMultiThreadMultipartUploadListPartsPagination(key, filePath);
    }

    public void mpuSingleThreaded(String key, String filePath) {
		AmazonS3 s3 = AWSS3Factory.getS3Client();

		// part size for chunking in multi-parts
		long partSize = 128 * 1024 * 1024; // Set part size to 2 MB.

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
				System.out.println("doing upload part");
				UploadPartResult upr = s3.uploadPart(uploadRequest);

				System.out.println("getting pn");
				int pn = upr.getPartNumber();
				System.out.println("pn = " + Integer.toString(pn));

				System.out.println("getting etag");
				PartETag eTagPart = upr.getPartETag();
				//PartETag eTagPart = s3.uploadPart(uploadRequest).getPartETag();
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
			System.out.println("Something went wrong. Need to abort");
			s3.abortMultipartUpload(new AbortMultipartUploadRequest(
					AWSS3Factory.S3_BUCKET, key, initResponse.getUploadId()));
		}

		// print bucket key/value and content for validation
		System.out.println( String.format("completed mulit-part upload for object [%s/%s] with file path: [%s]",
				AWSS3Factory.S3_BUCKET, key, filePath));
	}

	public void testMultiThreadMultipartUploadListPartsPagination(String key, String filePath) throws Exception {
		//client needs to be final to use it in the executor threads
		//AmazonS3 client = AWSS3Factory.getS3Client();
		String theBucket = AWSS3Factory.S3_BUCKET;


		// get the file and file length
		File file = new File(filePath);
		long contentLength = file.length();


		//String key = "mpuListPartsTest";
		//File file = createRandomTempFile(10 * 1024 * 1024 + 333); // 10MB+ (not a power of 2)
		int partSize = 128 * 1024 * 1024; // 2MB parts

		//String uploadId = client.initiateMultipartUpload(theBucket, key);
		// Step 1: Initialize.
		InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(
				theBucket, key);
		InitiateMultipartUploadResult initResponse =
				client.initiateMultipartUpload(initRequest);



		try {
			List<Future<PartETag>> futures = new ArrayList<Future<PartETag>>();
			ExecutorService executor = Executors.newFixedThreadPool(8);

			System.out.println("Going to upload each part...");
			int partNumber = 1;
			long offset = 0, length = partSize;
			while (offset < file.length()) {
				if (offset + length > file.length()) length = file.length() - offset;

				System.out.println("creating UploadPartRequest");
				final UploadPartRequest partRequest = new UploadPartRequest()
						.withBucketName(theBucket).withKey(key)
						.withUploadId(initResponse.getUploadId()).withPartNumber(partNumber++)
						.withFileOffset(offset)
						.withFile(file)
						.withPartSize(length);
				//final UploadPartRequest partRequest = new UploadPartRequest(theBucket, key, initResponse.getUploadId(), partNumber++);
				//partRequest.withFile(file).withOffset(offset).withLength(length);

				futures.add(executor.submit(new Callable<PartETag>() {
					@Override
					public PartETag call() throws Exception {
						System.out.println("executing part upload");
						UploadPartResult upr = client.uploadPart(partRequest);
						return upr.getPartETag();
					}
				}));
				offset += length;
			}

			// shutdown thread pool
			executor.shutdown();

			/*
			while (executor.awaitTermination(1, TimeUnit.MINUTES)) {
				System.out.println("awaiting termination");
			}
			*/

			System.out.println("executer has terminated");
			// wait for threads to finish and gather parts (future.get() will throw an exception if one occurred during execution)

			Collection<PartETag> parts = new TreeSet<>(new Comparator<PartETag>() {
				@Override
				public int compare(PartETag o1, PartETag o2) {
					return o1.getPartNumber() - o2.getPartNumber();
				}
			});
			//SortedSet<PartETag> parts = new TreeSet<PartETag>();

			System.out.println("Going to assemble PartETags");
			for (Future<PartETag> future : futures) {
				System.out.println("Assembling the PartETags from the futures.." + future.get().getETag());
				parts.add(future.get());
			}
			System.out.println("PartETags have been assembled");

			//some verification
			//////////////////////////////////////
			ListPartsRequest listPartsRequest = new ListPartsRequest(theBucket, key, initResponse.getUploadId());
			PartListing listPartsResult = null;
			List<PartETag> allParts = new ArrayList<PartETag>();

			//if (listPartsResult != null) listPartsRequest.setMarker(listPartsResult.getNextPartNumberMarker());
			listPartsResult = client.listParts(listPartsRequest);
			List<PartSummary> partSummaries = listPartsResult.getParts();
			if (parts.size() != partSummaries.size()) {
				System.out.println("ruh roh");
				throw (new Exception("number of parts didn't match up"));
			}
			else {
				System.out.println("Number of parts looks good");
			}
			//////////////////////////////////////

			//just for demo printout purposes
			//////////////////////////////////////
			System.out.println("going to pring the part numbers....");
			ArrayList<PartETag> tmpPe = new ArrayList<PartETag>(parts);
			for (PartETag pe : tmpPe) {
				System.out.println("partNumber: " + pe.getPartNumber());
				System.out.println("part eTag: " + pe.getETag());
			}
			//////////////////////////////////////

			// complete MP upload
			// Step 3: Complete.
			System.out.println("Waiting for completion of multi-part upload");
			List<PartETag> lpe = new ArrayList<>(parts);

			CompleteMultipartUploadRequest compRequest = new
					CompleteMultipartUploadRequest(theBucket,
					key,
					initResponse.getUploadId(),
					lpe);
			client.completeMultipartUpload(compRequest);
			//client.completeMultipartUpload(new CompleteMultipartUploadRequest(theBucket, key, uploadId).withParts(parts));
		} catch (Exception e) {
			System.out.println("Something went wrong. Need to abort");
			e.getMessage();
			client.abortMultipartUpload(new AbortMultipartUploadRequest(
					AWSS3Factory.S3_BUCKET, key, initResponse.getUploadId()));
			//client.abortMultipartUpload(new AbortMultipartUploadRequest(theBucket, key, uploadId));
		}
	}
}
