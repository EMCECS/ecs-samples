using System;
using Amazon.S3;
using Amazon.S3.Model;

namespace aws_net_workshop.examples
{
    class _11_DeleteBucket
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            bool moreRecords = true;
            string nextMarker = string.Empty;

            while (moreRecords)
            {
                ListVersionsRequest request = new ListVersionsRequest()
                {
                    BucketName = AWSS3Factory.S3_BUCKET,
                };

                if (nextMarker.Length > 0)
                    request.KeyMarker = nextMarker;

                ListVersionsResponse response = new ListVersionsResponse();
                response = s3.ListVersions(request);


                foreach (S3ObjectVersion theObject in response.Versions)
                {
                    s3.DeleteObject(new DeleteObjectRequest()
                    {
                        BucketName = AWSS3Factory.S3_BUCKET,
                        Key = theObject.Key,
                        VersionId = theObject.VersionId
                    });
                    Console.WriteLine("Deleted {0}/{1}", AWSS3Factory.S3_BUCKET, theObject.Key);
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
                BucketName = AWSS3Factory.S3_BUCKET
            });

            // print bucket name for validation
            Console.WriteLine(string.Format("Deleted bucket {0}", AWSS3Factory.S3_BUCKET));
            Console.ReadLine();
        }
    }
}
