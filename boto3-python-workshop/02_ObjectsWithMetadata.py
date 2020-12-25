import GetConnection

metadata = {'testkey': 'testvalue'}
objectName = "testmetadata"
testBucketName = "testtest"

if __name__ == '__main__':
    s3 = GetConnection.getConnection()
    # put object with metadata
    response = s3.put_object(
        Body=objectName + ' raw data',
        Bucket=testBucketName,
        Key=objectName,
        Metadata=metadata,
    )
    # get the object system metadata and user metadata without getting the object itself
    response = s3.head_object(
        Bucket=testBucketName,
        Key=objectName,
    )
    print("Metadata:")
    print(response["Metadata"])