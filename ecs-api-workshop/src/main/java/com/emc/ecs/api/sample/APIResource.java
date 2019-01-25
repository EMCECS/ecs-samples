package com.emc.ecs.api.sample;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIResource {
    private static final Logger logger = LoggerFactory.getLogger(APIResource.class);

    protected static Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Accept", "application/json");
        headers.put("Content-Encoding", "UTF-8");
        headers.put("Connection", "keep-alive");
        return headers;
    }


    public static String getResponse(String httpsURL, Map<String, String> headers, String payload, String method) throws Exception {
        URL myurl = new URL(httpsURL);
        String response = null;
        logger.info("Sending a " + method + " request to:"  + httpsURL);
        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
        con.setRequestMethod(method);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }
        con.setDoOutput(true);
        con.setDoInput(true);
        if (method.equals("POST")) {
            try (DataOutputStream output = new DataOutputStream(con.getOutputStream())) {
                output.writeBytes(payload);
            }
        }
        try (DataInputStream input = new DataInputStream(con.getInputStream())) {
            StringBuffer contents = new StringBuffer();
            String tmp;
            while ((tmp = input.readLine()) != null) {
                contents.append(tmp);
                logger.debug("tmp="+tmp);
            }
            response = contents.toString();
        }
        logger.info("Resp Code:" + con.getResponseCode());
        // logger.info("Resp Message:" + con.getResponseMessage());
        return response;
    }

    private static String getToken(String host, String username, String password) throws Exception {
        String tokenEndpoint = "/login";
        String address = "https://" + host + ":4443" + tokenEndpoint;
        logger.info("address="+address);
        URL myurl = new URL(address);
        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
        String method = "GET";
        con.setRequestMethod(method);
        Map<String, String> headers = getHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }
        String credential = username + ":" + password;
        String encodedString = Base64.getEncoder().encodeToString(credential.getBytes());
        con.setDoOutput(true);
        con.setDoInput(true);
        String response = null;
        String token = "";
        if (con.getResponseCode() == 200) {
            token = con.getHeaderField("X-SDS-AUTH-TOKEN");
        }
        logger.debug("token={}", token);
        return token;
    }

    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
        String host = "localhost";
        String username = "";
        String password = "";
        String endpoint = "https://"+ host + ":4443";
        String canonical_querystring = "";
        String method = "POST";
        try {
            String token = getToken(host, username, password);
            logger.info("token="+token);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception:", e);
        }
    }
}

// Output:
// [main] INFO com.emc.ecs.api.sample.APIResource - address=https://localhost:4443/login

