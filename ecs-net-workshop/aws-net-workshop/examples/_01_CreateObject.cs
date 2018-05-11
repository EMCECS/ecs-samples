using System;
using ECSSDK.S3;
using ECSSDK.S3.Model;

namespace ecs_net_workshop.examples
{
    class _01_CreateObject
    {
        public static void Main(string[] args)
        {
            // create the ECS S3 client
            ECSS3Client s3 = ECSS3Factory.getS3Client();

            // retrieve the object key and object content from the user
            Console.Write("Enter the object key: ");
            string key = Console.ReadLine();
            Console.Write("Enter the object content: ");
            string content = Console.ReadLine();

            // create object request with retrieved input
            PutObjectRequestECS request = new PutObjectRequestECS()
            {
                BucketName = ECSS3Factory.S3_BUCKET,
                ContentBody = content,
                Key = key
            };

            // create the object in demo bucket
            s3.PutObject(request);           

            // print out object key/value for validation
            Console.WriteLine(string.Format("Created object {0}/{1} with content: {2}", ECSS3Factory.S3_BUCKET, key, content));
            Console.ReadLine();
        }
    }
}