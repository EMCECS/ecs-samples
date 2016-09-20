using System;
using ECSManagementSDK;

namespace mgmt_net_workshop.examples
{
    public class _11_DeleteNamespaceAdmin
    {
        public static void Main(string[] args)
        {
            System.Net.ServicePointManager.ServerCertificateValidationCallback = ((sender, certificate, chain, sslPolicyErrors) => true);

            // create new management client USING SYSTEM ADMINISTRATOR to obtain token
            using (ECSManagementClient client = new ECSManagementClient(MgmtFactory.MGMT_USER_NAME, MgmtFactory.MGMT_USER_PASSWORD, MgmtFactory.MGMT_ENDPOINT))
            {
                // authenticate
                client.Authenticate().Wait();

                // using authenticated client, obtain new instance of service client
                ECSManagementService service = client.CreateServiceClient();

                // delete namespace admin
                bool result = service.DeleteManagementUser(MgmtFactory.NAMESPACE_ADMIN_NAME).Result;

                // print message to indicate namespace admin was deleted
                Console.WriteLine("Management user {0} {1} deleted.", MgmtFactory.NAMESPACE_ADMIN_NAME, result ? "was succssfully" : "was not successfully");

                Console.ReadLine();

                // log out
                client.LogOut();
            }
        }
    }
}
