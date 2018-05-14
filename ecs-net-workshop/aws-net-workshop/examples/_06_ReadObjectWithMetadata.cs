using System;
using ECSSDK.S3;
using Amazon.S3.Model;
using System.Collections.Generic;

namespace ecs_net_workshop.examples
{
    class _06_ReadObjectWithMetadata
    {
        public static void Main(string[] args)
        {
            // create the ECS S3 Client
            ECSS3Client s3 = ECSS3Factory.getS3Client();

            // retrieve the object key from the user
            Console.Write("Enter the object key: ");
            string key = Console.ReadLine();

            // create object metadata request
            GetObjectMetadataRequest request = new GetObjectMetadataRequest()
            {
                BucketName = ECSS3Factory.S3_BUCKET,
                Key = key
            };

            // get object metadata - not actual content (HEAD request not GET).
            GetObjectMetadataResponse response =  s3.GetObjectMetadata(request);

            // print out object key/value and metadata key/value for validation
            Console.WriteLine(string.Format("Metadata for {0}/{1}", ECSS3Factory.S3_BUCKET, key));

            MetadataCollection metadataCollection = response.Metadata;

            ICollection<string> metaKeys = metadataCollection.Keys;
            foreach (string metaKey in metaKeys)
            {
                Console.WriteLine("{0}={1}", metaKey, metadataCollection[metaKey]);
            }
            Console.ReadLine();
        }
    }
}
