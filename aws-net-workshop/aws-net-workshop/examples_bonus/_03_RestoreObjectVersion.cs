using System;
using Amazon.S3;
using Amazon.S3.Model;

namespace aws_net_workshop.examples_bonus
{
    class _03_RestoreObjectVersion
    {
        public static void Main(string[] args)
        {
            /*
             * This code sample will do the following:
             * 
             *   1. Enable object versioning in the bucket
             *   2. Create a new object (version 1)
             *   3. Delete the object. As versioning is enabled, it will actually create a version 2 with a delete marker
             *   4. Try to get the object, which will fail and return a 404 Not Found
             *   5. List the versions of the object and obtain the version ID of the first version
             *   6. Restore the first version using a server-side copy operation (no need to upload the object content again). Will create a new version 3 of the object.
             *   7. Verify that the object can be successfully obtained
             */

            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            System.Net.ServicePointManager.ServerCertificateValidationCallback = ((sender, certificate, chain, sslPolicyErrors) => true);

            Console.Write(string.Format("Enabling bucket versioning for bucket '{0}'... ", AWSS3Factory.S3_BUCKET));

            PutBucketVersioningRequest pvr = new PutBucketVersioningRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET,
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

            String objectKey = "object-restore-" + DateTime.Now.ToString("yyyyMMddHHmmssffff");

            Console.Write(string.Format("Creating a new object with key '{0}'... ", objectKey));

            PutObjectRequest poRequest = new PutObjectRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET,
                ContentBody = "Object content...",
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

            Console.Write(string.Format("Deleting object with key '{0}' (setting a deletion marker)... ", objectKey));

            DeleteObjectRequest doRequest = new DeleteObjectRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET,
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

            Console.Write(string.Format("Trying to read object with key '{0}' (latest version)... ", objectKey));

            GetObjectRequest goRequest = new GetObjectRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET,
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
            Console.WriteLine("object not found");

            Console.WriteLine(string.Format("Listing versions for object '{0}'... ", objectKey));

            ListVersionsResponse lvResponse = s3.ListVersions(AWSS3Factory.S3_BUCKET);

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
                    continue;
                }

                Console.WriteLine(string.Format("-> VersionId: {0} - IsDeleteMarker: {1} - Timestamp: {2}",
                     version.VersionId,
                     version.IsDeleteMarker,
                     version.LastModified));

                if (!version.IsDeleteMarker) {
                    restoreVersion = version.VersionId;
                }
            }

            if (restoreVersion.Length == 0)
            {
                Console.WriteLine("Could not find a version to restore");
                Console.ReadLine();
                System.Environment.Exit(1);
            }

            Console.Write(string.Format("Restoring object version ID '{0}' (server-side copy)... ", restoreVersion));

            CopyObjectRequest coRequest = new CopyObjectRequest()
            {
                SourceBucket = AWSS3Factory.S3_BUCKET,
                SourceKey = objectKey,
                SourceVersionId = restoreVersion,
                DestinationBucket = AWSS3Factory.S3_BUCKET,
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

            Console.Write(string.Format("Trying to read object '{0}'... ", objectKey));

            GetObjectResponse goResponse = s3.GetObject(goRequest);

            if (coResponse.HttpStatusCode != System.Net.HttpStatusCode.OK || goResponse.ContentLength != poRequest.ContentBody.Length)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
        
            Console.WriteLine("done");
            Console.WriteLine(string.Format("-> Object '{0}' successfully restored. New VersionId: '{1}'", goResponse.Key, goResponse.VersionId));

            Console.ReadLine();
        }
    }
}
