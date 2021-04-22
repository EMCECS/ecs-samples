using ECSSDK.S3;
using ECSSDK.S3.Model;


namespace ecs_net_workshop
{
    class __06__QueryObjects
    {
        static ECSS3Client client = ECSS3Factory.getECSS3Client();
        static string temp_bucket = ECSS3Factory.S3_BUCKET;

        public static void QueryDecimal()
        {
            QueryObjectsRequest query_request = new QueryObjectsRequest()
            {
                BucketName = temp_bucket,
                Query = "x-amz-meta-decimalvalue>=6",
            };

            var response = client.QueryObjects(query_request);
        }

        public static void QueryString()
        {
            QueryObjectsRequest query_request = new QueryObjectsRequest()
            {
                BucketName = temp_bucket,
                Query = "x-amz-meta-stringvalue==\"sample-4\"",
            };

            var response = client.QueryObjects(query_request);
        }

        public static void QueryWithMaxKeysPaging()
        {
            QueryObjectsRequest query_request = new QueryObjectsRequest()
            {
                BucketName = temp_bucket,
                Query = "x-amz-meta-decimalvalue>4",
                MaxKeys = 1
            };

            var response = client.QueryObjects(query_request);
        }

        public static void QueryWithVersioning()
        {
            QueryObjectsRequest qor = new QueryObjectsRequest()
            {
                BucketName = temp_bucket,
                IncludeOlderVersions = true,
                Query = "x-amz-meta-decimalvalue>4"
            };

            var qor_respose = client.QueryObjects(qor);
        }
    }
}
