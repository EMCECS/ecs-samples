using System;
using ECSSDK.S3;
using ECSSDK.S3.Model;


namespace ecs_net_workshop
{
    class __02__PutBucket
    {
        static ECSS3Client client = ECSS3Factory.getECSS3Client();
        static string temp_bucket = ECSS3Factory.S3_BUCKET;

        public static void PutBucketFileSystemEnabled()
        {
            PutBucketRequestECS request = new PutBucketRequestECS()
            {
                BucketName = temp_bucket,
                EnableFileSystem = true
            };

            PutBucketResponseECS response = client.PutBucket(request);
        }

        public static void PutBucketWithExtensions()
        {
            string bucket = Guid.NewGuid().ToString();

            PutBucketRequestECS request = new PutBucketRequestECS()
            {
                BucketName = bucket,
                RetentionPeriod = 180,
                StaleReadAllowed = true,
                EnableCompliance = true,
                EnableServerSideEncryption = true,
                Namespace = "ECS_NAMESPACE",
            };
        }
    }
}
