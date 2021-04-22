package com.emc.ecs.api.sample;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Resource {
    private static final Logger logger = LoggerFactory.getLogger(Resource.class);

    public static Map<String, String> getHeaders(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-SDS-AUTH-TOKEN", token);
        headers.put("Content-Type", "application/json");
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
        return response;
    }

    public static String getToken() {
        // read from config or provide as constant
        String token = "my_access_token";
        return token;
    }
   
    public static String getPath() {
         return "https://localhost:4443";
    }

    public static Map<String, String> getHeadersWithAuth() {
        String token = getToken();
        return getHeaders(token);
    }
}
