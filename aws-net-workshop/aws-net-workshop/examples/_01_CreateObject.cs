using System;
using Amazon.S3;
using Amazon.S3.Model;

namespace aws_net_workshop.examples
{
    class _01_CreateObject
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            // retrieve the object key and object content from the user
            Console.Write("Enter the object key: ");
            string key = Console.ReadLine();
            Console.Write("Enter the object content: ");
            string content = Console.ReadLine();

            // create object request with retrieved input
            PutObjectRequest request = new PutObjectRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET,
                ContentBody = content,
                Key = key
            };

            // create the object in demo bucket
            s3.PutObject(request);
           

            // print out object key/value for validation
            Console.WriteLine(string.Format("Created object {0}/{1} with content: {2}", AWSS3Factory.S3_BUCKET, key, content));
            Console.ReadLine();
        }
    }
}
