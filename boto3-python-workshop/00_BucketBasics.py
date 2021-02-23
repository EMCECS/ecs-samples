import botocore
import GetConnection

bucket_names = ["testb1", "testb2", "testb3"]

if __name__ == '__main__':
    s3 = GetConnection.getConnection()
    # create bucket
    for bucket in bucket_names:
        # response: class dict
            # ResponseMetadata: class dict
                # RequestId
                # HTTPStatusCode
                # ...
            # Location: class str the path of the bucket
        response = s3.create_bucket(Bucket=bucket)

    # list bucket
    buckets = s3.list_buckets()
    for bucket in buckets['Buckets']:
        print(bucket['Name'])

    for bucket in bucket_names:
        try:
            # delete bucket
            # response: class dict
                # ResponseMetadata: class dict
                    # RequestId
                    # HTTPStatusCode
                    # ...
            response = s3.delete_bucket(Bucket=bucket)
        # error handling example
        except botocore.exceptions.ClientError as err:
            if err.response['Error']['Code'] == 'BucketNotEmpty':
                print("bucket not empty")
            raise err

    buckets = s3.list_buckets()
    for bucket in buckets['Buckets']:
        print(bucket['Name'])
