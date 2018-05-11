using System;
using System.IO;
using Amazon.S3.Model;
using ECSSDK.S3;
using ECSSDK.S3.Model;
using ECSSDK.S3.Model.Util;

namespace ecs_net_workshop.examples
{
    class _11_UpdateAppendExtensions
    {
        public static void Main(string[] args)
        {
            ECSS3Client s3 = ECSS3Factory.getS3Client();
            PutObjectRequestECS request;

            string key = "update-append.txt";                   // object key to create, update, and append
            string bucketName = ECSS3Factory.S3_BUCKET;         // bucket to create object in
            string content = "The tan fox jumped over the dog"; // initial object content
            int tanIndex = content.IndexOf("tan");

            Console.WriteLine(string.Format("creating initial object {0}/{1} with content: {2}", ECSS3Factory.S3_BUCKET, key, content));

            // first create an initial object
            request = new PutObjectRequestECS();
            request.BucketName = bucketName;
            request.Key = key;
            request.ContentBody = content;
            s3.PutObject(request);

            // read object and print content
            Console.WriteLine(string.Format("initial object {0}/{1} with content: [{2}]", bucketName, key, readObject(s3, bucketName, key)));

            // update the object in the middle
            string content2 = "red";
            request = new PutObjectRequestECS();
            request.BucketName = bucketName;
            request.Key = key;
            request.ContentBody = content2;
            request.Range = Range.fromOffsetLength(tanIndex, content2.Length);
            Console.WriteLine(string.Format("updating object at offset {0}", tanIndex));
            s3.PutObject(request);

            // read object and print content
            Console.WriteLine(string.Format("updated object {0}/{1} with content: [{2}]", bucketName, key, readObject(s3, bucketName, key)));

            // append to the object
            string content3 = " and cat";
            Console.WriteLine(string.Format("appending object at offset {0}", content.Length));
            request = new PutObjectRequestECS();
            request.BucketName = bucketName;
            request.Key = key;
            request.ContentBody = content3;
            request.Range = Range.fromOffset(content.Length);
            s3.PutObject(request);

            // read object and print content
            Console.WriteLine(string.Format("appended object {0}/{1} with content: [{2}]", bucketName, key, readObject(s3, bucketName, key)));

            // create a sparse object by appending past the end of the object
            string content4 = "#last byte#";
            Console.WriteLine(string.Format("sparse append object at offset {0}", 45));
            request = new PutObjectRequestECS();
            request.BucketName = bucketName;
            request.Key = key;
            request.ContentBody = content4;
            request.Range = Range.fromOffset(45);
            s3.PutObject(request);

            // read object and print content
            Console.WriteLine(string.Format("sparse append object {0}/{1} with content: [{2}]", bucketName, key, readObject(s3, bucketName, key)));

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
