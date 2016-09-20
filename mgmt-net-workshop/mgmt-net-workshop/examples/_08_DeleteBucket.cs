using System;
using ECSManagementSDK;

namespace mgmt_net_workshop.examples
{
    public class _08_DeleteBucket
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

                // delete bucket
                bool result = service.DeleteBucket(MgmtFactory.BUCKET_NAME, MgmtFactory.NAMESPACE_NAME).Result;

                // print message to indicate bucket was deleted
                Console.WriteLine("Bucket {0} {1} deleted from namespace {2}", MgmtFactory.BUCKET_NAME,
                    result ? "was succssfully" : "was not successfully", MgmtFactory.NAMESPACE_NAME);

                Console.ReadLine();

                // log out
                client.LogOut();
            }
        }
    }
}
