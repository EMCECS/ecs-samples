using System;
using System.IO;
using Amazon.S3;
using Amazon.S3.Model;

namespace aws_net_workshop.examples
{
    class _02_ReadObject
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            // retrieve the key value from user
            Console.Write("Enter the object key: ");
            string key = Console.ReadLine();

            // create the request object
            GetObjectRequest request = new GetObjectRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET,
                Key = key,
            };

            // read the object from the demo bucket
            GetObjectResponse response = s3.GetObject(request);

            // convert the object to a text string
            Stream responseStream = response.ResponseStream;
            StreamReader reader = new StreamReader(responseStream);
            string content = reader.ReadToEnd();

            // print object key/value and content for validation
            Console.WriteLine(string.Format("Object {0} contents: {1}", response.Key, content));
            Console.ReadLine();
        }
    }
}
