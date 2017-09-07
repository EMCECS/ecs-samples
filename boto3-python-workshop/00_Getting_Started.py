from boto3.s3.transfer import TransferConfig, S3Transfer
import boto3

service_name = 's3'

# The hostname or IP address of your ECS system.
host = ''

# Your AWS access key ID is also known in ECS as your object user
access_key_id = ''

# The secret key that belongs to your object user.
secret_key = ''


s3 = boto3.client(
    service_name,
    aws_access_key_id=access_key_id,
    aws_secret_access_key=secret_key,
    endpoint_url=host,
)

'''
Create a bucket
'''
bucket_name = "python-workshop"
bucket = s3.create_bucket(Bucket=bucket_name)

config = TransferConfig(
    multipart_threshold=2 * 1024 * 1024,
    max_concurrency=10,
    num_download_attempts=10,
)

transfer = S3Transfer(s3, config)

transfer.upload_file('vnx-demo.mp4', bucket_name, 'mpu-1')



# Call S3 to list current buckets
#response = s3.list_buckets()

# Get a list of all bucket names from the response
#buckets = [bucket['Name'] for bucket in response['Buckets']]

# Print out the bucket list
#print("Bucket List: %s" % buckets)