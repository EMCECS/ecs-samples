using System;
using ECSSDK.S3;
using ECSSDK.S3.Model;
using ECSSDK.S3.Model.Util;
using System.Collections.Generic;
using System.Text;

namespace ecs_net_workshop.examples
{
    class _13_SearchObjects
    {
        public static string[] KEY_LIST = new string[] {
            "20151102/account-12345/bill.xml",
            "20151102/account-12345/bill.pdf",
            "20151102/account-12345/bill.html",
            "20151102/account-55555/bill.xml",
            "20151102/account-55555/bill.pdf",
            "20151102/account-55555/bill.html",
            "20151102/account-77777/bill.xml",
            "20151102/account-77777/bill.pdf",
            "20151102/account-77777/bill.html",
            "20151103/account-11111/bill.xml",
            "20151103/account-11111/bill.pdf",
            "20151103/account-11111/bill.html",
            "20151103/account-11122/bill.xml",
            "20151103/account-11122/bill.pdf",
            "20151103/account-11122/bill.html",
            "20151103/account-11133/bill.xml",
            "20151103/account-11133/bill.pdf",
            "20151103/account-11133/bill.html",
            "20141103/account-11111/bill.xml",
            "20141103/account-11111/bill.pdf",
            "20141103/account-11111/bill.html",
            "20141103/account-11122/bill.xml",
            "20141103/account-11122/bill.pdf",
            "20141103/account-11122/bill.html",
            "20141103/account-11133/bill.xml",
            "20141103/account-11133/bill.pdf",
            "20141103/account-11133/bill.html" };

        public static string FIELD_ACCOUNT_ID = "account-id";
        public static string FIELD_BILLING_DATE = "billing-date";
        public static string FIELD_BILL_TYPE = "bill-type";
        public static string BUCKET_NAME = "search-test";
        public static string USER_PREFIX = "x-amz-meta-";

        public static void Main(string[] args)
        {
            // create the ECS S3 client
            ECSS3Client s3 = ECSS3Factory.getS3Client();

            // Create the bucket with indexed keys
            List<MetaSearchKey> bucketMetadataSearchKeys = new List<MetaSearchKey>()
            {
                new MetaSearchKey() { Name = USER_PREFIX + FIELD_ACCOUNT_ID, Type = MetaSearchDatatype.integer },
                new MetaSearchKey() { Name = USER_PREFIX + FIELD_BILLING_DATE, Type = MetaSearchDatatype.datetime },
                new MetaSearchKey() { Name = USER_PREFIX + FIELD_BILL_TYPE, Type = MetaSearchDatatype.@string }
            };


            PutBucketRequestECS pbr = new PutBucketRequestECS();
            pbr.BucketName = BUCKET_NAME;
            pbr.SetMetadataSearchKeys(bucketMetadataSearchKeys);
            s3.PutBucket(pbr);

            foreach(string key in KEY_LIST)
            {
                PutObjectRequestECS por = new PutObjectRequestECS();
                por.BucketName = BUCKET_NAME;
                por.Key = key;
                por.Metadata.Add(FIELD_ACCOUNT_ID, extractAccountId(key));
                por.Metadata.Add(FIELD_BILLING_DATE, extractBillDate(key));
                por.Metadata.Add(FIELD_BILL_TYPE, extractBillType(key));
                s3.PutObject(por);
            }

            while (true)
            {
                Console.Write("Enter the account id (empty for none): ");
                string accountId = Console.ReadLine();
                Console.Write("Enter the billing date (e.g. 2016-09-22, empty for none): ");
                string billingDate = Console.ReadLine();
                Console.Write("Enter the bill type (e.g. xml.  empty for none): ");
                string billType = Console.ReadLine();

                QueryObjectsRequest qor = new QueryObjectsRequest()
                {
                    BucketName = BUCKET_NAME
                };

                StringBuilder query = new StringBuilder();
                if (accountId.Length > 0)
                {
                    query.Append(USER_PREFIX + FIELD_ACCOUNT_ID + "==" + accountId + "");
                }

                if (billingDate.Length > 0)
                {
                    if (query.Length > 0)
                    {
                        query.Append(" and ");
                    }
                    query.Append(USER_PREFIX + FIELD_BILLING_DATE + "==" + billingDate + "T00:00:00Z");

                }

                if (billType.Length > 0)
                {
                    if (query.Length > 0)
                    {
                        query.Append(" and ");
                    }
                    query.Append(USER_PREFIX + FIELD_BILL_TYPE + "=='" + billType + "'");
                }

                qor.Query = query.ToString();

                QueryObjectsResponse res = s3.QueryObjects(qor);
                Console.WriteLine("--------------------------");
                Console.WriteLine("Bucket: " + res.BucketName);
                Console.WriteLine("Query: " + qor.Query);
                Console.WriteLine();

                Console.WriteLine("Key");
                Console.WriteLine("--------------------------");

                foreach(QueryObject obj in res.ObjectMatches)
                {
                    Console.WriteLine(string.Format("{0}", obj.Name));
                }

                Console.Write("Another? (Y/N) ");
                string another = Console.ReadLine();

                if(another.ToUpper() == "N")
                {
                    break;
                }
            }

            //cleanup
            foreach (string key in KEY_LIST)
            {
                s3.DeleteObject(BUCKET_NAME, key);
            }
            s3.DeleteBucket(BUCKET_NAME);

        }

        private static string extractAccountId(string key)
        {
            return key.Split('/')[1].Split('-')[1];
        }

        private static string extractBillDate(string key)
        {
            string date = key.Split('/')[0];
            // Make it a ISO-8601 TS
            return string.Format("{0}-{1}-{2}T00:00:00Z", date.Substring(0, 4), date.Substring(4, 2), date.Substring(6, 2));
        }

        private static string extractBillType(string key)
        {
            return key.Substring(key.LastIndexOf(".") + 1);
        }
    }
}
