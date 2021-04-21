# refer to https://boto3.amazonaws.com/v1/documentation/api/latest/reference/services/s3.html#S3.Client.put_object_acl

import GetConnection
import io

testBucketName = "testtest"
fileInRam = io.BytesIO(b'data stored in RAM')
testObject = 'RAMFile'


if __name__ == '__main__':
    s3 = GetConnection.getConnection()
    response = s3.upload_fileobj(fileInRam, testBucketName, testObject, Callback=None)
    # use the access control policy
    # by default, user ID is same with the DisplayName
    response = s3.put_object_acl(
        AccessControlPolicy={
            'Grants': [
                {
                    'Grantee': {
                        'ID': 'obuser-1',
                        'Type': 'CanonicalUser',
                        'DisplayName': 'obuser-1',
                    },
                    'Permission': 'FULL_CONTROL',
                },
            ],
            'Owner': {
                'DisplayName': 'obuser-1',
                'ID': 'obuser-1'
            }
        },
        Bucket=testBucketName,
        Key=testObject
    )

    # response: class dict
        # ResponseMetadata: class dict
            # RequestId
            # HTTPStatusCode
            # ...
        # Owner: dict
        # Grants: list of grant dict
    response = s3.get_object_acl(
        Bucket=testBucketName,
        Key=testObject
    )

    print("owner info: " + str(response["Owner"]))
    print("grants info: " + str(response["Grants"]))

    # simple switch for one user one time
    response = s3.put_object_acl(
        # GrantFullControl='string',
        # GrantReadACP='string',
        # GrantWrite='string',
        # GrantWriteACP='string',
        GrantRead='id=obuser-1',
        Bucket=testBucketName,
        Key=testObject
    )

    response = s3.get_object_acl(
        Bucket=testBucketName,
        Key=testObject
    )

    print("owner info: " + str(response["Owner"]))
    print("grants info: " + str(response["Grants"]))
