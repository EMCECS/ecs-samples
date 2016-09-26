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

import com.emc.object.s3.S3Client;

import java.io.IOException;

public class _10_AtomicAppend {

	public static void main(String[] args) throws Exception {
    	// create the ECS S3 Client
    	S3Client s3 = ECSS3Factory.getS3Client();

    	// object key to create, update, and append
    	String key = "atomic-append.txt";
    	String bucketName = ECSS3Factory.S3_BUCKET;	// bucket to create object in
    	String content = "Hello World!";				// initial object content

        // first create an initial object
        System.out.println(String.format("creating initial object %s/%s", bucketName, key));
        s3.putObject(bucketName, key, content, "text/plain");
        
        // read object and print content
        System.out.println(String.format("initial object %s/%s with content: [%s]",
                bucketName, key, readObject(s3, bucketName, key)));
        
        // append to the end of the object
        String content2 = " ... and Universe!!";

		// the offset at which our appended data was written is returned
		// (this is the previous size of the object)
        long appendOffset = s3.appendObject(bucketName, key, content2);
		System.out.println(String.format("Append successful at offset %d", appendOffset));

        // read object and print content
        System.out.println(String.format("final object %s/%s with content: [%s]",
                bucketName, key, readObject(s3, bucketName, key)));
    }

	private static String readObject(S3Client s3, String bucketName, String key) throws IOException
	{
		// read the object from the demo bucket
		return s3.readObject(bucketName, key, String.class);
	}
}
