using System;
using ECSManagementSDK;
using DataSchemas;

namespace mgmt_net_workshop.examples
{
    public class _05_GetNamespaceBillingInfo
    {
        public static void Main(string[] args)
        {
            System.Net.ServicePointManager.ServerCertificateValidationCallback = ((sender, certificate, chain, sslPolicyErrors) => true);

            // create new management client USING NAMESPACE ADMINISTRATOR to obtain token
            using (ECSManagementClient client = new ECSManagementClient(MgmtFactory.NAMESPACE_ADMIN_NAME, MgmtFactory.NAMESPACE_ADMIN_PASSWORD, MgmtFactory.MGMT_ENDPOINT))
            {

                bool includeBucketDetail = true;
                string metricSizeUnit = "GB";

                // authenticate
                client.Authenticate().Wait();

                // using authenticated client, obtain new instance of service client
                ECSManagementService service = client.CreateServiceClient();

                // get metrics for specific namespace
                namespaceBillingRestRep result = service.GetNamespaceBillingInfo(MgmtFactory.NAMESPACE_NAME, metricSizeUnit, includeBucketDetail, null).Result;

                // print namespace metrics
                Console.WriteLine(string.Format("Namespace {0}: Total Objects: {1} Total Size: {2} {3}.", 
                    MgmtFactory.NAMESPACE_NAME, result.total_objects, result.total_size, result.total_size_unit));

                // print bucket metrics
                if (includeBucketDetail)
                {
                    foreach (var item in result.bucket_billing_info)
                    {
                        Console.WriteLine(string.Format("Bucket {0, -35} Total Objects: {1, 8} Total Size: {2, -4} {3, -2}",
                            item.name, item.total_objects, item.total_size, item.total_size_unit));
                    }
                }

                Console.ReadLine();

                // log out
                client.LogOut();
            }
        }
    }
}
