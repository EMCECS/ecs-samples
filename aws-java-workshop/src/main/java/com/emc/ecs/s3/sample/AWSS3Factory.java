/*
 * Copyright 2013-2018 EMC Corporation. All Rights Reserved.
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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.commons.codec.binary.Base64;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

/**
 * Factory class to create the AWS S3 client.  The client will be used in the examples for the
 * AWS S3 interface.
 */
public class AWSS3Factory {
    /*
     * The end point of the S3 REST interface - this should take the form of
     * http://ecs-address:9020 or https://ecs-address:9021
     * or
     * https://object.ecstestdrive.com
     */
    static final String S3_ENDPOINT = "https://object.ecstestdrive.com";

    // the S3 access key id - this is equivalent to the user
    static final String S3_ACCESS_KEY_ID = "131123009294999359@ecstestdrive.emc.com";

    // the S3 secret key associated with the S3_ACCESS_KEY_ID
    static final String S3_SECRET_KEY = "Ej816BbmYcy6SsBANKbSw1knSS8406aSKOsiV7+F";

    // a unique bucket name to store objects
    public static final String S3_BUCKET = "workshop-bucket";

    // a unique bucket name to store objects
    public static final String S3_BUCKET_V4 = "workshop-bucket-v4";

    public static final String S3_VERSIONBUCKET = "versioned-bucket";

    // this should be a namespace-enabled baseURL w/ wildcard DNS & SSL
    public static final String PUBLIC_ENDPOINT = "https://<namespace>.public.ecstestdrive.com";

    private static AmazonS3ClientBuilder getBasicS3ClientBuilder() {

        AmazonS3ClientBuilder builder = AmazonS3Client.builder();

        // set endpoint
        builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(S3_ENDPOINT, "us-east-1"));

        // set credentials
        builder.setCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(S3_ACCESS_KEY_ID, S3_SECRET_KEY)));

        // path-style bucket naming is highly recommended
        builder.setPathStyleAccessEnabled(true);

        return builder;

    }

    public static AmazonS3 getS3ClientWithV4Signatures() {
        return getBasicS3ClientBuilder().build();
    }

    public static AmazonS3 getS3ClientWithV2Signatures() {

        AmazonS3ClientBuilder builder = getBasicS3ClientBuilder();

        // switch to v2 auth
        builder.setClientConfiguration(new ClientConfiguration().withSignerOverride("S3SignerType"));

        return builder.build();

    }

    // Generates a RSA key pair for testing.
    public static void main(String[] args) {
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(1024, new SecureRandom());
            KeyPair myKeyPair = keyGenerator.generateKeyPair();

            // Serialize.
            byte[] pubKeyBytes = myKeyPair.getPublic().getEncoded();
            byte[] privKeyBytes = myKeyPair.getPrivate().getEncoded();

            String pubKeyStr = new String(Base64.encodeBase64(pubKeyBytes, false), "US-ASCII");
            String privKeyStr = new String(Base64.encodeBase64(privKeyBytes, false), "US-ASCII");

            System.out.println("Public Key: " + pubKeyStr);
            System.out.println("Private Key: " + privKeyStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
