using System;
using Amazon.S3;
using Amazon.S3.Model;

namespace aws_net_workshop.examples
{
    class _07_PresignedURL
    {
        public static void Main(string[] args)
        {
            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            // retrieve the object key and hours before expiration
            Console.Write("Enter the object key: ");
            string key = Console.ReadLine();
            Console.Write("How long should this tag be valid?: ");
            string hours = Console.ReadLine();

            // create expiration based on input
            DateTime expiration = DateTime.Now.AddHours(Convert.ToDouble(hours));

            // create the request object to generate pre-signed url
            GetPreSignedUrlRequest request = new GetPreSignedUrlRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET,
                Key = key,
                Expires = expiration,
                Verb = HttpVerb.GET,
            };

            // get pre-signed url
            string url = s3.GetPreSignedURL(request);

            // print objects pre-signed url
            Console.WriteLine(string.Format("Object {0}/{1} pre-signed url: {2}", AWSS3Factory.S3_BUCKET, key, url));
            Console.ReadLine();
        }
    }
}
