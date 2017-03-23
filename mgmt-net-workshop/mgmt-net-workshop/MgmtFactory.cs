using Amazon.Runtime;
using Amazon.S3;

namespace mgmt_net_workshop
{
    public static class MgmtFactory
    {
        /* the ecs management user name - namespace admin or system admin. */
        public static string MGMT_USER_NAME = "";
        /* the password for the management user */
        public static string MGMT_USER_PASSWORD = "";
        /* the end point of the ECS management api REST interface */
        public static string MGMT_ENDPOINT = "https://IP_OF_ECS:4443";

        /* the namespace administrator - to be created and used in subsequent examples */
        public static string NAMESPACE_ADMIN_NAME = "workshop-admin";
        /* the namespace administrator password */
        public static string NAMESPACE_ADMIN_PASSWORD = "Password1";
        /* the namespace - to be created and used in subsequent examples */
        public static string NAMESPACE_NAME = "workshop";
        /* the object user - to be created and used in subsequent examples */
        public static string OBJECT_USER = "workshop-object-user";
        /* the replication group within to create namespace */
        public static string REPLICATION_GROUP = "";
        /* the bucket name to be created in new namespace */
        public static string BUCKET_NAME = "workshop-bucket";

        /* ----- S3 ----- */

        /*
         * The end point of the ECS S3 REST interface - this should take the form of
         * http://ecs-address:9020 or https://ecs-address:9021
         * or
         * https://object.ecstestdrive.com
        */
        public static string S3_ENDPOINT = "http://IP_OF_ECS:9020";
        /* the s3 access key id - this is equivalent to the object user */
        public static string S3_ACCESS_KEY_ID = OBJECT_USER;
        /* the s3 secret key associated with the S3_ACCESS_KEY_ID */
        public static string S3_SECRET_KEY = "";

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
