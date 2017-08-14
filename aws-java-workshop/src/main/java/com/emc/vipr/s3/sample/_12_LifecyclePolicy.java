package com.emc.vipr.s3.sample;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilter;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilterPredicate;
import com.amazonaws.services.s3.model.lifecycle.LifecyclePredicateVisitor;
import com.amazonaws.services.s3.model.lifecycle.LifecyclePrefixPredicate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by schumb on 8/14/2017.
 */
public class _12_LifecyclePolicy {

    public static void main(String[] args) throws Exception {

        System.out.println( "Enter the number of days to keep objects before delete:" );
        String days = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
        System.out.println( "Enter the prefix (i.e. folder1/) to identify specific objects to delete:" );
        String prefix = new BufferedReader( new InputStreamReader( System.in ) ).readLine();

        // create the AWS S3 Client
        AmazonS3 s3 = AWSS3Factory.getS3Client();

        // create the new rule
        BucketLifecycleConfiguration.Rule rule = new BucketLifecycleConfiguration.Rule()
                .withExpirationInDays(Integer.valueOf(days))
                .withId("rule-1")
                .withPrefix(prefix.trim())
                .withStatus(BucketLifecycleConfiguration.ENABLED.toString());

        // build the rule into configuration type
        BucketLifecycleConfiguration configuration = new BucketLifecycleConfiguration(Arrays.asList(rule));

        // save the lifecycle policy
        s3.setBucketLifecycleConfiguration(AWSS3Factory.S3_BUCKET, configuration);

        // Retrieve configuration.
        BucketLifecycleConfiguration result = s3.getBucketLifecycleConfiguration(AWSS3Factory.S3_BUCKET);
        System.out.println(String.format("bucket lifecycle configuration: prefix is %s and days is %s.",
                result.getRules().get(0).getPrefix(), result.getRules().get(0).getExpirationInDays()));

        //s3.deleteBucketLifecycleConfiguration(AWSS3Factory.S3_BUCKET);
    }

}
