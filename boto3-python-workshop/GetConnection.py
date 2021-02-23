import boto3

# set connection sample code
def getConnection() -> boto3.client:
    # host address
    # ECS runs S3 with SSL/TLS on 9021 and plaintext on 9020.  If you're behind a load balancer this will usually be
    # remapped to 80/443.
    host = "http://10.246.153.115:9020"
    secure = False
    # Your AWS access key ID is also known in ECS as your object user
    access_key_id = 'testuser'
    # The secret key that belongs to your object user.
    secret_key = '0OCHNOW2RKzl407+m8EhGIQ7gKiMY3O99W9xJNh6'
    s3 = boto3.client('s3', aws_access_key_id=access_key_id, aws_secret_access_key=secret_key, use_ssl=secure,
                      endpoint_url=host)
    # boto3.client
    # https://boto3.amazonaws.com/v1/documentation/api/latest/reference/services/s3.html#client
    return s3


if __name__ == '__main__':
    s3 = getConnection()
    buckets = s3.list_buckets()
    for bucket in buckets['Buckets']:
        print(bucket['Name'])
