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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BucketList implements Runnable {
    public static final int DEFAULT_THREADS = 32;
    public static final String DEFAULT_DELIMITER = "/";

    public BucketList(AmazonS3 client, String bucket, String prefix, String delimiter, int threads) throws IOException {
        output = new BufferedWriter(new FileWriter(new File("bucket-list.out")));
        this.s3 = client;
        this.bucket = bucket;
        this.delimiter = delimiter;
        this.prefix = prefix;
        this.threads = threads;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) { this.threads = threads; }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) { this.prefix = prefix; }

    public long getListedObjects() { return listedObjects.get(); }

    public long getListedPrefixes() { return listedPrefixes.get(); }

    public List<String> getErrors() { return errors; }

    public synchronized List<String> getSubPrefixes() { return subPrefixes; }

    public synchronized void printKey(String key) throws IOException {
        output.write(key);
        output.write("\n");
    }

    @Override
    public void run() {

        System.out.println(String.format("Working on bucket: [%s]", bucket));
        executor = Executors.newFixedThreadPool(threads);
        listAllObjects(s3);
        executor.shutdown();
    }

    protected void listAllObjects(AmazonS3 client) {
        List<Future> futures = new ArrayList<>();
        futures.add(executor.submit(new ListPage(client, bucket, delimiter, prefix)));
        try {
            while (runningTasks.get() > 0 || subPrefixes.size() > 0) {
                Iterator i = getSubPrefixes().iterator(); // Must be in synchronized block
                while (i.hasNext()) {
                    futures.add(executor.submit(new ListPage(client, bucket, delimiter, i.next().toString())));
                    i.remove();
                }
                handleSingleFutures(futures);
                Thread.sleep(1000);
            }

        } catch (InterruptedException e) {
            // :)
        }
        handleSingleFutures(futures);
    }

    private AmazonS3 s3;
    private URI endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String delimiter = DEFAULT_DELIMITER;
    private String prefix = null;
    private ExecutorService executor;
    private int threads = DEFAULT_THREADS;
    private List<String> errors = Collections.synchronizedList(new ArrayList<String>());
    private AtomicInteger listedObjects = new AtomicInteger(0);
    private AtomicInteger listedPrefixes = new AtomicInteger(0);
    private AtomicInteger runningTasks = new AtomicInteger(0);
    private List<String> subPrefixes = Collections.synchronizedList(new ArrayList<String>());
    public BufferedWriter output;

    protected class ListResult {
        private int keys;
        private int subKeys;

        public ListResult() {
            this.keys = 0;
            this.subKeys = 0;
        }
    }

    protected class ListPage implements Callable<ListResult> {
        private AmazonS3 client;
        private String bucket;
        private String delimiter;
        private String prefix;

        public ListPage(AmazonS3 client, String bucket, String delimiter, String prefix) {
            runningTasks.addAndGet(1);
            this.client = client;
            this.bucket = bucket;
            this.delimiter = delimiter;
            this.prefix = prefix;
        }

        @Override
        public ListResult call() {
            ListResult total = new ListResult();
            try {
                ObjectListing result = null;
                do {
                    ListObjectsRequest request = new ListObjectsRequest();
                    request.setBucketName(bucket);
                    request.setDelimiter(delimiter);
                    if (result != null) {
                        request.setMarker(result.getMarker());
                    }
                    if (prefix != null) {
                        request.setPrefix(prefix);
                    }
                    int count = 0;
                    int maxRetry = 10;
                    while (true) {
                        try {
                            result = client.listObjects(request);
                            break;
                        } catch (Exception e) {
                            if (++count == maxRetry)
                                throw e;
                        }
                    }
                    for (S3ObjectSummary summary : result.getObjectSummaries()) {
                        printKey(summary.getKey());
                    }
                    total.keys += result.getObjectSummaries().size();
                    total.subKeys += result.getCommonPrefixes().size();
                    subPrefixes.addAll(result.getCommonPrefixes());
                } while (result.isTruncated());

            } catch (AmazonS3Exception a) {
                errors.add(a.getCause().getMessage());
            } catch (IOException e) {
                errors.add(e.getMessage());
            }
            return total;
        }
    }

    protected void handleSingleFutures(List<Future> futures) {
        for (Iterator<Future> i = futures.iterator(); i.hasNext(); ) {
            Future future = i.next();
            i.remove();
            try {
                ListResult result = (ListResult) future.get();
                listedObjects.addAndGet(result.keys);
                listedPrefixes.addAndGet(result.subKeys);
                runningTasks.decrementAndGet();
            } catch (InterruptedException e) {
                errors.add(e.getMessage());
            } catch (ExecutionException e) {
                errors.add(e.getCause().getMessage());
            }
        }
    }
}
