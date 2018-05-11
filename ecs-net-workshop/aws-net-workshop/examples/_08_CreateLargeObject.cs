using System;
using ECSSDK.S3;
using Amazon.S3.Model;
using System.Collections.Generic;
using System.IO;

namespace ecs_net_workshop.examples
{
    class _08_CreateLargeObject
    {
        public static void Main(string[] args)
        {
            // create the ECS S3 client
            ECSS3Client s3 = ECSS3Factory.getS3Client();

            // retrieve the object key/value from user
            Console.Write("Enter the object key: ");
            string key = Console.ReadLine();
            Console.Write("Enter the file location: ");
            string filePath = Console.ReadLine();

            // grab the start time of upload
            DateTime startDate = DateTime.Now;

            // part size for chunking in multi-part
            long partSize = 1024 * 1024 * 8; // 2 MB

            // list of upload part response objects for each part that is uploaded
            List<PartETag> partETags = new List<PartETag>();

            // Step 1: Initialize
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest()
            {
                BucketName = ECSS3Factory.S3_BUCKET,
                Key = key,
            };

            InitiateMultipartUploadResponse initResponse = s3.InitiateMultipartUpload(initRequest);

            // get the file and file length
            long contentLength = new FileInfo(filePath).Length;

            Console.WriteLine(string.Format("Starting multi-part upload for object {0}/{1} with file path {2} and size {3} in {4} MB size chunks", 
                ECSS3Factory.S3_BUCKET, key, filePath, Convert.ToString(contentLength), partSize / 1024 / 1024));

            try
            {
                // Step 2: Upload parts
                long filePosition = 0;
                for (int i = 1; filePosition < contentLength; i++)
                {
                    // get the size of the chunk. Note - the last part can be less than the chunk size
                    partSize = Math.Min(partSize, (contentLength - filePosition));

                    Console.WriteLine(string.Format("Sending chunk {0} starting at position {1}", i, filePosition));

                    // create request to upload  a part
                    UploadPartRequest uploadRequest = new UploadPartRequest()
                    {
                        BucketName = ECSS3Factory.S3_BUCKET,
                        Key = key,
                        UploadId = initResponse.UploadId,
                        PartNumber = i,
                        FilePosition = filePosition,
                        FilePath = filePath,                        
                        PartSize = partSize                        
                    };

                    UploadPartResponse partResponse = s3.UploadPart(uploadRequest);
                    PartETag eTagPart = new PartETag(partResponse.PartNumber, partResponse.ETag);
                    partETags.Add(eTagPart);

                    filePosition = filePosition += partSize;
                }

                // Step 3: complete
                Console.WriteLine("Waiting for completion of multi-part upload");
                CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest() {
                    BucketName = ECSS3Factory.S3_BUCKET,
                    Key = key,
                    UploadId = initResponse.UploadId,
                    PartETags = partETags
                };

                s3.CompleteMultipartUpload(compRequest);
            }
            catch (Exception e)
            {
                s3.AbortMultipartUpload(new AbortMultipartUploadRequest() {
                    BucketName = ECSS3Factory.S3_BUCKET,
                    Key = key,
                    UploadId = initResponse.UploadId
                });

                Console.WriteLine(e);
            }

            // grab the end time of upload
            DateTime endDate = DateTime.Now;

            Console.WriteLine(string.Format("Completed multi-part upload for object {0}/{1} with file path: {2}", ECSS3Factory.S3_BUCKET, key, filePath));
            Console.WriteLine(string.Format("Process took: {0} seconds.", (endDate - startDate).TotalSeconds.ToString()));
            Console.ReadLine();
        }
    }
}
