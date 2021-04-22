package com.emc.ecs.s3.sample;
import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PutS3Request {
    private static final Logger logger = LoggerFactory.getLogger(PutS3Request.class);

    protected static byte[] sha256(String content) throws Exception { 
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         byte[] encodedhash = digest.digest(
                   content.getBytes(StandardCharsets.UTF_8));
         return encodedhash;
    }	
    protected static String bytesToHex(byte[] hash) {
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < hash.length; i++) {
    String hex = Integer.toHexString(0xff & hash[i]);
    if(hex.length() == 1) hexString.append('0');
        hexString.append(hex);
    }
    return hexString.toString();
    }

    protected static String emptySha() {
        try {
            return bytesToHex(sha256(""));
        } catch (Exception e)  {
            e.printStackTrace();
            return null;
        }
    }

    protected static byte[] HmacSHA256(String data, byte[] key) throws Exception {
        String algorithm="HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes("UTF8"));
    }

    protected static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes("UTF8");
        byte[] kDate = HmacSHA256(dateStamp, kSecret);
        byte[] kRegion = HmacSHA256(regionName, kDate);
        byte[] kService = HmacSHA256(serviceName, kRegion);
        byte[] kSigning = HmacSHA256("aws4_request", kService);
        return kSigning;
    }

    protected static Map<String, String> getHeaders(String amz_date, String authorization_header, String apiName, String content_type) {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-amz-date", amz_date);
        headers.put("Authorization", authorization_header);
        headers.put("x-amz-content-sha256", emptySha());
        headers.put("Accept", "application/json");
        headers.put("Connection", "keep-alive");
        return headers;
    }

    public static String getResponse(String methodName, String httpsURL, Map<String, String> headers, String payload) throws Exception {
            URL myurl = new URL(httpsURL);
            String response = null;
            logger.info("Sending a "  + methodName + " request to:"  + httpsURL);
            HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
            con.setRequestMethod(methodName);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                logger.info("Header "+entry.getKey()+": " + entry.getValue());
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }
            con.setDoOutput(true);
            con.setDoInput(true);
            if (payload.equals("") == false) {
            try (DataOutputStream output = new DataOutputStream(con.getOutputStream())) {
                logger.info("payload="+payload);
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
            logger.info("Resp Message:" + con.getResponseMessage());
            return response;
        }

    protected static String getDateString() {
        String dateString = null;
        try {
            Date dt = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            dateString = dateFormatter.format(dt);
            logger.info("x_amz_date = "+dateString);
        } catch (Exception e) {
            logger.error("Exception:", e); 
        }
        return dateString;
    }

    protected static String createBucketParameters(String bucketName) {
        String request_parameters = "";
/*
        request_parameters += "{";
        request_parameters += "\"Version\": \"2012-10-17\",";
        request_parameters += "\"Statement\": [";
        request_parameters += "{";
        request_parameters += "\"Sid\": \"statement1\",";
        request_parameters += "\"Effect\": \"Allow\",";
        // request_parameters += "\"Principal\": {";
        // request_parameters += "\"AWS\": \"arn:aws:iam::AccountB-ID:user/Dave\"";
        // request_parameters += "},";
        request_parameters += "\"Action\": [\"s3:CreateBucket\"],";
        request_parameters += "\"Resource\": [\"arn:aws:s3:::*\"],";
        request_parameters += "\"Condition\": {";
        request_parameters += "\"StringLike\": {";
        request_parameters += "\"s3:LocationConstraint\": \"us-east-1\"";
        request_parameters += "},";
        request_parameters += "\"StringEquals\": {";
        request_parameters += "\"s3:x-amz-acl\":[\"public-read-write\"]";
        request_parameters += "}";
        request_parameters += "}";
        request_parameters += "}";
        request_parameters += "]";
        request_parameters += "}";
*/
        return request_parameters;
    }

    protected static String createBucket(String bucketName) {
        String response = "";
        String host=bucketName + ".s3.amazonaws.com";
        String endpoint="https://s3.amazonaws.com/"+bucketName;
        String method = "PUT";
        String apiName = "CreateBucket";
        String content_type = "application/x-amz-json-1.0";
        String amz_date = getDateString();
        String request_parameters = createBucketParameters(bucketName);
        String authorization_header = getAuthorizationHeader(host, request_parameters, method, apiName);
        Map<String, String> headers = getHeaders(amz_date, authorization_header, apiName, content_type);
        try {
            response = getResponse(method, endpoint, headers, request_parameters);
        } catch (Exception e) {
          logger.error("Exception:", e);
        }
        logger.info("response:"+response);
        return response;
    }

    protected static String deleteBucket(String bucketName) {
        String response = "";
        String host = "s3.amazonaws.com";
        String endpoint="https://s3.amazonaws.com/" + bucketName; // + host + "/";
        String method = "DELETE";
        String apiName = "DeleteBucket";
        String content_type = "application/x-amz-json-1.0";
        String amz_date = getDateString();
        String authorization_header = getAuthorizationHeader(host, "", method, apiName);
        Map<String, String> headers = getHeaders(amz_date, authorization_header, apiName, content_type);
        try {
            response = getResponse(method, endpoint, headers, "");
        } catch (Exception e) {
          logger.error("Exception:", e);
        }
        logger.info("response:"+response);
        return response;
    }

    public static String getAuthorizationHeader(String host, String request_parameters, String method, String apiName) {
        String service="s3";
        String region="us-east-1";
        String amz_date = getDateString();
        logger.info("amz_date="+amz_date);
        String date_stamp = amz_date.substring(0, amz_date.indexOf("T"));
        String canonical_uri = "/";
        String canonical_querystring = "";
        String content_type = "application/x-amz-json-1.0";
        String amz_target = "GraniteServiceVersion20100801."+apiName;
        logger.info("host="+host);
        String canonical_headers =  "host:" + host + "\n" + "x-amz-content-sha256:" + emptySha() + "\n"  + "x-amz-date:" + amz_date ;
        String signed_headers = "host;x-amz-content-sha256;x-amz-date";
        String accessKey = "your_access_key";
        String accessSecretKey = "your_access_secret";
        String date = "20130806";
        String signing = "aws4_request";
       try {
            String payload_hash = bytesToHex(sha256(request_parameters));
            String canonical_request = method + "\n" + canonical_uri + "\n" + canonical_querystring + "\n" + canonical_headers + "\n" + signed_headers + "\n" + payload_hash;
            canonical_request = new String(canonical_request.getBytes("UTF-8"), "UTF-8");
            String algorithm = "AWS4-HMAC-SHA256";
            String credential_scope = date_stamp + "/" + region + "/" + service + "/" + "aws4_request";
            logger.info("date_stamp="+date_stamp);
            String string_to_sign = algorithm + "\n" +  amz_date + "\n" +  credential_scope + "\n" +  bytesToHex(sha256(canonical_request));
            string_to_sign = new String(string_to_sign.getBytes("UTF-8"), "UTF-8");
            byte[] signing_key = getSignatureKey(accessSecretKey, date_stamp, region, service);
            String signature = bytesToHex(HmacSHA256(string_to_sign, signing_key));
            String authorization_header = algorithm + " " + "Credential=" + accessKey + "/" + credential_scope + "," +  "SignedHeaders=" + signed_headers + ", " + "Signature=" + signature;
            return authorization_header;
       } catch (Exception e) {
            e.printStackTrace();
            return null;
       }
    }

    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
         createBucket("examplebucket");
         deleteBucket("examplebucket");
    }
}
/*
[main] INFO com.emc.ecs.s3.sample.PutS3Request - x_amz_date = 20190505T203151Z
[main] INFO com.emc.ecs.s3.sample.PutS3Request - x_amz_date = 20190505T203151Z
[main] INFO com.emc.ecs.s3.sample.PutS3Request - amz_date=20190505T203151Z
[main] INFO com.emc.ecs.s3.sample.PutS3Request - host=examplebucket.s3.amazonaws.com
[main] INFO com.emc.ecs.s3.sample.PutS3Request - date_stamp=20190505
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Sending a PUT request to:https://s3.amazonaws.com/examplebucket
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header Authorization: AWS4-HMAC-SHA256 Credential=your_access_key/20190505/us-east-1/s3/aws4_request,SignedHeaders=host;x-amz-content-sha256;x-amz-date, Signature=6e1cbaf4e9135c792bf84193235d8f2acc282b3d5f66ec003e9acc29a7613f8f
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header x-amz-content-sha256: e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header x-amz-date: 20190505T203151Z
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header Accept: application/json
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header Connection: keep-alive
[main] ERROR com.emc.ecs.s3.sample.PutS3Request - Exception:
java.io.IOException: Server returned HTTP response code: 403 for URL: https://s3.amazonaws.com/examplebucket
        at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1894)
        at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1492)
        at sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:263)
        at com.emc.ecs.s3.sample.PutS3Request.getResponse(PutS3Request.java:90)
        at com.emc.ecs.s3.sample.PutS3Request.createBucket(PutS3Request.java:158)
        at com.emc.ecs.s3.sample.PutS3Request.main(PutS3Request.java:224)
[main] INFO com.emc.ecs.s3.sample.PutS3Request - response:
[main] INFO com.emc.ecs.s3.sample.PutS3Request - x_amz_date = 20190505T203152Z
[main] INFO com.emc.ecs.s3.sample.PutS3Request - x_amz_date = 20190505T203152Z
[main] INFO com.emc.ecs.s3.sample.PutS3Request - amz_date=20190505T203152Z
[main] INFO com.emc.ecs.s3.sample.PutS3Request - host=s3.amazonaws.com
[main] INFO com.emc.ecs.s3.sample.PutS3Request - date_stamp=20190505
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Sending a DELETE request to:https://s3.amazonaws.com/examplebucket
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header Authorization: AWS4-HMAC-SHA256 Credential=your_access_key/20190505/us-east-1/s3/aws4_request,SignedHeaders=host;x-amz-content-sha256;x-amz-date, Signature=7f7aca28c6fd6facecf7fefb55ac375c57059ca2085178668ea142d6027018c8
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header x-amz-content-sha256: e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header x-amz-date: 20190505T203152Z
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header Accept: application/json
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header Connection: keep-alive
[main] ERROR com.emc.ecs.s3.sample.PutS3Request - Exception:
java.io.IOException: Server returned HTTP response code: 403 for URL: https://s3.amazonaws.com/examplebucket
        at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1894)
        at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1492)
        at sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:263)
        at com.emc.ecs.s3.sample.PutS3Request.getResponse(PutS3Request.java:90)
        at com.emc.ecs.s3.sample.PutS3Request.deleteBucket(PutS3Request.java:177)
        at com.emc.ecs.s3.sample.PutS3Request.main(PutS3Request.java:225)
[main] INFO com.emc.ecs.s3.sample.PutS3Request - response:
*/
