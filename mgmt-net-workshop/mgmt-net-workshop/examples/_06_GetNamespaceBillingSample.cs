using System;
using ECSManagementSDK;
using DataSchemas;

namespace mgmt_net_workshop.examples
{
    public class _06_GetNamespaceBillingSample
    {
        public static void Main(string[] args)
        {
            System.Net.ServicePointManager.ServerCertificateValidationCallback = ((sender, certificate, chain, sslPolicyErrors) => true);

            // create new management client USING NAMESPACE ADMINISTRATOR to obtain token
            using (ECSManagementClient client = new ECSManagementClient(MgmtFactory.NAMESPACE_ADMIN_NAME, MgmtFactory.NAMESPACE_ADMIN_PASSWORD, MgmtFactory.MGMT_ENDPOINT))
            {

                bool includeBucketDetail = true;
                string metricSizeUnit = "GB";
                DateTime startTime = new DateTime(2016, 8, 1);
                DateTime endTime = new DateTime(2016, 8, 31);

                // authenticate
                client.Authenticate().Wait();

                // using authenticated client, obtain new instance of service client
                ECSManagementService service = client.CreateServiceClient();

                // get metrics for specific namespace and specified timeframe
                namespaceBillingSampleRestRep result = service.GetNamespaceBillingSample(MgmtFactory.NAMESPACE_NAME, 
                    startTime, endTime, metricSizeUnit, includeBucketDetail, null).Result;

                // print namespace metrics
                Console.WriteLine("For timeframe {0} to {1} ...", startTime.ToString(), endTime.ToString());
                Console.WriteLine(string.Format("NAMESPACE: {0} OBJECTS: {1} SIZE: {2} {3} INGRESS: {4} EGRESS {5}.",
                    MgmtFactory.NAMESPACE_NAME, result.total_objects, result.total_size, result.total_size_unit, result.ingress, result.egress));

                Console.WriteLine();

                // print bucket metrics
                if (includeBucketDetail)
                {
                    foreach (var item in result.bucket_billing_sample)
                    {
                        Console.WriteLine(string.Format("BUCKET: {0, -20} OBJECTS: {1, 8} SIZE: {2, -4} {3, -2} INGRESS: {4, -10} EGRESS {5, -10}",
                            item.name, item.total_objects, item.total_size, item.total_size_unit, item.ingress, item.egress));
                    }
                }

                Console.ReadLine();

                // log out
                client.LogOut();
            }
        }
    }
}
