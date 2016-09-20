using System;
using ECSManagementSDK;
using DataSchemas;

namespace mgmt_net_workshop.examples
{
    public class _02_CreateObjectUser
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
                secretKeyInfoRep result = service.CreateObjectUser(MgmtFactory.OBJECT_USER, MgmtFactory.NAMESPACE_NAME).Result;

                // print object user result for validation
                Console.WriteLine(string.Format("Successfully created object user {0}.", result.link.href));
                Console.ReadLine();

                // log out
                client.LogOut();
            }
        }
    }
}
