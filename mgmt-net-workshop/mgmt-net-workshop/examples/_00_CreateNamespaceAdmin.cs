using System;
using ECSManagementSDK;
using DataSchemas;

namespace mgmt_net_workshop.examples
{
    public class _00_CreateNamespaceAdmin
    {
        public static void Main(string[] args)
        {
            System.Net.ServicePointManager.ServerCertificateValidationCallback = ((sender, certificate, chain, sslPolicyErrors) => true);

            // create new management client to obtain token
            using (ECSManagementClient client = new ECSManagementClient(MgmtFactory.MGMT_USER_NAME, MgmtFactory.MGMT_USER_PASSWORD, MgmtFactory.MGMT_ENDPOINT))
            {
                // authenticate
                client.Authenticate().Wait();

                // using authenticated client, obtain new instance of service client
                ECSManagementService service = client.CreateServiceClient();

                // create new namespace administrator
                mgmtUserInfoRestRep result = service.CreateManagementUser(MgmtFactory.NAMESPACE_ADMIN_NAME, MgmtFactory.NAMESPACE_ADMIN_PASSWORD, false, false).Result;

                // print namespace admin userid for validation
                Console.WriteLine(string.Format("Successfully created namespace administrator {0}.", result.userId));
                Console.ReadLine();

                // log out
                client.LogOut();
            }                
        }
    }
}
