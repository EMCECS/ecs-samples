using System;
using ECSSDK.S3;
using Amazon.S3.Model;

namespace ecs_net_workshop.examples
{
    class _20_DeleteBucket
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 client
            ECSS3Client s3 = ECSS3Factory.getS3Client();

            bool moreRecords = true;
            string nextMarker = string.Empty;

            while (moreRecords)
            {
                ListVersionsRequest request = new ListVersionsRequest()
                {
                    BucketName = ECSS3Factory.S3_BUCKET,
                };

                if (nextMarker.Length > 0)
                    request.KeyMarker = nextMarker;

                ListVersionsResponse response = new ListVersionsResponse();
                response = s3.ListVersions(request);


                foreach (S3ObjectVersion theObject in response.Versions)
                {
                    s3.DeleteObject(new DeleteObjectRequest()
                    {
                        BucketName = ECSS3Factory.S3_BUCKET,
                        Key = theObject.Key,
                        VersionId = theObject.VersionId
                    });
                    Console.WriteLine("Deleted {0}/{1}", ECSS3Factory.S3_BUCKET, theObject.Key);
                }

                Console.WriteLine(string.Format("Next Marker: {0} Version Count: {1}", response.NextKeyMarker, response.Versions.Count.ToString()));

                if (response.IsTruncated)
                {
                    nextMarker = response.NextKeyMarker;
                }
                else
                {
                    moreRecords = false;
                }
            }

            

            s3.DeleteBucket(new DeleteBucketRequest()
            {
                BucketName = ECSS3Factory.S3_BUCKET
            });

            // print bucket name for validation
            Console.WriteLine(string.Format("Deleted bucket {0}", ECSS3Factory.S3_BUCKET));
            Console.ReadLine();
        }
    }
}
