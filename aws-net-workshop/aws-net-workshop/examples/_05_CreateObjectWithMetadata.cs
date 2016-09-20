using System;
using Amazon.S3;
using Amazon.S3.Model;

namespace aws_net_workshop.examples
{
    class _05_CreateObjectWithMetadata
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 Client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            // retrieve the object key and object content from the user
            Console.Write("Enter the object key: ");
            string key = Console.ReadLine();
            Console.Write("Enter the object content: ");
            string content = Console.ReadLine();

            // retrieve the object metadata key and value from user
            Console.Write("Enter the metadata key: ");
            string metaKey = Console.ReadLine();
            Console.Write("Enter the metadata value: ");
            string metaValue = Console.ReadLine();

            // create object request with retrieved input
            PutObjectRequest request = new PutObjectRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET,
                ContentBody = content,
                Key = key
            };

            // add metadata to request
            request.Metadata.Add(metaKey, metaValue);

            // create the object with metadata in the demo bucket
            s3.PutObject(request);

            // print out object key/value and metadata key/value for validation
            Console.WriteLine(string.Format("Create object {0}/{1} with metadata {2}={3} and content: {4}", 
                AWSS3Factory.S3_BUCKET, key, metaKey, metaValue, content));
            Console.ReadLine();
        }
    }
}
