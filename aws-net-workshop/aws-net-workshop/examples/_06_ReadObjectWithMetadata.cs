using System;
using Amazon.S3;
using Amazon.S3.Model;
using System.Collections.Generic;

namespace aws_net_workshop.examples
{
    class _06_ReadObjectWithMetadata
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 Client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            // retrieve the object key from the user
            Console.Write("Enter the object key: ");
            string key = Console.ReadLine();

            // create object request
            GetObjectRequest request = new GetObjectRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET,
                Key = key
            };

            // create the object with metadata in the demo bucket
            GetObjectResponse response = s3.GetObject(request);

            // print out object key/value and metadata key/value for validation
            Console.WriteLine(string.Format("Metadata for {0}/{1}", AWSS3Factory.S3_BUCKET, key));

            MetadataCollection metadataCollection = response.Metadata;

            ICollection<string> metaKeys = metadataCollection.Keys;
            foreach (string metaKey in metaKeys)
            {
                Console.WriteLine("     {0}={1}", metaKey, metadataCollection[metaKey]);
            }
            Console.ReadLine();
        }
    }
}
