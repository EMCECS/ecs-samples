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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.S3ClientOptions;
import org.apache.commons.codec.binary.Base64;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

/**
 * Factory class to create the ViPR S3 client.  The client will be used in the examples for the
 * Java ViPR S3 interface.
 */
public class AWSS3Factory {

	/* the S3 access key id - this is equivalent to the user */
	public static final String S3_ACCESS_KEY_ID = "";
	
	/* the S3 secret key associated with the S3_ACCESS_KEY_ID */
    public static final String S3_SECRET_KEY = "";
	
    /*
     * The end point of the ViPR S3 REST interface - this should take the form of
     * http://ecs-address:9020 or https://ecs-address:9021
     * or
     * https://object.ecstestdrive.com
     * if you're using ECS Test Drive.  Note that you'll need to install the RSA root certificate in Java's
     * cacerts file if you're ECS Test Drive:
     * https://portal.ecstestdrive.com/Content/ECS%20Test%20Drive%20SSL%20issues%20with%20Java.docx
     * or run the InstallCert program in the tools directory:
     * java -jar installcert-usn-20140115.jar object.ecstestdrive.com:443
     */	
	public static final String S3_ENDPOINT = "";
	
	/* a unique bucket name to store objects */
    public static final String S3_BUCKET = "";
	

    public static AmazonS3 getS3Client() {
        BasicAWSCredentials creds = new BasicAWSCredentials(S3_ACCESS_KEY_ID, S3_SECRET_KEY);

        ClientConfiguration cc = new ClientConfiguration();
        //cc.setProxyHost("localhost");
        //cc.setProxyPort(8888);

        // Force use of v2 Signer.
        cc.setSignerOverride("S3SignerType");

        AmazonS3 client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .withClientConfiguration(cc)
                .withPathStyleAccessEnabled(true) // Path-style bucket naming is highly recommended
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(S3_ENDPOINT, null))
                .build();


		return client;
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
