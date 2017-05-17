using System;
using Amazon.S3;
using Amazon.S3.Model;
using System.Collections.Generic;

namespace aws_net_workshop.examples_bonus
{
    /// <summary>
    /// Configure bucket lifecycle
    /// </summary>
    /// <remarks>
    /// Lifecycle configuration enables you to specify the lifecycle management
    /// of objects in a bucket. The configuration is a set of one or more rules,
    /// where each rule defines an action to apply to a group of objects.
    /// The expiration action allows you to specify when the objects expire, then
    /// ECS will delete the expired object.
    ///
    /// This sample will go through the following steps:
    ///
    ///   1. Create a bucket
    ///   2. Configure bucket lifecycle rules to automatically set an expiration time
    ///   3. Retrieve the bucket lifecycle configuration and verify the rules were applied
    ///   4. Delete the bucket lifecycle configuration
    ///   5. Retrieve the bucket lifecycle configuration and verify it is empty
    ///   6. Delete the bucket
    ///
    /// </remarks>
    class _04_Lifecycle
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();
            
            String bucketName = String.Join("-", AWSS3Factory.S3_BUCKET, DateTime.Now.ToString("yyyyMMddHHmmss"));

            //************************//
            // 1. Create a bucket     //
            //************************//

            Console.Write(string.Format(" [*] Creating bucket '{0}'... ", bucketName));

            PutBucketResponse pbRes = s3.PutBucket(bucketName);
            if (pbRes.HttpStatusCode != System.Net.HttpStatusCode.OK)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");

            //*************************************//
            // 2. Configure bucket lifecycle rules //
            //*************************************//

            Console.Write(string.Format(" [*] Updating lifecycle configuration for bucket '{0}'... ", bucketName));

            LifecycleConfiguration newConfiguration = new LifecycleConfiguration
            {
                Rules = new List<LifecycleRule>
                {
                    // Rule to delete keys with prefix "Test-" after 5 days
                    new LifecycleRule
                    {
                        Prefix = "Test-",
                        Expiration = new LifecycleRuleExpiration { Days = 5 },
                        Status = LifecycleRuleStatus.Enabled
                    },
                    // Rule to delete keys in subdirectory "Logs" after 2 days
                    new LifecycleRule
                    {
                        Prefix = "Logs/",
                        Expiration = new LifecycleRuleExpiration { Days = 2 },
                        Id = "log-file-removal",
                        Status = LifecycleRuleStatus.Enabled
                    }
                }
            };


            PutLifecycleConfigurationRequest plcReq = new PutLifecycleConfigurationRequest
            {
                BucketName = bucketName,
                Configuration = newConfiguration
            };
            PutLifecycleConfigurationResponse plcRes = s3.PutLifecycleConfiguration(plcReq);

            if (plcRes.HttpStatusCode != System.Net.HttpStatusCode.OK)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");

            //************************************************//
            // 3. Retrieve the bucket lifecycle configuration //
            //************************************************//

            Console.Write(string.Format(" [*] Retrieving current lifecycle configuration for bucket '{0}'... ", bucketName));

            GetLifecycleConfigurationResponse glcRes = s3.GetLifecycleConfiguration(bucketName);

            if (glcRes.HttpStatusCode != System.Net.HttpStatusCode.OK)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");

            Console.WriteLine(String.Format(" [x] Configuration contains {0} rules", glcRes.Configuration.Rules.Count));
            foreach (LifecycleRule rule in glcRes.Configuration.Rules)
            {
                Console.WriteLine(" [x]     Rule:");
                Console.WriteLine(" [x]         Prefix = " + rule.Prefix);
                Console.WriteLine(" [x]         Expiration (days) = " + rule.Expiration.Days);
                Console.WriteLine(" [x]         Id = " + rule.Id);
                Console.WriteLine(" [x]         Status = " + rule.Status);
            }

            //**********************************************//
            // 4. Delete the bucket lifecycle configuration //
            //**********************************************//

            Console.Write(String.Format(" [*] Deleting current lifecycle configuration for bucket '{0}'... ", bucketName));

            DeleteLifecycleConfigurationResponse dlcRes = s3.DeleteLifecycleConfiguration(bucketName);

            if (dlcRes.HttpStatusCode != System.Net.HttpStatusCode.NoContent)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");


            Console.Write(String.Format(" [*] Verifying current lifecycle rules for bucket '{0}' are empty... ", bucketName));

            LifecycleConfiguration configuration = s3.GetLifecycleConfiguration(bucketName).Configuration;

            if (configuration.Rules.Count != 0)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");

            //************************//
            // 5. Delete the bucket   //
            //************************//

            Console.Write(String.Format(" [*] Deleting bucket '{0}'... ", bucketName));

            DeleteBucketResponse dbRes = s3.DeleteBucket(bucketName);

            if (dbRes.HttpStatusCode != System.Net.HttpStatusCode.NoContent)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");

            Console.WriteLine(" [*] Example is completed. Press any key to exit...");
            Console.ReadLine();

        }
    }
}
