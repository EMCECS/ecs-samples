using System;
using Amazon.S3;
using Amazon.S3.Model;

namespace aws_net_workshop.examples_bonus
{
    class _02_ListVersions
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            // create bucket versioning request
            GetBucketVersioningRequest gvr = new GetBucketVersioningRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET
            };

            // submit get bucket versioning request
            GetBucketVersioningResponse gvrResponse = s3.GetBucketVersioning(gvr);

            // print status
            Console.WriteLine(string.Format("Bucket versioning status: {0}",
                gvrResponse.VersioningConfig.Status));

            // enabled versioning if not yet enabled
            if (gvrResponse.VersioningConfig.Status != VersionStatus.Enabled)
            {
                Console.Write(string.Format("Enabling bucket versioning for bucket '{0}'... ", AWSS3Factory.S3_BUCKET));

                // create request to enable versioning on bucket
                PutBucketVersioningRequest pvr = new PutBucketVersioningRequest()
                {
                    BucketName = AWSS3Factory.S3_BUCKET,
                    VersioningConfig = new S3BucketVersioningConfig() { Status = VersionStatus.Enabled }
                };

                // submit request to enable versioning
                PutBucketVersioningResponse pvrResponse = s3.PutBucketVersioning(pvr);

                if (pvrResponse.HttpStatusCode != System.Net.HttpStatusCode.OK)
                {
                    Console.WriteLine("fail");
                    Console.ReadLine();
                    System.Environment.Exit(1);
                }
                Console.WriteLine("done");
            }

            Console.WriteLine("Getting object versions...");

            bool moreRecords = true;
            string nextMarker = string.Empty;
            string nextVersionId = string.Empty;
            while (moreRecords)
            {
                

                ListVersionsRequest request = new ListVersionsRequest()
                {
                    BucketName = AWSS3Factory.S3_BUCKET,
                };

                if (nextMarker.Length > 0)
                {
                    request.KeyMarker = nextMarker;
                }


                ListVersionsResponse response = new ListVersionsResponse();
                response = s3.ListVersions(request);


                foreach (S3ObjectVersion key in response.Versions)
                {
                    Console.WriteLine(string.Format("-> Object Key: {0} - VersionId: {1} - IsDelete: {2} - {3}",
                        key.Key,
                        key.VersionId,
                        key.IsDeleteMarker,
                        key.LastModified));
                }

                if (response.IsTruncated)
                {
                    Console.WriteLine(string.Format("Next Marker: {0}:{1}. Version Count: {2}",
                        response.NextKeyMarker,
                        response.NextVersionIdMarker,
                        response.Versions.Count.ToString()));

                    nextMarker = response.NextKeyMarker;
                    nextVersionId = response.NextVersionIdMarker;
                }
                else
                {
                    moreRecords = false;
                }
            }

            Console.ReadLine();
        }
    }
}
