package com.emc.vipr.s3.sample;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringInputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.util.StringInputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

/**
 * Created by conerj on 4/12/17.
 */
public class _10_CopyObject {

    public static void main(String[] args) throws Exception {
        _10_CopyObject co = new _10_CopyObject();
        co.copy1();
        //co.copy2();
    }

    //can go to different bucket with same or different key name
    private void copy1() throws Exception {
        // create the AWS S3 Client
        AmazonS3 s3 = AWSS3Factory.getS3Client();

        // retrieve the key value from user
        System.out.println( "Enter the source object key:" );
        String srcKey = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        // retrieve the key value from user
        System.out.println( "Enter the source bucket name:" );
        String srcBucket = new BufferedReader( new InputStreamReader( System.in ) ).readLine();


        // retrieve the key value from user
        System.out.println( "Enter the dest object key:" );
        String destKey = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        // retrieve the key value from user
        System.out.println( "Enter the dest bucket name:" );
        String destBucket = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        CopyObjectRequest cor = new CopyObjectRequest(srcBucket, srcKey, destBucket, destKey);
        s3.copyObject(cor);

    }

    private void copy2() throws Exception {
        String destPrefix = "destprefix/";
        // create the AWS S3 Client
        AmazonS3 s3 = AWSS3Factory.getS3Client();

        ListObjectsRequest req = new ListObjectsRequest().withBucketName(AWSS3Factory.S3_BUCKET)
        .withPrefix("vz/");

        ObjectListing listing = s3.listObjects(req);
        List<S3ObjectSummary> summaries = listing.getObjectSummaries();

        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects (listing);
            summaries.addAll (listing.getObjectSummaries());
        }

        Iterator<S3ObjectSummary> iter = summaries.iterator();
        while (iter.hasNext()) {
            S3ObjectSummary objSum = iter.next();
            s3.copyObject(AWSS3Factory.S3_BUCKET, objSum.getKey(), AWSS3Factory.S3_BUCKET, destPrefix + objSum.getKey());
        }
    }
}
