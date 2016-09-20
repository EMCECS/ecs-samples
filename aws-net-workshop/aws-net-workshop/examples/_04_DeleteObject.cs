using System;
using Amazon.S3;
using Amazon.S3.Model;

namespace aws_net_workshop.examples
{
    class _04_DeleteObject
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            // retrieve the object key from user
            Console.Write("Enter the object key: ");
            string key = Console.ReadLine();

            // create the request object
            DeleteObjectRequest request = new DeleteObjectRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET,
                Key = key
            };

            // delete the object in the demo bucket
            s3.DeleteObject(request);


            // print out object key/value for validation
            Console.WriteLine(string.Format("Object {0}/{1} deleted", AWSS3Factory.S3_BUCKET, key));
            Console.ReadLine();
        }
    }
}
