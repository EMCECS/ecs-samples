using System;
using Amazon.S3;
using ECSSDK.S3;
using Amazon.S3.Model;

namespace ecs_net_workshop.examples_bonus
{
    class _00_CopyObject
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 client
            ECSS3Client s3 = ECSS3Factory.getS3Client();

            // retrieve the key value from user to copy
            Console.Write("Enter the object key you want to copy: ");
            string key_source = Console.ReadLine();

            // retrieve the key value from user to name copied object
            Console.Write("Enter the object key for the copied object: ");
            string key_target = Console.ReadLine();

            // create the request object
            // When copying an object, you can preserve most of the metadata (default) or specify new metadata. 
            // However, the ACL is not preserved and is set to private for the user making the request. 
            CopyObjectRequest request = new CopyObjectRequest()
            {
                SourceBucket = ECSS3Factory.S3_BUCKET,
                SourceKey = key_source,
                DestinationBucket = ECSS3Factory.S3_BUCKET,
                DestinationKey = key_target,
                MetadataDirective = S3MetadataDirective.COPY
            };

            // copy the object
            CopyObjectResponse response = s3.CopyObject(request);

            // print out object key/value for validation
            Console.WriteLine(string.Format("Copied object {0}/{1} to {2}/{3}", ECSS3Factory.S3_BUCKET, key_source, ECSS3Factory.S3_BUCKET, key_target));
            Console.ReadLine();

        }
    }
}
