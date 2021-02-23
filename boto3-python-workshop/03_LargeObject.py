import io
import sys
import GetConnection

testBucketName = "testtest"
testPath = "testfile"
testFile = "Copy of IDP.xlsx"
testUpload = "testLarge"

def download(s3, localFileName, s3Bucket, s3ObjectKey):

    response = s3.head_object(Bucket=s3Bucket, Key=s3ObjectKey)
    length = int(response['ContentLength'])
    downloaded = 0

    def progress(chunk):
        nonlocal downloaded
        downloaded += chunk
        done = int(50 * downloaded / length)
        sys.stdout.write("\r[%s%s]" % ('=' * done, ' ' * (50-done)) )
        sys.stdout.flush()

    print(f'Downloading {s3ObjectKey}')
    with open(localFileName, 'wb') as f:
        s3.download_fileobj(s3Bucket, s3ObjectKey, f, Callback=progress)


if __name__ == '__main__':
    s3 = GetConnection.getConnection()
    # download a large object with callback example
    download(s3, testPath, testBucketName, testFile)
    # upload a large object with multipart
    # response: class dict
        # AbortDate (datetime): the time If the bucket has a lifecycle rule configured with an action to abort incomplete multipart uploads
        # UploadId (string): id for this multipart upload
    response = s3.create_multipart_upload(Bucket=testBucketName, Key=testUpload)
    uploadId = response["UploadId"]
    # generate a list of dict which contains info for each mutipart upload, the complete_multipart_upload will need this info
    parts = []
    for i in range(5):
        response = s3.upload_part(Body=io.BytesIO(bytes("part: " + str(i + 1), encoding="utf-8")),
                                  Bucket=testBucketName,
                                  Key=testUpload,
                                  PartNumber=i + 1,
                                  UploadId=uploadId)
        parts.append({"PartNumber": i + 1, "ETag": response["ETag"]})
    response = s3.complete_multipart_upload(Bucket=testBucketName,
                                            Key=testUpload,
                                            UploadId=uploadId,
                                            MultipartUpload={"Parts": parts})
    # get and print the raw data
    response = s3.get_object(
        Bucket=testBucketName,
        Key=testUpload,
        Range='bytes=0-40',
    )
    raw = response["Body"].read(amt=40)
    print("raw data get from the largeFile" + raw.decode())

    # how to abort the multipart upload
    response = s3.create_multipart_upload(Bucket=testBucketName, Key=testUpload)
    uploadId = response["UploadId"]
    response = s3.abort_multipart_upload(Bucket=testBucketName, Key=testUpload, UploadId=uploadId)
