using System;
using Amazon.S3;
using Amazon.S3.Model;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using System.Linq;

namespace aws_net_workshop.examples_bonus
{
    class _08_CreateLargeObjectAsync
    {
        static long fileSize = 0;

        public static void Main(string[] args)
        {
            // retrieve the object key/value from user
            Console.Write("Enter the object key: ");
            string key = Console.ReadLine();
            Console.Write("Enter the file location: ");
            string filePath = Console.ReadLine();

            DateTime startDate = DateTime.Now;

            MainSync(key, filePath).Wait();

            DateTime endDate = DateTime.Now;

            double totalSeconds = (endDate - startDate).TotalSeconds;

            Console.WriteLine(string.Format("Completed multi-part upload for object {0}/{1} with file path: {2}", AWSS3Factory.S3_BUCKET, key, filePath));
            Console.WriteLine(string.Format("Process took: {0} seconds.", totalSeconds.ToString()));
            Console.WriteLine(string.Format("Transfer rate: {0} megabytes per second.", (((fileSize / totalSeconds) / 1024) / 1024).ToString()));
            Console.ReadLine();
        }

        public static async Task MainSync(string key, string filePath)
        {
            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            // part size for chunking in multi-part
            long partSize = 1024 * 1024 * 2; // 2 MB

            // list of upload part response objects for each part that is uploaded
            IEnumerable<PartETag> partETags = new List<PartETag>();

            // Step 1: Initialize
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest()
            {
                BucketName = AWSS3Factory.S3_BUCKET,
                Key = key,
            };

            // call initialize method -obtain upload id to be used for subsequent parts.
            InitiateMultipartUploadResponse initResponse = s3.InitiateMultipartUpload(initRequest);

            // get the file and file length
            fileSize = new FileInfo(filePath).Length;

            Console.WriteLine(string.Format("Starting multi-part upload for object {0}/{1} with file path {2} and size {3} in {4} MB size chunks",
                AWSS3Factory.S3_BUCKET, key, filePath, Convert.ToString(fileSize), partSize / 1024 / 1024));

            try
            {
                // STEP 2: generate list of parts to be uploaded
                long filePosition = 0;

                // the parts list representing each chunk to be uploaded
                List<UploadPartRequest> parts = new List<UploadPartRequest>();

                for (int i = 1; filePosition < fileSize; i++)
                {
                    // get the size of the chunk. Note - the last part can be less than the chunk size
                    partSize = Math.Min(partSize, (fileSize - filePosition));

                    // create request to upload  a part
                    UploadPartRequest uploadRequest = new UploadPartRequest()
                    {
                        BucketName = AWSS3Factory.S3_BUCKET,
                        Key = key,
                        UploadId = initResponse.UploadId,
                        PartNumber = i,
                        FilePosition = filePosition,
                        FilePath = filePath,
                        PartSize = partSize
                    };

                    parts.Add(uploadRequest);

                    filePosition = filePosition += partSize;
                }

                // generate query to simultaneously upload chunks
                IEnumerable<Task<PartETag>> uploadTasksQuery = 
                    from part in parts select ProcessChunk(part);

                // execute the upload query
                List<Task<PartETag>> uploadTasks = uploadTasksQuery.ToList();

                //
                // Can do other work here while waiting ...
                Console.WriteLine("Waiting for completion of multi-part upload");
                //
                //

                // wait here for the query to complete
                partETags = await Task.WhenAll(uploadTasks);

                // STEP 3: complete the mpu
                CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest()
                {
                    BucketName = AWSS3Factory.S3_BUCKET,
                    Key = key,
                    UploadId = initResponse.UploadId,
                    PartETags = partETags.ToList()
                };

                s3.CompleteMultipartUpload(compRequest);
            }
            catch (Exception e)
            {
                s3.AbortMultipartUpload(new AbortMultipartUploadRequest()
                {
                    BucketName = AWSS3Factory.S3_BUCKET,
                    Key = key,
                    UploadId = initResponse.UploadId
                });

                Console.WriteLine(e);
            }
        }

        private static async Task<PartETag> ProcessChunk(UploadPartRequest upr)
        {
            Console.WriteLine(string.Format("Sending chunk {0} starting at position {1}", upr.PartNumber, upr.FilePosition));

            // upload the chucnk and return a new PartETag when upload completes
            UploadPartResponse response = await AWSS3Factory.getS3Client().UploadPartAsync(upr, new System.Threading.CancellationToken());

            return new PartETag(response.PartNumber, response.ETag);
        }
    }
}
