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

            System.Net.ServicePointManager.ServerCertificateValidationCallback = ((sender, certificate, chain, sslPolicyErrors) => true);

            GetBucketVersioningRequest gvr = new GetBucketVersioningRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET
            };

            Console.WriteLine(s3.GetBucketVersioning(gvr).VersioningConfig.Status);


            bool moreRecords = true;
            string nextMarker = string.Empty;
            while (moreRecords)
            {
                

                ListVersionsRequest request = new ListVersionsRequest()
                {
                    BucketName = AWSS3Factory.S3_BUCKET,
                };

                //if (nextMarker.Length > 0)
                //request.KeyMarker = nextMarker;


                request.VersionIdMarker = "1472739256446";


                ListVersionsResponse response = new ListVersionsResponse();
                response = s3.ListVersions(request);


                foreach (S3ObjectVersion key in response.Versions)
                {
                    Console.WriteLine(key.Key);
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

            // print out object key/value for validation
            //Console.WriteLine(string.Format("Copied object {0}/{1} to {2}/{3}", AWSS3Factory.S3_BUCKET, key_source, AWSS3Factory.S3_BUCKET, key_target));
            Console.ReadLine();

        }
    }
}
