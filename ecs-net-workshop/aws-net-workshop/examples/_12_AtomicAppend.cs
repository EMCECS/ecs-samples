using System;
using System.IO;
using Amazon.S3.Model;
using ECSSDK.S3;
using ECSSDK.S3.Model;

namespace ecs_net_workshop.examples
{
    class _12_AtomicAppend
    {
        public static void Main(string[] args)
        {
            // create the ECS S3 client
            ECSS3Client s3 = ECSS3Factory.getS3Client();

            // object key to create, update, and append
            string key = "atomic-append.txt";
            string bucketName = ECSS3Factory.S3_BUCKET; // bucket to create object in
            string content = "Hello World!";

            // first create an initial object
            Console.WriteLine(string.Format("creating initial object {0}/{1} with content: {2}", ECSS3Factory.S3_BUCKET, key, content));

            PutObjectRequestECS request = new PutObjectRequestECS() {
                BucketName = bucketName,
                Key = key,
                ContentBody = content
            };
            s3.PutObject(request);

            // read object and print content
            Console.WriteLine(string.Format("initial object {0}/{1} with content: [{2}]", bucketName, key, readObject(s3, bucketName, key)));

            // append to the end of the object
            string content2 = " ... and Universe!!";

            // the offset at which our appended data was written is returned
            // (this is the previous size of the object)
            long appendOffset = s3.AppendObject(bucketName, key, content2);

            Console.WriteLine(string.Format("append successful at offset {0}", appendOffset));

            // read object and print content
            Console.WriteLine(string.Format("final object {0}/{1} with content: [{2}]", bucketName, key, readObject(s3, bucketName, key)));

            Console.ReadLine();

        }

        private static string readObject(ECSS3Client s3, string bucketName, string key)
        {
            GetObjectResponse response = s3.GetObject(bucketName, key);
            Stream responseStream = response.ResponseStream;
            StreamReader reader = new StreamReader(responseStream);
            return reader.ReadToEnd();
        }
    }
}
