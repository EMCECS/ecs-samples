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

import com.emc.object.s3.S3Client;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.jersey.S3JerseyClient;

import org.apache.commons.codec.binary.Base64;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

/**
 * Factory class to create the ECS S3 client.  The client will be used in the examples for the
 * Java ECS S3 interface.
 */
public class ECSS3Factory {

    /* the S3 access key id - this is equivalent to the user */
    public static final String S3_ACCESS_KEY_ID = "";

    /* the S3 secret key associated with the S3_ACCESS_KEY_ID */
    public static final String S3_SECRET_KEY = "";

    /* the end point of the ECS S3 REST interface */
    public static final String S3_URI = "";

    /* a unique bucket name to store objects */
    public static final String S3_BUCKET = "workshop-bucket";

    /* the optional namespace within ECS - leave blank to use the default namespace */
    public static final String S3_ECS_NAMESPACE = null; // use default namespace

    /* a unique object name to store */
    public static final String S3_OBJECT = "workshop-object";

    private static S3Client s3client;

    private ECSS3Factory(){}

    public static synchronized S3Client getS3Client() throws URISyntaxException {

        // for client-side load balancing
        //S3Config config = new S3Config(Protocol.HTTPS, S3_HOST1, S3_HOST2);
        // ditto with multiple VDCs
        //S3Config config = new S3Config(Protocol.HTTPS, new Vdc(S3_V1_HOST), new Vdc(S3_V2_HOST));

        if (s3client == null) {
            S3Config config = new S3Config(new URI(S3_URI));
            config.withIdentity(S3_ACCESS_KEY_ID).withSecretKey(S3_SECRET_KEY);
            s3client = new S3JerseyClient(config);
        }
        return s3client;
    }

    /*
    private static void checkProxyConfig(AmazonS3Client client, Properties props) {
        String proxyHost = props.getProperty(PROP_PROXY_HOST);
        if (proxyHost != null && !proxyHost.isEmpty()) {
            int proxyPort = Integer.parseInt(props.getProperty(PROP_PROXY_PORT));
            ClientConfiguration config = new ClientConfiguration();
            config.setProxyHost(proxyHost);
            config.setProxyPort(proxyPort);
            client.setConfiguration(config);
        }
    }
     */

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
