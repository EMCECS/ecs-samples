import GetConnection
import io
import time

testBucketName = "testbr"
retention = 5
fileInRam = io.BytesIO(b'data stored in RAM')

if __name__ == '__main__':
    s3 = GetConnection.getConnection()

    # insert retention header through Boto3's event system.
    # https://boto3.amazonaws.com/v1/documentation/api/latest/guide/events.html
    event_system = s3.meta.events
    def add_retention_header(request, **kwargs):
        request.headers.add_header('x-emc-retention-period', str(retention))
    event_system.register_first('before-sign.s3.CreateBucket', add_retention_header)
    response = s3.create_bucket(Bucket=testBucketName)
    print("Created bucket " + testBucketName + " with retention period " + str(retention) + " seconds.")
    event_system.unregister('before-sign.s3.CreateBucket', add_retention_header)
    response = s3.upload_fileobj(fileInRam, testBucketName, 'RAMFile', Callback=None)
    try:
        response = s3.delete_object(
            Bucket=testBucketName,
            Key='RAMFile',
        )
        print("error, should not be deleted")
    except Exception as e:
        print("Expected exception: " + str(e));
    print("wait for " + str(retention) + " seconds.")
    time.sleep(retention)
    response = s3.delete_object(
        Bucket=testBucketName,
        Key='RAMFile',
    )
    print("deleted the object")
    # please remember to unregister the event
    response = s3.delete_bucket(Bucket=testBucketName)
    print("deleted the bucket")

