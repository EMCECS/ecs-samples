using System;
using Amazon.S3;
using Amazon.S3.Model;
using System.IO;

namespace aws_net_workshop.examples_bonus
{
    /// <summary>
    /// Restoring Previous Object Versions.
    /// </summary>
    /// <remarks>
    /// When object versioning is enabled on a bucket, ECS will automatically add
    /// a unique version ID to every object uploaded.
    /// Because all object versions are preserved, you can make any earlier version
    /// the current version by copying a specific version of the object into the
    /// same bucket. When versioning is enabled, a DELETE operation will not
    /// permanently delete an object. Instead, ECS inserts a delete marker in the
    /// bucket, and that marker becomes the current version of the object with a
    /// new ID. When you try to GET an object whose current version is a delete
    /// marker, ECS behaves as though the object has been deleted (even though it
    /// has not been erased) and returns a 404 error.
    /// To permanently delete an object, you need to send a DELETE request to
    /// every version in that object.
    ///
    /// This sample will go through the following steps:
    ///
    ///   1. Create a bucket
    ///   2. Enable object versioning on the bucket
    ///   3. Create a new object (version 1)
    ///   4. Delete the object. As versioning is enabled, it will actually create
    ///      a version 2 with a delete marker
    ///   5. Try to get the object, which will fail and return a 404 Not Found
    ///   6. List the versions of the object and obtain the version ID of the
    ///      first version
    ///   7. Restore the first version using a server-side copy operation (no need
    ///      to upload the object content again). This will create a new version 3
    ///      of the object.
    ///   8. Verify that the object can now be successfully obtained
    ///   9. Permanently delete the object versions
    ///  10. Delete the bucket
    ///
    /// </remarks>
    class _03_RestoreObjectVersion
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

            //*******************************************//
            // 2. Enable object versioning on the bucket //
            //*******************************************//

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

            //************************************//
            // 3. Create a new object (version 1) //
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
            Console.WriteLine(string.Format(" [x] Object content: '{0}'", poRequest.ContentBody));

            //****************************************//
            // 4. Delete the object (deletion marker) //
            //****************************************//

            Console.Write(string.Format(" [*] Deleting object with key '{0}' (adding a deletion marker)... ", objectKey));

            DeleteObjectRequest doRequest = new DeleteObjectRequest()
            {
                BucketName = bucketName,
                Key = objectKey
            };

            DeleteObjectResponse doResponse =  s3.DeleteObject(doRequest);

            if (doResponse.HttpStatusCode != System.Net.HttpStatusCode.NoContent || doResponse.DeleteMarker != "true")
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");

            //*************************************************//
            // 5. Try to get the object (expect 404 Not Found) //
            //*************************************************//

            Console.Write(string.Format(" [*] Trying to read object with key '{0}' (expecting 404 Not Found)... ", objectKey));

            GetObjectRequest goRequest = new GetObjectRequest()
            {
                BucketName = bucketName,
                Key = objectKey,
            };

            try
            {
                // should throw an exception as the object is marked as deleted
                s3.GetObject(goRequest);
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            catch (AmazonS3Exception e) {
                if (e.StatusCode != System.Net.HttpStatusCode.NotFound)
                {
                    Console.WriteLine("fail");
                    Console.ReadLine();
                    System.Environment.Exit(1);
                }
            }
            Console.WriteLine("done (404 Not Found)");

            //*************************************************************************//
            // 6. List the object versions and get the version ID of the first version //
            //*************************************************************************//

            Console.WriteLine(string.Format(" [*] Listing object versions for bucket '{0}' and getting version ID to restore... ", bucketName));

            ListVersionsResponse lvResponse = s3.ListVersions(bucketName);

            if (lvResponse.HttpStatusCode != System.Net.HttpStatusCode.OK)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }

            String restoreVersion = String.Empty;

            foreach (S3ObjectVersion version in lvResponse.Versions)
            {
                if (version.Key != objectKey)
                {
                    // filtering out other objects
                    continue;
                }

                Console.WriteLine(string.Format(" [x]     -> Object key: {0}", version.Key));
                Console.WriteLine(string.Format(" [x]           VersionId: {0}", version.VersionId));
                Console.WriteLine(string.Format(" [x]           IsDeleteMarker: {0}", version.IsDeleteMarker));
                Console.WriteLine(string.Format(" [x]           LastModified: {0}", version.LastModified));

                if (!version.IsDeleteMarker) {
                    restoreVersion = version.VersionId;
                }
            }

            if (restoreVersion.Length == 0)
            {
                Console.WriteLine(" [*] Could not find a version to restore, exiting...");
                Console.ReadLine();
                System.Environment.Exit(1);
            }

            //******************************************************************//
            // 7. Restore the first version using a server-side copy operation. //
            //******************************************************************//

            Console.Write(string.Format(" [*] Restoring object version ID '{0}' (server-side copy)... ", restoreVersion));

            CopyObjectRequest coRequest = new CopyObjectRequest()
            {
                SourceBucket = bucketName,
                SourceKey = objectKey,
                SourceVersionId = restoreVersion,
                DestinationBucket = bucketName,
                DestinationKey = objectKey
            };

            CopyObjectResponse coResponse = s3.CopyObject(coRequest);

            if (coResponse.HttpStatusCode != System.Net.HttpStatusCode.OK)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");

            //************************************************************//
            // 8. Verify that the object can now be successfully obtained //
            //************************************************************//

            Console.Write(string.Format(" [*] Trying to read object '{0}'... ", objectKey));

            GetObjectResponse goResponse = s3.GetObject(goRequest);

            if (goResponse.HttpStatusCode != System.Net.HttpStatusCode.OK || goResponse.ContentLength != poRequest.ContentBody.Length)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }

            Console.WriteLine("done");

            String responseBody = "";
            using (Stream responseStream = goResponse.ResponseStream)
            using (StreamReader reader = new StreamReader(responseStream))
            {
                responseBody = reader.ReadToEnd();
            }

            Console.WriteLine(string.Format(" [x] Object '{0}' successfully restored. New VersionId: '{1}'. Content: '{2}'", 
                goResponse.Key, 
                goResponse.VersionId,
                responseBody));

            //*******************************************//
            // 9. Permanently delete the object versions //
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

                if(do2Response.HttpStatusCode != System.Net.HttpStatusCode.NoContent)
                {
                    Console.WriteLine("fail");
                    Console.ReadLine();
                    System.Environment.Exit(1);
                }
            }

            Console.WriteLine("done");

            //***********************//
            // 10. Delete the bucket //
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
