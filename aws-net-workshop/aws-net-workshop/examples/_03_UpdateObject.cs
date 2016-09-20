using System;
using Amazon.S3;
using Amazon.S3.Model;

namespace aws_net_workshop.examples
{
    class _03_UpdateObject
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            // retrieve the object key and new object value from the user
            Console.Write("Enter the object key: ");
            string key = Console.ReadLine();
            Console.Write("Enter new object content: ");
            string content = Console.ReadLine();

            // create the request object
            PutObjectRequest request = new PutObjectRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET,
                ContentBody = content,
                Key = key
            };

            // update the object in the demo bucket
            s3.PutObject(request);

            // print out object key/value for validation
            Console.WriteLine(string.Format("Updated object {0}/{1} with content: {2}", AWSS3Factory.S3_BUCKET, key, content));
            Console.ReadLine();
        }
    }
}
