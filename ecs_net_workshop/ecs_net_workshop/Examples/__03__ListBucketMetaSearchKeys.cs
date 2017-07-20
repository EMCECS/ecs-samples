using ECSSDK.S3;
using ECSSDK.S3.Model;

namespace ecs_net_workshop
{
    class __03__ListBucketMetaSearchKeys
    {
        static ECSS3Client client = ECSS3Factory.getECSS3Client();
        static string temp_bucket = ECSS3Factory.S3_BUCKET;

        public static void ListBucketMetaSearchKeys()
        {
            ListBucketMetaSearchKeysRequest request = new ListBucketMetaSearchKeysRequest()
            {
                BucketName = temp_bucket
            };

            ListBucketMetaSearchKeysResponse response = client.ListBucketMetaSearchKeys(request);
        }
    }
}
