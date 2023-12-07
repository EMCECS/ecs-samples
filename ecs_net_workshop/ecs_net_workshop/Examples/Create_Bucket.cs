using System;
using ECSSDK.S3;
using ECSSDK.S3.Model;


namespace ecs_net_workshop
{
    class Create_Bucket
    {
        public static void CreateBucket()
        {
            /// <summary>
            /// The S3 client -handles object api interactions.
            ///</summary>
            ECSS3Client client = ECSS3Factory.getECSS3Client();

            /// <summary>
            /// Generic bucket name used for bucket API testing.
            ///</summary>
            string temp_bucket = Guid.NewGuid().ToString();

            System.Net.ServicePointManager.ServerCertificateValidationCallback = ((sender, certificate, chain, sslPolicyErrors) => true);


            PutBucketRequestECS request = new PutBucketRequestECS()
            {
                BucketName = temp_bucket,
            };

            client.PutBucket(request);
        }

    }
}
