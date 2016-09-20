using System;
using Amazon.S3;
using Amazon.S3.Model;

namespace aws_net_workshop.examples
{
    public class _00_CreateBucket
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            //System.Net.ServicePointManager.ServerCertificateValidationCallback = ((sender, certificate, chain, sslPolicyErrors) => true);
            //PutBucketVersioningRequest req = new PutBucketVersioningRequest()
            //{
            //    BucketName = AWSS3Factory.S3_BUCKET,

            //    VersioningConfig = new S3BucketVersioningConfig()
            //    {
            //        Status = VersionStatus.Enabled
            //    }
            //};

            //PutBucketVersioningResponse res = s3.PutBucketVersioning(req);

            // create bucket request
            PutBucketRequest request = new PutBucketRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET
            };

            // create bucket - used for subsequent demos
            s3.PutBucket(request);

            // create bucket lising request
            ListObjectsRequest objects = new ListObjectsRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET
            };

            // get bucket lising to retrieve bucket name
            ListObjectsResponse result = s3.ListObjects(objects);

            // print bucket name for validation
            Console.WriteLine(string.Format("Successfully created bucket {0}.", result.Name));
            Console.ReadLine();
        }
    }
}
