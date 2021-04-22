using Amazon.S3;
using Amazon.Runtime;
using ECSSDK.S3;

namespace ecs_net_workshop
{
    class ECSS3Factory
    {
        // credentials
        public static string END_POINT = "";
        public static string ACCESS_KEY = "";
        public static string SECRET_KEY = "";

        public static string S3_BUCKET = "";

        public static volatile ECSS3Client client;

        private static object syncAccess = new object();

        public static ECSS3Client getECSS3Client()
        {
            if (client == null)
            {
                lock (syncAccess)
                {
                    if (client == null)
                    {
                        BasicAWSCredentials creds = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
                        AmazonS3Config config = new AmazonS3Config()
                        {
                            ForcePathStyle = true,
                            ServiceURL = END_POINT,
                            SignatureVersion = "2",
                            SignatureMethod = SigningAlgorithm.HmacSHA1,
                            UseHttp = false,
                        };
                        client = new ECSS3Client(creds, config);
                    }
                }
            }
            return client;
        }
    }
}
