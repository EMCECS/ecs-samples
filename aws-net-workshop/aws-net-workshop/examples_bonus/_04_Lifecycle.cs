using System;
using Amazon.S3;
using Amazon.S3.Model;
using System.Collections.Generic;

namespace aws_net_workshop.examples_bonus
{
    class _04_Lifecycle
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            String bucketName = "bucket-" + DateTime.Now.ToString("yyyyMMddHHmmss");

            Console.Write(string.Format("Creating bucket '{0}'... ", bucketName));

            PutBucketResponse pbRes = s3.PutBucket(bucketName);
            if (pbRes.HttpStatusCode != System.Net.HttpStatusCode.OK)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");

            Console.Write(string.Format("Updating lifecycle configuration for bucket '{0}'... ", bucketName));

            LifecycleConfiguration newConfiguration = new LifecycleConfiguration
            {
                Rules = new List<LifecycleRule>
                {
                    // Rule to delete keys with prefix "Test-" after 5 days
                    new LifecycleRule
                    {
                        Prefix = "Test-",
                        Expiration = new LifecycleRuleExpiration { Days = 5 }
                    },
                    // Rule to delete keys in subdirectory "Logs" after 2 days
                    new LifecycleRule
                    {
                        Prefix = "Logs/",
                        Expiration = new LifecycleRuleExpiration { Days = 2 },
                        Id = "log-file-removal"
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

            Console.Write(string.Format("Retrieving current lifecycle configuration for bucket '{0}'... ", bucketName));

            GetLifecycleConfigurationResponse glcRes = s3.GetLifecycleConfiguration(bucketName);

            if (glcRes.HttpStatusCode != System.Net.HttpStatusCode.OK)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");


            Console.WriteLine(String.Format("-> Configuration contains {0} rules", glcRes.Configuration.Rules.Count));
            foreach (LifecycleRule rule in glcRes.Configuration.Rules)
            {
                Console.WriteLine("->   Rule:");
                Console.WriteLine("->     Prefix = " + rule.Prefix);
                Console.WriteLine("->     Expiration (days) = " + rule.Expiration.Days);
                Console.WriteLine("->     Id = " + rule.Id);
                Console.WriteLine("->     Status = " + rule.Status);
            }

            Console.Write(String.Format("Deleting current lifecycle configuration for bucket '{0}'... ", bucketName));

            DeleteLifecycleConfigurationResponse dlcRes = s3.DeleteLifecycleConfiguration(bucketName);

            if (dlcRes.HttpStatusCode != System.Net.HttpStatusCode.NoContent)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");


            Console.Write(String.Format("Verifying current lifecycle rules for bucket '{0}' are empty... ", bucketName));

            LifecycleConfiguration configuration = s3.GetLifecycleConfiguration(bucketName).Configuration;

            if (configuration.Rules.Count != 0)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");

            Console.Write(String.Format("Deleting bucket '{0}'... ", bucketName));

            DeleteBucketResponse dbRes = s3.DeleteBucket(bucketName);

            if (dbRes.HttpStatusCode != System.Net.HttpStatusCode.NoContent)
            {
                Console.WriteLine("fail");
                Console.ReadLine();
                System.Environment.Exit(1);
            }
            Console.WriteLine("done");


            Console.ReadLine();

        }
    }
}
