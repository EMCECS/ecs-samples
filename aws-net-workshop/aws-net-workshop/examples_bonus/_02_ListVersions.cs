using System;
using Amazon.S3;
using Amazon.S3.Model;

namespace aws_net_workshop.examples_bonus
{
    /// <summary>
    /// Listing object versions in a bucket.
    /// </summary>
    /// <remarks>
    /// When object versioning is enabled on a bucket, ECS will automatically add
    /// a unique version ID to every object uploaded.
    /// Each request to list object versions returns up to 1000 versions. If you
    /// have more, you will need to send a series of requests to retrieve a list
    /// of all versions. To illustrate how pagination works, the code examples
    /// limit the response to two object versions. If there are more than two
    /// object versions in the bucket, the response returns the IsTruncated element
    /// with the value "true" and also includes the NextKeyMarker and
    /// NextVersionIdMarker elements whose values you can use to retrieve the next
    /// set of object keys. The code example includes these values in the subsequent
    /// request to retrieve the next set of objects.
    ///
    /// This sample will go through the following steps:
    ///
    ///   1. Create a bucket
    ///   2. Get current bucket versioning status
    ///   3. Enable object versioning on the bucket
    ///   4. Upload three object with three versions each (Total of 9 versions)
    ///   5. List the object versions in the bucket
    ///   6. Delete all object versions (permanently)
    ///   7. Delete the bucket
    ///
    /// </remarks>
    class _02_ListVersions
    {
        public static void Main(string[] args)
        {
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

            //*******************************************//
            // 2. Get current bucket versioning status   //
            //*******************************************//

            Console.Write(string.Format(" [*] Getting bucket versioning status for bucket '{0}'... ", bucketName));

            GetBucketVersioningRequest gvr = new GetBucketVersioningRequest()
            {
                BucketName = bucketName
            };

            GetBucketVersioningResponse gvrResponse = s3.GetBucketVersioning(gvr);
            
            Console.Write(string.Format("status: {0}",
                gvrResponse.VersioningConfig.Status));

            //*******************************************//
            // 3. Enable object versioning on the bucket //
            //*******************************************//

            // enabled versioning if not yet enabled
            if (gvrResponse.VersioningConfig.Status != VersionStatus.Enabled)
            {
                Console.Write(string.Format(" [*] Enabling bucket versioning for bucket '{0}'... ", bucketName));

                PutBucketVersioningRequest pvr = new PutBucketVersioningRequest()
                {
                    BucketName = bucketName,
                    VersioningConfig = new S3BucketVersioningConfig() { Status = VersionStatus.Enabled }
                };

                PutBucketVersioningResponse pvrResponse = s3.PutBucketVersioning(pvr);

                if (pvrResponse.HttpStatusCode != System.Net.HttpStatusCode.OK)
                {
                    Console.WriteLine("fail");
                    Console.ReadLine();
                    System.Environment.Exit(1);
                }
                Console.WriteLine("done");
            }

            //***********************************************************************//
            // 4. Upload three object with three versions each (Total of 9 versions) //
            //***********************************************************************//

            Console.Write(string.Format(" [*] Uploading 3 objects with 3 versions each to bucket '{0}'... ", bucketName));

            for (int i=0; i<3; i++)
            {
                string objectKey = String.Format("object-{0}", i);

                for (int j=0; j<3; j++)
                {
                    string objectContent = String.Format("This is object {0}, revision {1}", i, j);
                    PutObjectRequest poRequest = new PutObjectRequest()
                    {
                        BucketName = bucketName,
                        Key = objectKey,
                        ContentBody = objectContent
                    };

                    PutObjectResponse poResponse = s3.PutObject(poRequest);
                    
                    if (poResponse.HttpStatusCode != System.Net.HttpStatusCode.OK)
                    {
                        Console.WriteLine("fail");
                        Console.ReadLine();
                        System.Environment.Exit(1);
                    }

                    Console.Write(".");
                }
            }

            Console.WriteLine("done");

            //*******************************************//
            // 5. List the object versions in the bucket //
            //*******************************************//

            Console.WriteLine(" [*] Listing object versions...");

            ListVersionsRequest request = new ListVersionsRequest()
            {
                BucketName = bucketName,
                // You can optionally specify key name prefix in the request
                // if you want list of object versions of a specific object.

                // For this example we limit response to return list of 2 versions.
                MaxKeys = 2
            };

            bool moreRecords = true;
            while (moreRecords)
            {
                ListVersionsResponse response = s3.ListVersions(request);
               
                foreach (S3ObjectVersion version in response.Versions)
                {
                    Console.WriteLine(string.Format(" [x]     -> Object key: {0}", version.Key));
                    Console.WriteLine(string.Format(" [x]           VersionId: {0}", version.VersionId));
                    Console.WriteLine(string.Format(" [x]           IsDeleteMarker: {0}", version.IsDeleteMarker));
                    Console.WriteLine(string.Format(" [x]           LastModified: {0}", version.LastModified));
                }

                // If response is truncated, set the marker to get the next 
                // set of keys.
                if (response.IsTruncated)
                {
                    request.KeyMarker = response.NextKeyMarker;
                    request.VersionIdMarker = response.NextVersionIdMarker;
                }
                else
                {
                    moreRecords = false;
                }

                Console.WriteLine(string.Format(" [x] More records? {0}", moreRecords));
            }

            //*******************************************//
            // 6. Permanently delete the object versions //
            //*******************************************//

            Console.Write(" [*] Permanently deleting all object versions... ");

            ListVersionsResponse lv2Response = s3.ListVersions(bucketName);

            if (lv2Response.HttpStatusCode != System.Net.HttpStatusCode.OK)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }

            foreach (S3ObjectVersion version in lv2Response.Versions)
            {
                DeleteObjectRequest do2Request = new DeleteObjectRequest()
                {
                    BucketName = bucketName,
                    Key = version.Key,
                    VersionId = version.VersionId
                };

                DeleteObjectResponse do2Response = s3.DeleteObject(do2Request);

                if (do2Response.HttpStatusCode != System.Net.HttpStatusCode.NoContent)
                {
                    Console.WriteLine("fail");
                    Console.ReadLine();
                    System.Environment.Exit(1);
                }
            }

            Console.WriteLine("done");

            //***********************//
            // 7. Delete the bucket //
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
