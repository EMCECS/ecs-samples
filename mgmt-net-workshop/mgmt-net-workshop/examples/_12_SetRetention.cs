using System;
using ECSManagementSDK;
using DataSchemas;
using Amazon.S3;
using Amazon.S3.Model;

namespace mgmt_net_workshop.examples
{
    public class _12_SetRetention
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

                // create the AWS S3 client
                AmazonS3Client s3 = MgmtFactory.getS3Client();

                // get current retention period
                bucketRetentionInfoRep retention = service.GetBucketRetention(MgmtFactory.BUCKET_NAME, MgmtFactory.NAMESPACE_NAME).Result;
                Console.WriteLine(string.Format("Current bucket retention period: {0} seconds", retention.period));

                // set retention period to 1 minute
                Console.WriteLine("Setting retention period to one minute");
                bool result = service.SetBucketRetention(MgmtFactory.BUCKET_NAME, MgmtFactory.NAMESPACE_NAME, 60).Result;
                if (result)
                {

                    // create object
                    PutObjectRequest request = new PutObjectRequest()
                    {
                        BucketName = MgmtFactory.BUCKET_NAME,
                        ContentBody = "object content",
                        Key = "object1"
                    };

                    // create the object in bucket
                    s3.PutObject(request);
                    Console.WriteLine("Object uploaded to bucket");
                    bool objectDeleted = false;

                    while (!objectDeleted)
                    {
                        Console.WriteLine("Trying to delete the object");
                        DeleteObjectRequest request2 = new DeleteObjectRequest()
                        {
                            BucketName = MgmtFactory.BUCKET_NAME,
                            Key = "object1"
                        };
                        try
                        {
                            DeleteObjectResponse response = s3.DeleteObject(request2);
                            Console.WriteLine("--> Object deleted successfully");
                            objectDeleted = true;
                        }
                        catch (Exception e)
                        {
                            Console.WriteLine("Error: {0}", e.Message);
                            System.Threading.Thread.Sleep(10000);
                        }
                    }
                }
                else
                {
                    Console.WriteLine("Could not set retention period. Exiting...");
                }

                Console.ReadLine();

                // log out
                client.LogOut();
            }
        }
    }
}
