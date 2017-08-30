'''
Import the boto library
*** Note: as of early 2015, the new 'boto3' library does NOT support non-AWS endpoints and cannot yet be used with ECS.

To install boto, use the Python package manager, pip

pip install boto
'''
from boto.s3.connection import S3Connection, OrdinaryCallingFormat

'''
First, define your connection information
'''

# The hostname or IP address of your ECS system.
host = "object.ecstestdrive.com"

# ECS runs S3 with SSL/TLS on 9021 and plaintext on 9020.  If you're behind a load balancer this will usually be
# remapped to 80/443.
port = 443

# Whether to use SSL.  'True' if port 9021 and 'False' for port 9020.  If you're using a load balancer, SSL would be
# port 443.
secure = port == 9021 or port == 443

# Your AWS access key ID is also known in ECS as your object user
access_key_id = 'user1'

# The secret key that belongs to your object user.
secret_key = ''

# We can turn on debug level 2 here so you can see the HTTP request/response traffic.
debug_level = 2

# I generally recommend this when using boto to force path-style bucket addressing
calling_format = OrdinaryCallingFormat()

'''
Create the S3Connection object
'''
s3 = S3Connection(aws_access_key_id=access_key_id, aws_secret_access_key=secret_key, is_secure=secure, port=port,
                  host=host, debug=debug_level,
                  calling_format=calling_format)
# Some other useful parameters not used above:
#  proxy, proxy_port, proxy_user, proxy_pass: use these parameters to configure a proxy.  This is useful for debugging
#                                             with tools like fiddler or Charles Proxy or for some corporate
#                                             environments that require proxies
#
#  validate_certs: Set this to False if you're testing SSL connections against an ECS with a self-signed certificate.
#                  **NOT** suitable for production use.
#

# Since the above doesn't actually connect to the server, execute some sort of operation to validate the connection
# information.
s3.get_all_buckets()

'''
Create a bucket
'''
bucket_name = "mybucket"
bucket = s3.create_bucket(bucket_name)

'''
Create an object in the bucket
'''
key_name = "hello.txt"
key = bucket.new_key(key_name)
key.set_contents_from_string("Hello World!", {"content-type": "text/plain"})

'''
Read back the object
'''
key = bucket.get_key(key_name)
print key.get_contents_as_string()

'''
Create a shareable URL
'''
one_minute = 60
print "GET URL valid for one minute: %s" % key.generate_url(one_minute)

'''
Server-side copy
'''
copy_name = "hello2.txt"
copy_key = key.copy(bucket_name, copy_name)

print "Contents of hello2.txt: %s" % copy_key.get_contents_as_string()

'''
Create an object with metadata.
'''
metadata_object_name = "hello_with_meta.txt"
metadata = {"content-type": "text/plain",
            "x-amz-meta-foo": "bar"}
metadata_object_key = bucket.new_key(metadata_object_name)
metadata_object_key.set_contents_from_string("Hello With Metadata!", metadata)

'''
Update metadata is done by copying to self.  Note that it's a complete overwrite of metadata!
'''
metadata = {"content-type": "text/plain",
            "x-amz-meta-foo": "baz"}
metadata_object_key.copy(bucket_name, metadata_object_key, metadata)

metadata_object_key = bucket.get_key(metadata_object_name)
print "Metadata: foo=%s" % metadata_object_key.get_metadata("foo")
print "Metadata: ping=%s" % metadata_object_key.get_metadata("ping")

'''
Delete the objects
'''
key.delete()
copy_key.delete()
metadata_object_key.delete()

'''
Delete the bucket
'''
bucket.delete()
