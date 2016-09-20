using Amazon.S3;
using Amazon.Runtime;

namespace aws_net_workshop
{
    public static class AWSS3Factory
    {
        /* the s3 access key id - this is equivalent to the user */
        public static string S3_ACCESS_KEY_ID = "";

        /* the s3 secret key associated with the S3_ACCESS_KEY_ID */
        public static string S3_SECRET_KEY = "";

        /*
         * The end point of the ViPR S3 REST interface - this should take the form of
         * http://ecs-address:9020 or https://ecs-address:9021
         * or
         * https://object.ecstestdrive.com
        */
        public static string S3_ENDPOINT = "";

        /* a unique bucket name to store objects */
        public static string S3_BUCKET = "workshop";

        /* private member to hold singleton client */
        private static volatile AmazonS3Client client;

        /* private lock for thread safety */
        private static object syncAccess = new object();

        public static AmazonS3Client getS3Client()
        {
            if (client == null)
            {
                lock (syncAccess)
                {
                    if (client == null)
                    {
                        BasicAWSCredentials creds = new BasicAWSCredentials(S3_ACCESS_KEY_ID, S3_SECRET_KEY);
                        AmazonS3Config cc = new AmazonS3Config()
                        {
                            ForcePathStyle = true,
                            ServiceURL = S3_ENDPOINT,
                            SignatureVersion = "2",
                            SignatureMethod = SigningAlgorithm.HmacSHA1,
                            UseHttp = false,
                        };
                        client = new AmazonS3Client(creds, cc);
                    }
                }
            }
            return client;
        }
    }
}
