using System;
using ECSManagementSDK;
using DataSchemas;

namespace mgmt_net_workshop.examples
{
    public class _03_CreateBucket
    {
        public static void Main(string[] args)
        {
            System.Net.ServicePointManager.ServerCertificateValidationCallback = ((sender, certificate, chain, sslPolicyErrors) => true);

            // create new management client USING NAMESPACE ADMINISTRATOR to obtain token
            using (ECSManagementClient client = new ECSManagementClient(MgmtFactory.NAMESPACE_ADMIN_NAME, MgmtFactory.NAMESPACE_ADMIN_PASSWORD, MgmtFactory.MGMT_ENDPOINT))
            {
                // authenticate
                client.Authenticate().Wait();

                // using authenticated client, obtain new instance of service client
                ECSManagementService service = client.CreateServiceClient();

                // create new object user
                objectBucketRestRep result = service.CreateBucket(MgmtFactory.BUCKET_NAME, MgmtFactory.REPLICATION_GROUP, false, "S3", MgmtFactory.NAMESPACE_NAME, false, MgmtFactory.OBJECT_USER).Result;

                // print object user result for validation
                Console.WriteLine(string.Format("Successfully created bucket {0}.", result.name));
                Console.ReadLine();

                // log out
                client.LogOut();
            }
        }
    }
}
