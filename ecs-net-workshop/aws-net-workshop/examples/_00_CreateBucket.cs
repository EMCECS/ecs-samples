using System;
using ECSSDK.S3;
using ECSSDK.S3.Model;
using Amazon.S3.Model;

namespace ecs_net_workshop.examples
{
    public class _00_CreateBucket
    {
        public static void Main(string[] args)
        {
            // create the ECS S3 client
            ECSS3Client s3 = ECSS3Factory.getS3Client();

            // create bucket request
            PutBucketRequestECS request = new PutBucketRequestECS()
            {
                BucketName = ECSS3Factory.S3_BUCKET
            };

            // create bucket - used for subsequent demos
            s3.PutBucket(request);

            // create bucket lising request
            ListObjectsRequest objects = new ListObjectsRequest()
            {
                BucketName = ECSS3Factory.S3_BUCKET
            };

            // get bucket lising to retrieve bucket name
            ListObjectsResponse result = s3.ListObjects(objects);

            // print bucket name for validation
            Console.WriteLine(string.Format("Successfully created bucket {0}.", result.Name));
            Console.ReadLine();
        }
    }
}
