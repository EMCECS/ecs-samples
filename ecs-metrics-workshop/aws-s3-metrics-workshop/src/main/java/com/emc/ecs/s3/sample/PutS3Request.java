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
        headers.put("x-amz-target", "GraniteServiceVersion20100801."+apiName);
        headers.put("Content-Type", content_type);
        headers.put("Accept", "application/json");
        headers.put("Content-Encoding", "amz-1.0");
        headers.put("Connection", "keep-alive");
        headers.put("x-amz-acl", "public-read-write");
        headers.put("Host", "examplebucket.s3.amazonaws.com");
        return headers;
    }


    public static String getResponse(String httpsURL, Map<String, String> headers, String payload) throws Exception {
            URL myurl = new URL(httpsURL);
            String response = null;
            logger.info("Sending a post request to:"  + httpsURL);
            HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
            con.setRequestMethod("POST");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                logger.info("Header "+entry.getKey()+": " + entry.getValue());
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }
            con.setDoOutput(true);
            con.setDoInput(true);
            try (DataOutputStream output = new DataOutputStream(con.getOutputStream())) {
                output.writeBytes(payload);
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
    protected static void createBucket() {
       String host = "s3.amazonaws.com";
       String region="us-east-1";
       String endpoint="https://s3.amazonaws.com";
      
    }
    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
        String AWS_ACCESS_KEY_ID="MyAccessKeyId";
        String AWS_SECRET_ACCESS_KEY="MyAccessSecret";
        String service="s3";
        String host="s3.amazonaws.com";
        String region="us-east-1";
        String endpoint="https://s3.amazonaws.com/examplebucket";
        String AWS_request_parameters="Action=CreateBucket&Version=2010-08-01";
        String amz_date = getDateString(); 
        String date_stamp = amz_date.substring(0, amz_date.indexOf("T"));
        String canonical_uri = "/";
        String canonical_querystring = "";
        String method = "POST";
        String apiName = "CreateBucket";
        String content_type = "application/x-amz-json-1.0";
        String amz_target = "GraniteServiceVersion20100801."+apiName;
        String canonical_headers = "content-type:" + content_type + "\n" + "host:" + host + "\n" + "x-amz-date:" + amz_date + "\n" + "x-amz-target:" + amz_target + "\n";
        String signed_headers = "content-type;host;x-amz-date;x-amz-target";
        String accessKey = AWS_ACCESS_KEY_ID;
        String accessSecretKey = AWS_SECRET_ACCESS_KEY;
        String date = "20130806";
        String signing = "aws4_request";
        String request_parameters = "";
        request_parameters += "{";
        request_parameters += "\"Version\": \"2010-08-01\",";
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
        request_parameters += "}";
        request_parameters += "}";
        request_parameters += "}";
        request_parameters += "]";
        request_parameters += "}";
        request_parameters = new String(request_parameters.getBytes("UTF-8"), "UTF-8");
        logger.info("REQUEST_PARAMETERS="+request_parameters);

        try {
            String payload_hash = bytesToHex(sha256(request_parameters)); 
            String canonical_request = method + "\n" + canonical_uri + "\n" + canonical_querystring + "\n" + canonical_headers + "\n" + signed_headers + "\n" + payload_hash;
            canonical_request = new String(canonical_request.getBytes("UTF-8"), "UTF-8");
            String algorithm = "AWS4-HMAC-SHA256";
            String credential_scope = date_stamp + "/" + region + "/" + service + "/" + "aws4_request";
            String string_to_sign = algorithm + "\n" +  amz_date + "\n" +  credential_scope + "\n" +  bytesToHex(sha256(canonical_request));
            string_to_sign = new String(string_to_sign.getBytes("UTF-8"), "UTF-8");
            byte[] signing_key = getSignatureKey(accessSecretKey, date_stamp, region, service);
            String signature = bytesToHex(HmacSHA256(string_to_sign, signing_key));
            logger.info("signature: {}", bytesToHex(signing_key));
            String authorization_header = algorithm + " " + "Credential=" + accessKey + "/" + credential_scope + ", " +  "SignedHeaders=" + signed_headers + ", " + "Signature=" + signature;
            logger.info("authorization_header="+authorization_header);
            Map<String, String> headers = getHeaders(amz_date, authorization_header, apiName, content_type);
            logger.info("Sending request with:" + request_parameters);
            String response = getResponse(endpoint, headers, request_parameters);
            logger.info("response:"+response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception:", e);
        }
    }
}
/*
[main] INFO com.emc.ecs.s3.sample.PutS3Request - x_amz_date = 20190501T113211Z
[main] INFO com.emc.ecs.s3.sample.PutS3Request - REQUEST_PARAMETERS={"Version": "2010-08-01","Statement": [{"Sid": "statement1","Effect": "Allow","Action": ["s3:CreateBucket"],"Resource": ["arn:aws:s3:::*"],"Condition": {"StringLike": {"s3:LocationConstraint": "us-east-1"}}}]}
[main] INFO com.emc.ecs.s3.sample.PutS3Request - signature: 10af6286fceb3822eee82253fd75411df979f0e8fe8d6d2eaa686d78a8a6dc4e
[main] INFO com.emc.ecs.s3.sample.PutS3Request - authorization_header=AWS4-HMAC-SHA256 Credential=<MyObfuscatedCredential>/20190501/us-east-1/s3/aws4_request, SignedHeaders=content-type;host;x-amz-date;x-amz-target, Signature=b00a18e1d41d1f16637c9ab6d2e5108a3a16a5c0398f830f0ae41b4ff697530d
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Sending request with:{"Version": "2010-08-01","Statement": [{"Sid": "statement1","Effect": "Allow","Action": ["s3:CreateBucket"],"Resource": ["arn:aws:s3:::*"],"Condition": {"StringLike": {"s3:LocationConstraint": "us-east-1"}}}]}
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Sending a post request to:https://s3.amazonaws.com/examplebucket
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header Authorization: AWS4-HMAC-SHA256 Credential=<MyObfuscatedCredential>/20190501/us-east-1/s3/aws4_request, SignedHeaders=content-type;host;x-amz-date;x-amz-target, Signature=b00a18e1d41d1f16637c9ab6d2e5108a3a16a5c0398f830f0ae41b4ff697530d
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header x-amz-target: GraniteServiceVersion20100801.CreateBucket
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header x-amz-date: 20190501T113211Z
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header Accept: application/json
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header x-amz-acl: public-read-write
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header Content-Encoding: amz-1.0
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header Connection: keep-alive
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header Host: examplebucket.s3.amazonaws.com
[main] INFO com.emc.ecs.s3.sample.PutS3Request - Header Content-Type: application/x-amz-json-1.0
java.io.IOException: Server returned HTTP response code: 412 for URL: https://s3.amazonaws.com/examplebucket
*/
