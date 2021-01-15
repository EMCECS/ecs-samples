import GetConnection

testBucketName = "testtest"
testdata = "Hello World!"
replace = "dell ecs!"
testName = "testappend"

# ecs support append update: update an object with offset/range
# however, AWS does not support it, thus we need to inject an custom range header in boto3
if __name__ == '__main__':
    s3 = GetConnection.getConnection()
    # upload a test file
    response = s3.put_object(
        Bucket=testBucketName,
        Body=testdata,
        Key=testName,
    )
    response = s3.get_object(
        Bucket=testBucketName,
        Key=testName,
        Range='bytes=0-' + str(len(testdata)),
    )
    raw = response["Body"].read(amt=len(testdata))
    print("Before append update: " + raw.decode())
    offset = testdata.find("World")
    # insert Range header through Boto3's event system.
    # https://boto3.amazonaws.com/v1/documentation/api/latest/guide/events.html

    # def add_range_header(request, **kwargs):
    #     request.headers.add_header('Range', 'bytes=' + str(offset) + '-' + str(offset + len("dell ecs") - 1))
    event_system = s3.meta.events
    event_system.register_first('before-sign.s3.PutObject',
                                lambda request, **kwargs: request.headers.add_header(
                                    'Range', 'bytes=' + str(offset) + '-' + str(offset + len(replace) - 1)))
    response = s3.put_object(
        Bucket=testBucketName,
        Body=replace,
        Key=testName,
    )
    # please remember to unregister the event, otherwise all the PutObject will use the header
    event_system.unregister('before-sign.s3.PutObject')
    response = s3.get_object(
        Bucket=testBucketName,
        Key=testName,
    )
    raw = response["Body"].read(amt=offset + len(replace))
    print("After append update: " + raw.decode())
