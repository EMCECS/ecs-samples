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

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class _99_DeleteBucket {

	public static void main(String[] args) throws Exception {
    	// create the AWS S3 Client
		AmazonS3Client s3 = AWSS3Factory.getS3Client();

        // delete the demo bucket and all its content
		for (S3ObjectSummary summary : s3.listObjects(AWSS3Factory.S3_BUCKET).getObjectSummaries())
		{
			s3.deleteObject(AWSS3Factory.S3_BUCKET, summary.getKey());
			System.out.println(String.format("Deleted [%s/%s]", AWSS3Factory.S3_BUCKET, summary.getKey()));
		}
		s3.deleteBucket(AWSS3Factory.S3_BUCKET);

    	            // print bucket key/value and content for validation
    	System.out.println( String.format("deleted bucket [%s]",
    			AWSS3Factory.S3_BUCKET));
    }
}
