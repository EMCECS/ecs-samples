import GetConnection

testNamespace = "testns1"
testBucketName = "testtest"
testObjectName = "testLarge"

if __name__ == '__main__':
    s3 = GetConnection.getConnection()
    # put bucket life cycle rule
    response = s3.put_bucket_lifecycle(
        Bucket=testBucketName,
        # currently ecs only support the follow rule fields
        LifecycleConfiguration={
            'Rules': [
                {
                    'Expiration': {
                        'Days': 123,
                    },
                    'ID': 'rule1',
                    'Prefix': 'test',
                    'Status': 'Enabled',
                    'Transition': {
                        'Days': 123,
                    },
                    'NoncurrentVersionExpiration': {
                        'NoncurrentDays': 123
                    },
                    'AbortIncompleteMultipartUpload': {
                        'DaysAfterInitiation': 123
                    }
                },
            ]
        },
    )
    # get bucket life cycle
    # if bucket police not exist
    # will raise botocore.exceptions.ClientError.response['Error']['Code'] == NoSuchBucketPolicy
    response = s3.get_bucket_lifecycle(Bucket=testBucketName)
    print(response["Rules"])
