using ECSSDK.S3;
using ECSSDK.S3.Model;

namespace ecs_net_workshop
{
    class __04__ListSystemMetaSearchKeys
    {
        static ECSS3Client client = ECSS3Factory.getECSS3Client();
        static string temp_bucket = ECSS3Factory.S3_BUCKET;

        public static void ListSystemMetaSearchKeys()
        {
            ListSystemMetaSearchKeysRequest request = new ListSystemMetaSearchKeysRequest();

            ListSystemMetaSearchKeysResponse response = client.ListSystemMetaSearchKeys(request);
        }
    }
}
