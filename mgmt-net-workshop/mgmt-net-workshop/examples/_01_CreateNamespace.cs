using System;
using System.Collections.Generic;
using ECSManagementSDK;
using DataSchemas;

namespace mgmt_net_workshop.examples
{
    public class _01_CreateNamespace
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

                // specify namespace administrator
                List<string> admns = new List<string>();
                admns.Add(MgmtFactory.NAMESPACE_ADMIN_NAME);

                // create new namespace 
                namespaceRestRep result = service.CreateNamespace(MgmtFactory.NAMESPACE_NAME, admns, MgmtFactory.REPLICATION_GROUP, string.Empty, new List<string>(), new List<string>()).Result;

                // print namespace name for validation
                Console.WriteLine(string.Format("Successfully created namespace {0}.", result.name));
                Console.ReadLine();

                // log out
                client.LogOut();
            }
        }
    }
}
