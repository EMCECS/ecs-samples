package com.emc.ecs.monitoring.sample;
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

public class PutMonitoringRequest {
    private static final Logger logger = LoggerFactory.getLogger(PutMonitoringRequest.class);

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
        return response;
    }


    private static String getToken(String accessKeyId, String accessSecret, String ARMResource, String tenantId, String spnPayload) {
        String TokenEndpoint = "https://login.windows.net/{0}/oauth2/token";
        String address = "https://login.windows.net/" + tenantId + "/oauth2/token";
        logger.info("address="+address);
        String token = "";
        try {
            String payload = "resource=" + 
                             java.net.URLEncoder.encode(ARMResource,"UTF-8") + 
                             "&client_id=" + java.net.URLEncoder.encode(accessKeyId, "UTF-8") + 
                             "&grant_type=client_credentials&client_secret=" + 
                             java.net.URLEncoder.encode(accessSecret, "UTF-8");
            logger.info("payload="+payload);
            Map<String, String> headers = getHeaders();
            String response = getResponse(address, headers, payload, "POST");
            int start = response.indexOf("\"access_token\":\"");
            if ( start != -1 ) {
                start += 16;
                int end = response.indexOf("\"", start);
                if ( end != -1  && end > start) {
                    token = response.substring(start, end);
                    logger.info("response:" + response);
                } else {
                    logger.info("token not found in response.");
                }
            } else {
                logger.info("access_token not found in response.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception:", e);
        }
        return token;
    }

    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
        String endpoint = "https://management.azure.com/";
        String subscriptionId = "my_azure_subscription_id";
        String resourceGroupName = "RaviRajamaniRG";
        String resource = "subscriptions/" + subscriptionId + "/resourcegroups/DELLEMC/providers/ECS/storageAccounts/objectstore/metrics?api-version=2018-02-01";
        String canonical_uri = endpoint + resource;
        String canonical_querystring = "";
        String method = "POST";
        String accessKey = "my_accesss_key_id";
        String accessSecretKey = "my_access_secret_id";
        String tenantId = "my_azure_tenant_id";
        String armResource = "https://management.core.windows.net/";
        String spnPayload = "resource={0}&client_id={1}&grant_type=client_credentials&client_secret={2}";
        String request_parameters = "{";
        request_parameters += "\"Namespace\":\"On-PremiseObjectStorageMetrics\",";
        request_parameters += "\"MetricData\":";
        request_parameters += "[";
        request_parameters += "  {";
        request_parameters += "    \"MetricName\": \"NumberOfObjects1\",";
        request_parameters += "    \"Dimensions\": [";
        request_parameters += "      {";
        request_parameters += "        \"Name\": \"BucketName\",";
        request_parameters += "        \"Value\": \"ExampleBucket\"";
        request_parameters += "      },";
        request_parameters += "      {";
        request_parameters += "        \"Name\": \"ECSSystemId\",";
        request_parameters += "        \"Value\": \"UUID\"";
        request_parameters += "      }";
        request_parameters += "    ],";
        request_parameters += "    \"Timestamp\": " + null + ",";
        request_parameters += "    \"Value\": 10,";
        request_parameters += "    \"Unit\": \"Count\",";
        request_parameters += "    \"StorageResolution\": 60";
        request_parameters += "  }";
        request_parameters += "]";
        request_parameters += "}";
        request_parameters = new String(request_parameters.getBytes("UTF-8"), "UTF-8");

        try {
            String token = getToken(accessKey, accessSecretKey, armResource, tenantId, spnPayload);
            logger.info("token="+token);
            Map<String, String> headers= getHeaders();
            headers.put("Authorization", "Bearer " + token);
            headers.remove("Content-Type");
            headers.put("Content-Type", "application/json");
            String response = getResponse(canonical_uri, headers, request_parameters, "POST");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception:", e);
        }
    }
}
//
// output:
//
//[main] INFO com.emc.ecs.monitoring.sample.PutMonitoringRequest - address=https://login.windows.net/<tenantId>/oauth2/token
//        [main] INFO com.emc.ecs.monitoring.sample.PutMonitoringRequest - payload=resource=https%3A%2F%2Fmanagement.core.windows.net%2F&client_id=<my_client_id>&grant_type=client_credentials&client_secret=<my_client_secret>
//        [main] INFO com.emc.ecs.monitoring.sample.PutMonitoringRequest - Sending a POST request to:https://login.windows.net/tenantId/oauth2/token
//        [main] INFO com.emc.ecs.monitoring.sample.PutMonitoringRequest - Resp Code:200
//        [main] INFO com.emc.ecs.monitoring.sample.PutMonitoringRequest - response:{"token_type":"Bearer","expires_in":"3599","ext_expires_in":"3599","expires_on":"1547440166","not_before":"1547436266","resource":"https://management.core.windows.net/","access_token":"<myToken>"}
//        [main] INFO com.emc.ecs.monitoring.sample.PutMonitoringRequest - token=<myToken>
//        [main] INFO com.emc.ecs.monitoring.sample.PutMonitoringRequest - Sending a POST request to:https://management.azure.com/subscriptions/656e67c6-f810-4ea6-8b89-636dd0b6774c/resourcegroups/DELLEMC/providers/ECS/storageAccounts/objectstore/metrics?api-version=2018-02-01

