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
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;

public class _99_DeleteBucket {

	public static void main(String[] args) throws Exception {
    	// create the AWS S3 Client
		AmazonS3 s3 = AWSS3Factory.getS3Client();

        // delete the demo bucket and all its content
        if (BucketVersioningConfiguration.OFF.equals(s3.getBucketVersioningConfiguration(AWSS3Factory.S3_BUCKET).getStatus())) {
            // no versioning, so list objects
            for (S3ObjectSummary summary : s3.listObjects(AWSS3Factory.S3_BUCKET).getObjectSummaries()) {
                s3.deleteObject(AWSS3Factory.S3_BUCKET, summary.getKey());
                System.out.println(String.format("Deleted [%s/%s]", AWSS3Factory.S3_BUCKET, summary.getKey()));
            }
        } else {
            // versioning was enabled, list versions
            for (S3VersionSummary summary : s3.listVersions(AWSS3Factory.S3_BUCKET, null).getVersionSummaries()) {
                s3.deleteVersion(AWSS3Factory.S3_BUCKET, summary.getKey(), summary.getVersionId());
                System.out.println(String.format("Deleted [%s/%s] (vId: %s)", AWSS3Factory.S3_BUCKET, summary.getKey(), summary.getVersionId()));
            }
        }

		s3.deleteBucket(AWSS3Factory.S3_BUCKET);

        // print bucket key/value and content for validation
        System.out.println(String.format("deleted bucket [%s]", AWSS3Factory.S3_BUCKET));
    }
}
