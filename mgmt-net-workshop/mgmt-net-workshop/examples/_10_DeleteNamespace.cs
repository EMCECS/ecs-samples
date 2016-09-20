using System;
using ECSManagementSDK;

namespace mgmt_net_workshop.examples
{
    public class _10_DeleteNamespace
    {
        public static void Main(string[] args)
        {
            System.Net.ServicePointManager.ServerCertificateValidationCallback = ((sender, certificate, chain, sslPolicyErrors) => true);

            // create new management client USING NAMESPACE ADMINISTRATOR to obtain token
            using (ECSManagementClient client = new ECSManagementClient(MgmtFactory.MGMT_USER_NAME, MgmtFactory.MGMT_USER_PASSWORD, MgmtFactory.MGMT_ENDPOINT))
            {
                // authenticate
                client.Authenticate().Wait();

                // using authenticated client, obtain new instance of service client
                ECSManagementService service = client.CreateServiceClient();

                // delete namespace
                bool result = service.DeleteNamespace(MgmtFactory.NAMESPACE_NAME).Result;

                // print message to indicate namespace was deleted
                Console.WriteLine("Namespace {0} deleted.", result ? "was succssfully" : "was not successfully");

                Console.ReadLine();

                // log out
                client.LogOut();
            }
        }
    }
}
