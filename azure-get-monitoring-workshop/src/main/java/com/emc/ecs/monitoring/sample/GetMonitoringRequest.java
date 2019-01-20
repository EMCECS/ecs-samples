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

public class GetMonitoringRequest {
    private static final Logger logger = LoggerFactory.getLogger(GetMonitoringRequest.class);

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
        String AZURE_ACCESS_KEY_ID="my_access_key_id";
        String AZURE_SECRET_ACCESS_KEY="my_access_secret_id";
        String AZURE_TENANT_ID = "my_azure_tenant_guid";
        String ARMResource = "https://management.core.windows.net/";
        String SPNPayload = "resource={0}&client_id={1}&grant_type=client_credentials&client_secret={2}";
        String endpoint="https://management.azure.com/";
        String AZURE_request_parameters="Action=GetMetricStatistics&Version=2010-08-01";
        String amz_date = "20181230T125500Z";
        String date_stamp = "20181230";
        String subscriptionId = "my_azure_subscription_guid";
        String resourceGroupName = "RaviRajamaniRG";
        String resource = "subscriptions/"+ subscriptionId + "/resourceGroups/" +  resourceGroupName + "/providers/Microsoft.Web/sites/shrink-text/metricdefinitions?api-version=2018-02-01";
        String canonical_uri = endpoint + resource;
        String canonical_querystring = "";
        String method = "POST";
        String accessKey = "my_access_key_id";
        String accessSecretKey = "my_access_secret_id";
        String tenantId = "my_azure_tenant_id";
        String armResource = "https://management.core.windows.net/";
        AZURE_ACCESS_KEY_ID=accessKey;
        AZURE_SECRET_ACCESS_KEY=accessSecretKey;
        String request_parameters = "";

        try {
            String token = getToken(AZURE_ACCESS_KEY_ID, AZURE_SECRET_ACCESS_KEY, ARMResource, AZURE_TENANT_ID, SPNPayload);
            logger.info("token="+token);
            Map<String, String> headers= getHeaders();
            headers.put("Authorization", "Bearer " + token);
            String response = getResponse(canonical_uri, headers, request_parameters, "GET");
            logger.info("response:"+response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception:", e);
        }
    }
}
//
// output:
/*
(venv) ravi@RaviRajamani:~/ecs/ecs-samples/azure-java-workshop1$ java -jar build/libs/azure-java-workshop1-1.0.jar
[main] INFO com.emc.ecs.monitoring.sample.GetMonitoringRequest - address=https://login.windows.net/<tenantId>/oauth2/token
[main] INFO com.emc.ecs.monitoring.sample.GetMonitoringRequest - payload=resource=https%3A%2F%2Fmanagement.core.windows.net%2F&client_id=<client_id>&grant_type=client_credentials&client_secret=<client_secret>
[main] INFO com.emc.ecs.monitoring.sample.GetMonitoringRequest - Sending a POST request to:https://login.windows.net/<tenantId>/oauth2/token
[main] INFO com.emc.ecs.monitoring.sample.GetMonitoringRequest - Resp Code:200
[main] INFO com.emc.ecs.monitoring.sample.GetMonitoringRequest - response:{"token_type":"Bearer","expires_in":"3600","ext_expires_in":"3600","expires_on":"1547434329","not_before":"1547430429","resource":"https://management.core.windows.net/","access_token":"<MyToken>"}
[main] INFO com.emc.ecs.monitoring.sample.GetMonitoringRequest - token=<MyToken>
[main] INFO com.emc.ecs.monitoring.sample.GetMonitoringRequest - Sending a GET request to:https://management.azure.com/subscriptions/<tenantId>/resourceGroups/RaviRajamaniRG/providers/Microsoft.Web/sites/shrink-text/metricdefinitions?api-version=2018-02-01
[main] INFO com.emc.ecs.monitoring.sample.GetMonitoringRequest - Resp Code:200
*/
