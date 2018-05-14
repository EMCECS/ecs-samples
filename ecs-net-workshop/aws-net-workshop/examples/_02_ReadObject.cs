using System;
using System.IO;
using ECSSDK.S3;
using Amazon.S3.Model;

namespace ecs_net_workshop.examples
{
    class _02_ReadObject
    {
        public static void Main(string[] args)
        {
            // create the ECS S3 client
            ECSS3Client s3 = ECSS3Factory.getS3Client();

            // retrieve the key value from user
            Console.Write("Enter the object key: ");
            string key = Console.ReadLine();

            // create the request object
            GetObjectRequest request = new GetObjectRequest()
            {
                BucketName = ECSS3Factory.S3_BUCKET,
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
