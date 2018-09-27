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
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringInputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

//you can't pass in an empty prefix
//you can pass an object name in full
public class _11_EnableVersioning {

    public static void main(String[] args) throws Exception {
        System.out.println( "Enter the bucket name:" );
        String bn = new BufferedReader( new InputStreamReader( System.in ) ).readLine();


        // create the AWS S3 Client
        AmazonS3 s3 = AWSS3Factory.getS3ClientWithV2Signatures();

        // create the bucket - used for subsequent demo operations
        s3.createBucket(AWSS3Factory.S3_VERSIONBUCKET );

        _11_EnableVersioning ev = new _11_EnableVersioning();
        ev.enableVersioning(s3, AWSS3Factory.S3_VERSIONBUCKET);
        String prefix = ev.createSampleObject(s3, AWSS3Factory.S3_VERSIONBUCKET);

        //String prefix = "versionPrefix/foo";
        //prefix = "";
        ev.listSampleObject(s3, AWSS3Factory.S3_VERSIONBUCKET, "versionPrefix/foo");
        //ev.listAndDeleteSampleObject(s3, AWSS3Factory.S3_VERSIONBUCKET);


    }

    public void enableVersioning(AmazonS3 client, String versionBucket) throws Exception {
        // 1. Enable versioning on the bucket.
        BucketVersioningConfiguration configuration =
                new BucketVersioningConfiguration().withStatus("Enabled");

        SetBucketVersioningConfigurationRequest setBucketVersioningConfigurationRequest =
                new SetBucketVersioningConfigurationRequest(versionBucket,configuration);

        client.setBucketVersioningConfiguration(setBucketVersioningConfigurationRequest);

        // 2. Get bucket versioning configuration information.
        BucketVersioningConfiguration conf = client.getBucketVersioningConfiguration(versionBucket);
        System.out.println("bucket versioning configuration status:    " + conf.getStatus());
    }

    public String createSampleObject(AmazonS3 client, String versionBucket) throws Exception {
        String prefix = "versionPrefix/";

        // create a few versions of the same key
        String key = prefix + "foo", content = "Hello Versions!";
        client.putObject(versionBucket, key, new StringInputStream(content), null);
        client.deleteObject(versionBucket, key);
        client.putObject(versionBucket, key, new StringInputStream(content), null);
        System.out.println("using prefix: " + prefix);
        return prefix;
    }

    public void listSampleObject(AmazonS3 client, String versionBucket, String prefix) throws Exception {
        //VersionListing result = client.listVersions(versionBucket, prefix);

        ListVersionsRequest lvr = new ListVersionsRequest();
        lvr.setBucketName(versionBucket);
        VersionListing result = client.listVersions(lvr);

        List<S3VersionSummary> versions = result.getVersionSummaries();
        for (S3VersionSummary vs : versions) {
            System.out.println("key: " + vs.getKey() + "\t id: " + vs.getVersionId());
            System.out.println("Is this a delete marker?... " + vs.isDeleteMarker());
        }
    }


    public void listAndDeleteSampleObject(AmazonS3 client, String versionBucket) throws Exception {
        //VersionListing result = client.listVersions(versionBucket, prefix);
        ListVersionsRequest lvr = new ListVersionsRequest();
        lvr.setBucketName(versionBucket);
        VersionListing result = client.listVersions(lvr);

        List<S3VersionSummary> versions = result.getVersionSummaries();
        for (S3VersionSummary vs : versions) {
            System.out.println("key: " + vs.getKey() + "\t id: " + vs.getVersionId());

            //AWS sample code is wrong!
            //http://docs.aws.amazon.com/AmazonS3/latest/dev/DeletingOneObjectUsingJava.html
            //client.deleteObject(new DeleteVersionRequest(versionBucket, vs.getKey(), vs.getVersionId()));
            client.deleteVersion(versionBucket, vs.getKey(), vs.getVersionId());
        }
    }




    public void testListAndReadVersions(AmazonS3 client, String versionBucket) throws Exception {
        String prefix = "versionPrefix/";
        // turn on versioning first

        // 1. Enable versioning on the bucket.
        BucketVersioningConfiguration configuration =
                new BucketVersioningConfiguration().withStatus("Enabled");

        SetBucketVersioningConfigurationRequest setBucketVersioningConfigurationRequest =
                new SetBucketVersioningConfigurationRequest(versionBucket,configuration);

        client.setBucketVersioningConfiguration(setBucketVersioningConfigurationRequest);

        // 2. Get bucket versioning configuration information.
        BucketVersioningConfiguration conf = client.getBucketVersioningConfiguration(versionBucket);
        System.out.println("bucket versioning configuration status:    " + conf.getStatus());


		/*
		client.setBucketVersioning(versionBucket,
				new VersioningConfiguration().withStatus(VersioningConfiguration.Status.Enabled));
*/

        // create a few versions of the same key
        String key = prefix + "foo", content = "Hello Versions!";
        client.putObject(versionBucket, key, new StringInputStream(content), null);
        client.deleteObject(versionBucket, key);
        client.putObject(versionBucket, key, new StringInputStream(content), null);

        VersionListing result = client.listVersions(versionBucket, prefix);

        List<S3VersionSummary> versions = result.getVersionSummaries();
        for (S3VersionSummary vs : versions) {
            System.out.println("key: " + vs.getKey() + "\t id: " + vs.getVersionId());
        }
    }
}
