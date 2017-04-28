using System;
using Amazon.S3;
using Amazon.S3.Model;

namespace aws_net_workshop.examples
{
    class _09_ListObjects
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            // display object list to user
            bool moreRecords = true;
            string nextMarker = string.Empty;

            while (moreRecords)
            {
                // create the request object
                ListObjectsRequest request = new ListObjectsRequest()
                {
                    BucketName = AWSS3Factory.S3_BUCKET,
                    Delimiter = "/",
                    Prefix = "january/"
                    
                };

                // if there's a marker from previous request, set it here
                if (nextMarker.Length > 0)
                    request.Marker = nextMarker;

                // get the list of objects
                ListObjectsResponse response = s3.ListObjects(request);

                // display common prefixes
                foreach (string prefix in response.CommonPrefixes)
                {
                    Console.WriteLine(string.Format("Prefix: {0}", prefix));
                }

                // display object keys to user
                foreach (S3Object s3Object in response.S3Objects)
                {
                    Console.WriteLine(string.Format("Object: {0}", s3Object.Key));
                }

                //Console.WriteLine(string.Format("Next Marker: {0}", response.NextMarker));

                // set next marker or exit
                if (response.IsTruncated)
                {
                    nextMarker = response.NextMarker;
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
