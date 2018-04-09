using System;
using Amazon.S3;
using Amazon.S3.Model;

namespace aws_net_workshop.examples
{
    class _09_ListObjects
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
            "20141103/account-11133/bill.html"
        };

        public static void Main(string[] args)
        {
            // create the AWS S3 client
            AmazonS3Client s3 = AWSS3Factory.getS3Client();

            foreach (string key in KEY_LIST) {
                // create object request with retrieved input
                PutObjectRequest request = new PutObjectRequest()
                {
                    BucketName = AWSS3Factory.S3_BUCKET,
                    ContentBody = key,
                    Key = key
                };

                // create the object in demo bucket
                s3.PutObject(request);
            }


            while (true)
            {
                Console.Write("Enter the prefix (empty if none): ");
                string prefix = Console.ReadLine();
                Console.Write("Enter the delimiter (e.g. /, empty for none): ");
                string delimiter = Console.ReadLine();
                Console.Write("Enter the marker (empty if none): ");
                string marker = Console.ReadLine();
                Console.Write("Enter the max keys (empty for defaul): ");
                string maxKeys = Console.ReadLine();

                ListObjectsRequest request = new ListObjectsRequest()
                {
                    BucketName = AWSS3Factory.S3_BUCKET

                };

                if (prefix.Length > 0)
                    request.Prefix = prefix;

                if (delimiter.Length > 0)
                    request.Delimiter = delimiter;

                if (marker.Length > 0)
                    request.Marker = marker;

                if (maxKeys.Length > 0)
                    request.MaxKeys = Int32.Parse(maxKeys);

                ListObjectsResponse response = s3.ListObjects(request);

                Console.WriteLine("-----------------");
                Console.WriteLine("Bucket: " + AWSS3Factory.S3_BUCKET);
                Console.WriteLine("Prefix: " + response.Prefix);
                Console.WriteLine("Delimiter: " + response.Delimiter);
                Console.WriteLine("Marker: " + marker);
                Console.WriteLine("IsTruncated? " + response.IsTruncated);
                Console.WriteLine("NextMarker: " + response.NextMarker);

                Console.WriteLine();

                if (response.CommonPrefixes != null)
                {
                    foreach (string commonPrefix in response.CommonPrefixes)
                    {
                        Console.WriteLine("CommonPrefix: " + commonPrefix);
                    }
                }

                Console.WriteLine("Printing objects");
                Console.WriteLine("-----------------");


                foreach (S3Object s3Object in response.S3Objects)
                {
                    Console.WriteLine(String.Format("{0}    {1}     {2}", s3Object.LastModified.ToString(), s3Object.Size, s3Object.Key));
                }

                Console.Write("Another? (Y/N) ");
                string another = Console.ReadLine();

                if (another.ToUpper() == "N")
                    break;

            }

            foreach (string key in KEY_LIST)
            {
                s3.DeleteObject(AWSS3Factory.S3_BUCKET, key);
            }
                
            Console.ReadLine();
        }
    }
}
