using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using System.IO;
using System.Configuration;
using System.Windows.Forms;
using FileHelpers;
using Amazon.S3;
using Amazon.Runtime;
using Amazon.S3.Model;

namespace ecspics_loader
{
    public partial class Main : Form
    {
        public string ENDPOINT_URL;
        public string USER_NAME;
        public string SECRET_KEY;
        public string BUCKET_NAME;
        public int PICS_TO_UPLOAD;
        public int PICS_UPLOADED;

        public Main()
        {
            InitializeComponent();
        }

        private async void cmdUpload_Click(object sender, EventArgs e)
        {
            txtStatus.Text = string.Empty;
            cmdUpload.Enabled = false;

            USER_NAME = txtUserName.Text;
            SECRET_KEY = txtSecretKey.Text;
            BUCKET_NAME = txtBucketName.Text;
            PICS_TO_UPLOAD = Convert.ToInt16(cmbPictureCount.Text);
            PICS_UPLOADED = 0;

            List<Picture> picturesToUpload = new List<Picture>();

            DateTime startDate = DateTime.Now;

            CancellationTokenSource cts = new CancellationTokenSource();

            // Uri
            string host = string.Empty;
            string scheme = string.Empty;
            string port = string.Empty;
            bool useHttp = true;

            if (USER_NAME.Length == 0 || SECRET_KEY.Length == 0 || BUCKET_NAME.Length == 0 || PICS_TO_UPLOAD == 0 || txtIpAddress.Text.Length == 0)
            {
                updateStatus("Please provide an endpoint ip, user name, secret key, bucket name and number of pictures to upload.");
                cmdUpload.Enabled = true;
                return;                
            }

            try {

                port = cmbPort.Text;
                host = txtIpAddress.Text;

                if (port == "9020")
                {
                    scheme = "http://";
                }
                else
                {
                    scheme = "https://";
                    useHttp = true;
                }

                ENDPOINT_URL = string.Format("{0}{1}:{2}", scheme, host, port);

                //bool isValid = System.Net.IPAddress.TryParse(txtIpAddress.Text, out ip);
                //if (isValid)
                //{
                //    temp = string.Format("{0}{1}:{2}", scheme, ip, port);
                //}
                //else
                //{
                //    updateStatus("Please provide a valid IP address.");
                //    cmdUpload.Enabled = true;
                //    return;
                //}

                //ENDPOINT_URL = new Uri(temp);
            }
            catch (Exception ex)
            {
                updateStatus(ex.Message);
                cmdUpload.Enabled = true;
                return;
            }

            var progress = new Progress<UploadProgress>(update =>
            {
                if (update.cancelLoop)
                {
                    cts.Cancel();
                }

                if (update.errorMessage != null)
                {
                    updateStatus(update.errorMessage);
                }
                else
                {
                    double percentage = (double)PICS_UPLOADED / PICS_TO_UPLOAD;
                    updateStatus(string.Format("{0} of {1} uploaded - size {2}b - progress: {3}", Convert.ToString(PICS_UPLOADED), Convert.ToString(PICS_TO_UPLOAD), update.imageSize, percentage.ToString("0%")));
                }
            });

            updateStatus("Checking that specified bucket exists.");

            bool result = false;

            await Task.Run(() =>
            {
                IProgress<UploadProgress> report = progress;

                try
                {
                    BasicAWSCredentials creds = new BasicAWSCredentials(USER_NAME, SECRET_KEY);

                    AmazonS3Config cc = new AmazonS3Config()
                    {
                        ServiceURL = ENDPOINT_URL,
                        ForcePathStyle = true,
                        UseHttp = useHttp,
                        Timeout = TimeSpan.FromSeconds(5),
                        MaxErrorRetry = 0
                    };

                    AmazonS3Client client = new AmazonS3Client(creds, cc);
                    ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
                    ListBucketsResponse listBucketsResponse = client.ListBuckets(listBucketsRequest);

                    foreach (var bucket in listBucketsResponse.Buckets)
                    {
                        if (bucket.BucketName == BUCKET_NAME)
                        {
                            result = true;
                        }
                    }

                    if (!result)
                    {
                        report.Report(new UploadProgress { errorMessage = "Unable to locate specified bucket.  Did you provide the correct bucket name?" });
                    }
                }
                catch (AmazonS3Exception amazonS3Exception)
                {
                    report.Report(new UploadProgress { errorMessage = string.Format("An error occured while communicating with ECS.  Error: {0}", amazonS3Exception.Message) });
                }
                catch(Exception ex)
                {
                    report.Report(new UploadProgress { errorMessage = "An error occured while attempting to access specified bucket.  Please check all provided parameters." });
                }
            });

            if (!result)
            {
                cmdUpload.Enabled = true;
                return;
            }

            updateStatus(string.Format("Successfully verified that bucket '{0}' exists.", BUCKET_NAME));

            var engine = new FileHelperEngine<Pictures>();
            var records = engine.ReadFile(ConfigurationManager.AppSettings["CSV_FILE_LOCATION"]);

            int recordsIndex = 0;

            foreach (var record in records)
            {
                recordsIndex++;

                int height = record.height;
                int width = record.width;
                double longitude = record.longitude;
                double latitude = record.latitude;
                int viewCount = record.viewCount;
                string fileName = record.fileName;
                string filePath = string.Format("{0}\\{1}", ConfigurationManager.AppSettings["IMAGE_FOLDER_LOCATION"], fileName);
                string thumbPath = string.Format("{0}\\thumbnails\\{1}", ConfigurationManager.AppSettings["IMAGE_FOLDER_LOCATION"], fileName);

                picturesToUpload.Add(new Picture(recordsIndex, height, width, fileName, filePath, thumbPath, longitude, latitude, viewCount, this));

                if (!(recordsIndex < PICS_TO_UPLOAD)) { break; }
            }

            await Task.Run(() => {

                ParallelOptions po = new ParallelOptions();
                po.CancellationToken = cts.Token;

                try
                {
                    Parallel.ForEach(picturesToUpload, po, (image) =>
                    {
                        try
                        {
                            image.uploadTheImage(progress);
                        }
                        finally
                        {
                            Interlocked.Increment(ref PICS_UPLOADED);
                        }
                    });
                } catch(OperationCanceledException ex)
                {
                    
                }
            });

            if (!cts.IsCancellationRequested) {
                DateTime endDate = DateTime.Now;
                updateStatus(string.Format("Done:  Process took: {0} seconds.", (endDate - startDate).TotalSeconds.ToString()));
            }

            cmdUpload.Enabled = true;

        }


        public sealed class UploadProgress
        {
            public int imageSize { get; set; }
            public string imageName { get; set; }
            public string errorMessage { get; set; }
            public bool cancelLoop { get; set; }
        }


        public void updateStatus(string message)
        {
            string statusUpdate = string.Format("{0}{1}", message, Environment.NewLine);
            txtStatus.AppendText(statusUpdate);
        }

        public class Picture
        {
            internal int height;
            internal int width;
            internal string fileName;
            internal string thumbPath;
            internal string filePath;
            internal double longitude;
            internal double latitude;
            internal int viewCount;
            internal Main parent;
            internal int index;


            internal Picture(int index, int height, int width, string fileName, string filePath, string thumbPath, double longitude, double latitude, int viewCount, Main parent)
            {
                this.height = height;
                this.width = width;
                this.filePath = filePath;
                this.thumbPath = thumbPath;
                this.fileName = fileName;
                this.longitude = longitude;
                this.latitude = latitude;
                this.viewCount = viewCount;
                this.parent = parent;
                this.index = index;
            }

            public bool uploadTheImage(IProgress<UploadProgress> progress)
            {
                PutObjectRequest putObjectRequest_Image;
                PutObjectRequest putObjectRequest_Thumb;
                byte[] payload;
                byte[] thumbnail;

                try
                {
                    if (!File.Exists(filePath))
                    {
                        progress.Report(new UploadProgress { errorMessage = string.Format("Image does not exist on disk: {0}.", filePath) });
                        return false;
                    }

                    payload = File.ReadAllBytes(filePath);
                    thumbnail = File.ReadAllBytes(thumbPath);

                    putObjectRequest_Thumb = new PutObjectRequest()
                    {
                        InputStream = new MemoryStream(thumbnail),
                        BucketName = parent.BUCKET_NAME,
                        Key = "thumbnails/" + fileName
                    };

                    putObjectRequest_Image = new PutObjectRequest()
                    {
                        InputStream = new MemoryStream(payload),
                        BucketName = parent.BUCKET_NAME,
                        Key = fileName //"pictures/" + fileName
                    };

                    putObjectRequest_Image.Metadata.Add("x-amz-meta-image-width", Convert.ToString(viewCount));
                    putObjectRequest_Image.Metadata.Add("x-amz-meta-image-height", Convert.ToString(height));
                    putObjectRequest_Image.Metadata.Add("x-amz-meta-image-viewcount", Convert.ToString(width));
                    putObjectRequest_Image.Metadata.Add("x-amz-meta-gps-latitude", Convert.ToString(latitude));
                    putObjectRequest_Image.Metadata.Add("x-amz-meta-gps-longitude", Convert.ToString(longitude));

                    try
                    {
                        BasicAWSCredentials creds = new BasicAWSCredentials(parent.USER_NAME, parent.SECRET_KEY);

                        AmazonS3Config cc = new AmazonS3Config()
                        {
                            ServiceURL = parent.ENDPOINT_URL,
                            ForcePathStyle = true,
                            UseHttp = Convert.ToBoolean(ConfigurationManager.AppSettings["S3_USE_HTTP"])
                        };

                        AmazonS3Client client = new AmazonS3Client(creds, cc);

                        client.PutObject(putObjectRequest_Image);
                        client.PutObject(putObjectRequest_Thumb);
                        progress.Report(new UploadProgress { imageSize = payload.Length, imageName = fileName, errorMessage = null });

                    }
                    catch (AmazonS3Exception amazonS3Exception)
                    {
                        if (amazonS3Exception.ErrorCode != null && (amazonS3Exception.ErrorCode.Equals("InvalidAccessKeyId") || amazonS3Exception.ErrorCode.Equals("InvalidSecurity")))
                        {
                            progress.Report(new UploadProgress { errorMessage = "Please check the provided AWS Credentials." });
                        }
                        else
                        {
                            progress.Report(new UploadProgress { errorMessage = string.Format("An Error, number {0}, occurred when creating the object with the message '{1}", amazonS3Exception.ErrorCode, amazonS3Exception.Message) });
                        }

                        progress.Report(new UploadProgress { cancelLoop = true });
                    }
                }
                catch (Exception e)
                {
                    progress.Report(new UploadProgress { errorMessage = string.Format("Image upload failed.  Moving to next image.  Error: {0}", e.ToString()) });
                }
                finally
                {
                    putObjectRequest_Image = null;
                    putObjectRequest_Thumb = null;
                }

                return true;
            }
        }
    }    
}
