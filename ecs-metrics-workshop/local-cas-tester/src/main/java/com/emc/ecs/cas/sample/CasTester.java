package com.emc.ecs.monitoring.sample;
import com.filepool.fplibrary.*;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_BYTE_ARRAY;
import static org.joda.time.format.ISODateTimeFormat.basicDateTime;

import javax.net.ssl.HttpsURLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
* This class is a standalone utility to test the DELL ECS CAS Head Service from command-line
* It lists the helper methods required to test the CAS Head Service.
*/
public class CasTester {
    private static final Logger logger = LoggerFactory.getLogger(CasTester.class);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = basicDateTime().withZoneUTC();
    public static final int blobSize = 1024 * 16 * 2; //32768
    private static final String DEFAULT_BLOB_TAG_NAME = "blob";
    private static final String CLIP_LIST_TAG_NAME = "clip_list";
    private static final String STANDARD_CAS_HEAD_PORT = "3218";
    private static final byte[] payload = new byte[blobSize];

    static {
        for (int i = 0; i < payload.length; ++i) {
            for (byte c = 0x21; c <= 0x7A && i < payload.length; ++i, ++c) {
                payload[i] = c;
            }
        }
    }

    private class CasConnection {
        private FPPool fpPool;
        private String connectionString;

        public CasConnection(String connectionString, FPPool fpPool) {
            this.fpPool = fpPool;
            this.connectionString = connectionString;
        }

        public FPPool getFpPool() {
            return fpPool;
        }

        public String getConnectionString() {
            return connectionString;
        }

        public void Close() throws Exception {
            if (fpPool != null){
                fpPool.Close();
            }
        }
    }

    private CasConnection getCasConnection(String ip, String port, String user, String password, String namespace, File pea) throws Exception {
        String connectionString = format(
                "%s:%d?path=%s",
                ip, STANDARD_CAS_HEAD_PORT, pea.getAbsolutePath());
        FPPool fpPool = getFP(connectionString);
        if (fpPool == null) {
            String message = "FPPool could not be instantiated.";
            logger.error(message);
            throw new Exception(message);
        }
        CasConnection casConnection = new CasConnection(connectionString, fpPool);
        return casConnection;
    }

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

    private static FPPool getFP(String connectionString) throws Exception {
        FPPool fpPool = null;
        try {
            fpPool = new FPPool(connectionString);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception:", e);
        } finally {
            if(fpPool != null) {
                fpPool.Close();
            }
        }
        return fpPool;
    }


    private static String fillParams(String pattern, String ip, String port, String user, String namespace){
        String result = pattern
                .replace("{ip}", ip)
                .replace("{port}", port)
                .replace("{userId}", user)
                .replace("{namespace}", namespace);
        return result;
    }

    public static String getUserCasSecret(String ip, String port, String user, String namespace){
        String secretEndPoint = fillParams("https://{ip}:{port}/object/user-cas/secret/{namespace}/{userId}",
                ip, port, user, namespace);
        logger.info("address={}", secretEndPoint);
        String response = null;
        try {
            Map<String, String> headers = getHeaders();
            response = getResponse(secretEndPoint, headers, null, "GET");
            logger.info("secret:" + response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception:", e);
        }
        return response;
    }

    public static String getProfilePea(String ip, String port, String user, String namespace){
        String peaEndpoint = fillParams("https://{ip}:{port}/object/user-cas/secret/{namespace}/{userId}/pea",
                                        ip, port, user, namespace);
        logger.info("address={}", peaEndpoint);
        String response = null;
        try {
            Map<String, String> headers = getHeaders();
            response = getResponse(peaEndpoint, headers, null, "GET");
            logger.info("pea:" + response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception:", e);
        }
        return response;
    }

    public static File fetchPeaFile(String ip,
                              String port,
                              final String user,
                              final String namespace) {
        final File peaFile = new File(
                FileUtils.getTempDirectory(),
                String.join("-", user, ip, port) + ".pea"
        );

        final String pea = getProfilePea(ip, port, user, namespace);

        try {
            FileUtils.writeStringToFile(peaFile, pea);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        return peaFile;
    }

    private static class ClipsTask implements Callable<Void> {
        private final CasConnection casConnection;
        private final String ecsCasSecret;
        private final File pea;
        private byte[] payload;

        public ClipsTask(CasConnection casConnection, String ecsCasSecret, File pea) {
            this.casConnection = casConnection;
            this.ecsCasSecret = ecsCasSecret;
            this.pea = pea;
            final byte[] payloadBytes = new byte[1024];
            for (int i = 0; i < payloadBytes.length; ++i) {
                for (byte c = 0x21; c <= 0x7A && i < payloadBytes.length; ++i, ++c) {
                    this.payload[i] = c;
                }
            }
        }
        public void accept(FPPool fpPool, FPClip fpClip) {
            try {
                final FPTag topTag = fpClip.getTopTag();
                try {
                    final FPTag blob = new FPTag(topTag, DEFAULT_BLOB_TAG_NAME);
                    try {
                        final byte[] clipTime = EMPTY_BYTE_ARRAY;

                        blob.BlobWrite(new SequenceInputStream(new ByteArrayInputStream(clipTime), new ByteArrayInputStream(payload)));
                    } finally {
                        blob.Close();
                    }
                } finally {
                    topTag.Close();
                }
            } catch (FPLibraryException | IOException e) {
                Throwables.propagate(e);
            }
        }

        @Override
        public Void call() throws Exception {
            try {
                for (int i = 0; i < 10; i++) {
                    final Optional<DateTime> dateTimeOp = Optional.empty();
                    final FPClip fpClip = new FPClip(casConnection.getFpPool());
                    try {
                        accept(casConnection.getFpPool(), fpClip);
                        final String clipRefId = fpClip.Write();

                        final ByteArrayOutputStream cdf = new ByteArrayOutputStream();
                        fpClip.RawRead(cdf);
                        logger.info("clip Id: {}, size read = {}", clipRefId, cdf.size());
                    } finally {
                        fpClip.Close();
                    }

                }
            } catch (RuntimeException | IOException | FPLibraryException e) {
                throw e;
            }

            return null;
        }
    }

    public void  writeClips(String ip, String port, String user, String password, String namespace) throws Exception {
        final String ecsCasSecret = getUserCasSecret(ip, port, user, namespace);
        if (ecsCasSecret == null) {
            String message = "Credentials are not correct";
            logger.error(message);
            throw new Exception(message);
        }
        File pea = fetchPeaFile(ip, port, user, namespace);
        CasConnection casConnection = getCasConnection(ip, port, user, password, namespace, pea);

        try {
            final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));

            executor.submit(new ClipsTask(casConnection, ecsCasSecret, pea));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception:", e);
        }

        Thread.sleep(15000);
        casConnection.Close();
    }

    public void main(String[] args) throws Exception {
        String ip = "10.247.142.111"; // NetworkUtility.getNodeIp();
        String user = "apiuser";
        String port = "3218";
        String password = "";
        String namespace = "s3";
        String bucket = "b6";
        try {

            writeClips(ip, port, user, password, namespace);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception:", e);
        }
    }
}
