package com.emc.vipr.s3.sample;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LargeFileDownloader implements Runnable {

    public static final int MIN_PART_SIZE = 2 * 1024 * 1024; // 2MB
    public static final int DEFAULT_PART_SIZE = 4 * 1024 * 1024; // 4MB

    public static final int DEFAULT_THREADS = 8;


    private AmazonS3 s3Client;
    private String bucket;
    private String key;
    private File file;
    private long objectSize;
    private long partSize = DEFAULT_PART_SIZE;
    private int threads = DEFAULT_THREADS;
    private ExecutorService executorService;

    /**
     * Creates a new LargeFileDownloader instance that will use <code>AmazonS3Client</code> to download
     * <code>bucket/key</code> to <code>file</code>.
     */
    public LargeFileDownloader(AmazonS3 s3Client, String bucket, String key, File file) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.key = key;
        this.file = file;
    }

    @Override
    public void run() {
        // sanity checks
        if (file.exists() && !file.canWrite())
            throw new IllegalArgumentException("cannot write to file: " + file.getPath());

        if (partSize < MIN_PART_SIZE) {
            System.out.println(String.format("%,dk is below the minimum part size (%,dk). the minimum will be used instead",
                    partSize / 1024, MIN_PART_SIZE / 1024));
            partSize = MIN_PART_SIZE;
        }

        ObjectMetadata objectMetadata = s3Client.getObjectMetadata(bucket, key);
        objectSize = objectMetadata.getContentLength();

        // set up thread pool
        if (executorService == null) executorService = Executors.newFixedThreadPool(threads);
        List<Future<Void>> futures = new ArrayList<Future<Void>>();

        try {
            // open file for random write
            RandomAccessFile raFile = new RandomAccessFile(file, "rw");
            raFile.setLength(objectSize);
            FileChannel channel = raFile.getChannel();


            // submit all download tasks
            long offset = 0, length = partSize;
            while (offset < objectSize) {
                if (offset + length > objectSize) length = objectSize - offset;
                futures.add(executorService.submit(new DownloadPartTask(offset, length, channel)));
                offset += length;
            }

            // wait for threads to finish
            for (Future<Void> future : futures) {
                future.get();
            }

            // close file
            raFile.close();

        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("error downloading file", e);
        } finally {

            // make sure all spawned threads are shut down
            executorService.shutdown();
        }
    }

    public AmazonS3 getS3Client() {
        return s3Client;
    }

    public String getBucket() {
        return bucket;
    }

    public String getKey() {
        return key;
    }

    public File getFile() {
        return file;
    }

    public long getPartSize() {
        return partSize;
    }

    public long getObjectSize() {
        return objectSize;
    }

    /**
     * Sets the size of each part to download. Note that 1MB is the minimum part size and
     * the default is 5MB.
     */
    public void setPartSize(long partSize) {
        this.partSize = partSize;
    }

    public int getThreads() {
        return threads;
    }

    /**
     * Sets the number of threads to use for transferring parts. <code>thread</code> parts will be
     * transferred in parallel. Default is 6
     */
    public void setThreads(int threads) {
        this.threads = threads;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Allows for providing a custom thread executor (i.e. for custom thread factories). Note that if
     * you set a custom executor service, the <code>threads</code> property will be ignored.
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    protected class DownloadPartTask implements Callable<Void> {
        private long start;
        private long end;
        private FileChannel channel;

        public DownloadPartTask(long offset, long length, FileChannel channel) {
            this.start = offset;
            this.end = offset + length -1;
            this.channel = channel;
        }

        @Override
        public Void call() throws Exception {
            byte[] data = IOUtils.toByteArray(s3Client.getObject(new GetObjectRequest(bucket, key).withRange(start, end)).getObjectContent());
            channel.write(ByteBuffer.wrap(data), start);
            return null;
        }
    }
}
