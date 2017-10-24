using System;
using Amazon.S3;
using Amazon.S3.Model;
using System.IO;
using System.Collections.Generic;

namespace aws_net_workshop.examples_bonus
{
    /// <summary>
    /// Obtaining object size and other metadata.
    /// </summary>
    /// <remarks>
    /// 
    /// The HEAD operation retrieves metadata from an object without returning the object 
    /// itself. This operation is useful if you're only interested in an object's metadata. 
    /// To use HEAD, you must have READ access to the object.
    /// 
    /// In this example we will retrieve the size of an object and other useful metadata 
    /// without downloading the object.
    ///
    /// This sample will go through the following steps:
    ///
    ///   1. Create a bucket
    ///   2. Upload an object
    ///   3. Get the metadata of the object by sending a HEAD request and obtain the object size
    ///   4. Delete the object
    ///   5. Delete the bucket
    ///
    /// </remarks>
    class _05_GetObjectSize
    {
        public static void Main(string[] args)
        {
            System.Net.ServicePointManager.ServerCertificateValidationCallback = ((sender, certificate, chain, sslPolicyErrors) => true);

            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            String bucketName = String.Join("-", AWSS3Factory.S3_BUCKET, DateTime.Now.ToString("yyyyMMddHHmmss"));

            //********************//
            // 1. Create a bucket //
            //********************//

            Console.Write(string.Format(" [*] Creating bucket '{0}'... ", bucketName));

            PutBucketResponse pbRes = s3.PutBucket(bucketName);
            if (pbRes.HttpStatusCode != System.Net.HttpStatusCode.OK)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");

            //************************************//
            // 2. Create and upload object        //
            //************************************//

            String objectKey = "object-" + DateTime.Now.ToString("yyyyMMddHHmmssffff");

            Console.Write(string.Format(" [*] Creating a new object with key '{0}'... ", objectKey));

            PutObjectRequest poRequest = new PutObjectRequest()
            {
                BucketName = bucketName,
                ContentBody = "Lorem ipsum dolor sit amet, consectetur adipiscing elit...",
                Key = objectKey
            };

            PutObjectResponse poResponse = s3.PutObject(poRequest);

            if (poResponse.HttpStatusCode != System.Net.HttpStatusCode.OK)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");

            //****************************************//
            // 3. Obtain object metadata              //
            //****************************************//

            Console.Write(string.Format(" [*] Obtaining object size and other metadata for object '{0}'... ", objectKey));

            GetObjectMetadataRequest request = new GetObjectMetadataRequest()
            {
                BucketName = bucketName,
                Key = objectKey
            };

            // get object metadata - not actual content (HEAD request not GET).
            GetObjectMetadataResponse gomResponse = s3.GetObjectMetadata(request);

            if (gomResponse.HttpStatusCode != System.Net.HttpStatusCode.OK)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }

            Console.WriteLine("done");

            // Obtain the object size in Bytes
            long objectSize = gomResponse.Headers.ContentLength;

            Console.WriteLine(string.Format(" [x] Object size is: {0} bytes", objectSize));

            Console.WriteLine(" [x] Other headers and metadata:");

            ICollection<string> headers = gomResponse.Headers.Keys;
            foreach (string header in headers)
            {
                Console.WriteLine("[x]     {0}: {1}", header, gomResponse.Headers[header]);
            }

            ICollection<string> metaKeys = gomResponse.Metadata.Keys;
            foreach (string metaKey in metaKeys)
            {
                Console.WriteLine("[x]     {0}: {1}", metaKey, gomResponse.Metadata[metaKey]);
            }

            //*******************************************//
            // 4. Delete the object                      //
            //*******************************************//

            Console.Write(string.Format(" [*] Deleting object '{0}'... ", objectKey));

            // create the request object
            DeleteObjectRequest doRequest = new DeleteObjectRequest()
            {
                BucketName = bucketName,
                Key = objectKey
            };

            // delete the object in the demo bucket
            DeleteObjectResponse doResponse = s3.DeleteObject(doRequest);

            if (doResponse.HttpStatusCode != System.Net.HttpStatusCode.NoContent)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            

            Console.WriteLine("done");

            //***********************//
            // 5. Delete the bucket  //
            //***********************//

            Console.Write(String.Format(" [*] Deleting bucket '{0}' (sleeping 5 seconds)... ", bucketName));

            System.Threading.Thread.Sleep(5000);

            DeleteBucketResponse dbRes = s3.DeleteBucket(bucketName);

            if (dbRes.HttpStatusCode != System.Net.HttpStatusCode.NoContent)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");

            Console.WriteLine(" [*] Example is completed. Press any key to exit...");
            Console.ReadLine();
        }
    }
}
