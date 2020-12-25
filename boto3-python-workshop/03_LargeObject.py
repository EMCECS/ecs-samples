import sys
import GetConnection

testBucketName = "testtest"
testPath = "testfile"
testFile = "Copy of IDP.xlsx"

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
    download(s3, testPath, testBucketName, testFile)