using System;
using ECSManagementSDK;
using DataSchemas;

namespace mgmt_net_workshop.examples
{
    public class _07_GetBucketBillingSample
    {
        public static void Main(string[] args)
        {
            System.Net.ServicePointManager.ServerCertificateValidationCallback = ((sender, certificate, chain, sslPolicyErrors) => true);

            // create new management client USING NAMESPACE ADMINISTRATOR to obtain token
            using (ECSManagementClient client = new ECSManagementClient(MgmtFactory.NAMESPACE_ADMIN_NAME, MgmtFactory.NAMESPACE_ADMIN_PASSWORD, MgmtFactory.MGMT_ENDPOINT))
            {
                string metricSizeUnit = "GB";
                DateTime startTime = new DateTime(2014, 8, 1);
                DateTime endTime = new DateTime(2016, 8, 31);

                // authenticate
                client.Authenticate().Wait();

                // using authenticated client, obtain new instance of service client
                ECSManagementService service = client.CreateServiceClient();

                // get metrics for specific bucket and specified timeframe
                bucketBillingSampleRestRep result = service.GetBucketBillingSample(MgmtFactory.NAMESPACE_NAME, MgmtFactory.BUCKET_NAME, startTime, endTime, metricSizeUnit).Result;

                // print bucket metrics
                Console.WriteLine("For timeframe {0} to {1} ...", startTime.ToString(), endTime.ToString());
                Console.WriteLine(string.Format("BUCKET: {0} OBJECTS: {1} SIZE: {2} {3} INGRESS: {4} EGRESS {5}.",
                    result.name, result.total_objects, result.total_size, result.total_size_unit, result.ingress, result.egress));

                Console.ReadLine();

                // log out
                client.LogOut();
            }
        }
    }
}
