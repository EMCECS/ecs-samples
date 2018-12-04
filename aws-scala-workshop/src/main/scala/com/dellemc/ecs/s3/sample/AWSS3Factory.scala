/*
 * Copyright 2018 Dell Inc. or its subsidiaries. All Rights Reserved.
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
package com.dellemc.ecs.s3.sample

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder

object AWSS3Factory {
    /*
     * The end point of the S3 REST interface - this should take the form of
     * http://ecs-address:9020 or https://ecs-address:9021
     * or
     * https://object.ecstestdrive.com
     */
    def S3_ENDPOINT = "https://object.ecstestdrive.com"

    // the S3 namespace for the user
    def S3_NAMESPACE = "131123009294999359"

    // the S3 access key id - this is equivalent to the user
    def S3_ACCESS_KEY_ID = "131123009294999359@ecstestdrive.emc.com"

    // the S3 access key id - this is equivalent to the user
    def S3_ACCESS_KEY_ID_2 = "131123009294999359@ecstestdrive.emc.com"

    // the S3 secret key associated with the S3_ACCESS_KEY_ID
    def S3_SECRET_KEY = ""

    // a unique bucket name to store objects
    def S3_BUCKET = "scala-workshop-bucket"

    // another unique bucket name to store objects
    def S3_BUCKET_2 = "scala-workshop-bucket-2"

    // a unique bucket name to store versioned objects
    def S3_VERSIONED_BUCKET = "scala-workshop-versioned-bucket"

    // a unique object name
    def S3_OBJECT = "workshop-object"

    // this should be a namespace-enabled baseURL w/ wildcard DNS & SSL
    def PUBLIC_ENDPOINT = "https://" + S3_NAMESPACE + ".public.ecstestdrive.com"
    //    public String PUBLIC_ENDPOINT = "https://<namespace>.public.ecstestdrive.com"

    /**
     * @return a basic builder that will use V4 signatures
     */
    def getBasicS3ClientBuilder(): AmazonS3ClientBuilder = {
        val builder: AmazonS3ClientBuilder = AmazonS3Client.builder()

        // set endpoint
        builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(S3_ENDPOINT, "us-east-1"))

        // set credentials
        builder.setCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(S3_ACCESS_KEY_ID, S3_SECRET_KEY)))

        // path-style bucket naming is highly recommended
        builder.setPathStyleAccessEnabled(true)

        return builder
    }

    /**
     * @return a basic client that will use V4 signatures
     */
    def getS3ClientWithV4Signatures(): AmazonS3 = {
        println("Running with V4 Signatures:\n")
        return getBasicS3ClientBuilder().build()
    }

    /**
     * @return a client that will use V2 signatures
     */
    def getS3ClientWithV2Signatures(): AmazonS3 = {

        val builder: AmazonS3ClientBuilder = getBasicS3ClientBuilder()

        // switch to v2 auth
        builder.setClientConfiguration(new ClientConfiguration().withSignerOverride("S3SignerType"))
        println("Running with V2 Signatures:\n")

        return builder.build()
    }

}