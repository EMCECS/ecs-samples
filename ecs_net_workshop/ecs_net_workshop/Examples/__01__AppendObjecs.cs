using System;
using System.IO;
using Amazon.S3.Model;
using ECSSDK.S3;
using ECSSDK.S3.Model;


namespace ecs_net_workshop
{
    public class __01__AppendObject
    {
        public static void Append_Object()
        {   
            ECSS3Client client = ECSS3Factory.getECSS3Client();

            string temp_bucket = ECSS3Factory.S3_BUCKET;

            string key = "key-1";
            string content = "What goes up";

            PutObjectRequestECS por = new PutObjectRequestECS()
            {
                BucketName = temp_bucket,
                Key = key,
                ContentBody = content
            };

            // create the object
            client.PutObject(por);

            // append to the object
            long result = client.AppendObject(temp_bucket, key, " must come down.");

            // verify the append worked
            GetObjectResponse response = client.GetObject(temp_bucket, key);
            Stream responseStream = response.ResponseStream;
            StreamReader reader = new StreamReader(responseStream);
            string readContent = reader.ReadToEnd();
            Console.Write(readContent);


        }
    }
}

