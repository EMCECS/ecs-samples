using System;
using Amazon.S3.Model;
using ECSSDK.S3;
using ECSSDK.S3.Model;
using ECSSDK.S3.Model.Util;

namespace ecs_net_workshop
{
    class __05__PutObjectECS
    {
        static ECSS3Client client = ECSS3Factory.getECSS3Client();
        static string temp_bucket = ECSS3Factory.S3_BUCKET;

        public static void UpdateObjectWithRange()
        {
            string key = "key-1";
            string content = "The cat crossed the road.";
            int offset = content.IndexOf("cat");

            PutObjectRequestECS por = new PutObjectRequestECS()
            {
                BucketName = temp_bucket,
                Key = key,
                ContentBody = content
            };

            // create the object
            client.PutObject(por);

            string updatePart = "dog";

            por = new PutObjectRequestECS()
            {
                BucketName = temp_bucket,
                Key = key,
                Range = Range.fromOffsetLength(offset, updatePart.Length),
                ContentBody = updatePart
            };

            // update the object
            client.PutObject(por);
        }

        public static void ConditionalPut()
        {
            string key = "key-1";
            string content = "testing a conditional PUT";

            DateTime in_the_past = TimeZoneInfo.ConvertTimeToUtc(DateTime.Now.AddMinutes(-5));
            DateTime in_the_future = TimeZoneInfo.ConvertTimeToUtc(DateTime.Now.AddMinutes(10));

            PutObjectRequestECS por = new PutObjectRequestECS()
            {
                BucketName = temp_bucket,
                Key = key,
                ContentBody = content,
                ContentType = "text/plain"
            };

            PutObjectResponse response = client.PutObject(por);

            string eTag = response.ETag;

            por.UnmodifiedSinceDate = in_the_past;

            client.PutObject(por);
        }

        public static void PutObjectRetentionPolicy()
        {
            string key = "retention-1";
            string content = "sample retention content ...";

            PutObjectRequestECS por = new PutObjectRequestECS()
            {
                BucketName = temp_bucket,
                Key = key,
                RetentionPolicy = "hold-me",
                ContentBody = content
            };

            client.PutObject(por);
        }

        public static void PutObjectRetentionPeriod()
        {
            string key = "retention-1";
            string content = "sample retention content ...";

            PutObjectRequestECS por = new PutObjectRequestECS()
            {
                BucketName = temp_bucket,
                Key = key,
                RetentionPeriod = 5,
                ContentBody = content
            };

            client.PutObject(por);

        }
    }
}
