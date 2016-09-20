using System;
using Amazon.S3;
using Amazon.S3.Transfer;

namespace aws_net_workshop.examples
{
    class _09_CreateLargeObject_TransferUtility
    {
        public static void Main(string[] args)
        {
            // grab the start time of upload
            DateTime startDate = DateTime.Now;

            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            // create the transfer utility using AWS S3 client
            TransferUtility fileTransferUtility = new TransferUtility(s3);

            // retrieve the object key/value from user
            Console.Write("Enter the object key: ");
            string key = Console.ReadLine();
            Console.Write("Enter the file location: ");
            string filePath = Console.ReadLine();

            // configure transfer utility for parallel upload
            TransferUtilityUploadRequest uploadRequest = new TransferUtilityUploadRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET,
                FilePath = filePath,
                StorageClass = S3StorageClass.Standard,
                PartSize = 1024 * 1024 * 16, // 16MB
                Key = key,
            };

            // upload the file
            fileTransferUtility.Upload(uploadRequest);

            // grab the end time of upload
            DateTime endDate = DateTime.Now;

            Console.WriteLine(string.Format("Completed multi-part upload for object {0}/{1} with file path: {2}", AWSS3Factory.S3_BUCKET, key, filePath));
            Console.WriteLine(string.Format("Process took: {0} seconds.", (endDate - startDate).TotalSeconds.ToString()));

            Console.ReadLine();
        }
    }
}
