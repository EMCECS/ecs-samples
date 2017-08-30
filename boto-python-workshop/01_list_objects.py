'''
Same stuff as 00_getting_started.  If you've pasted all this into your python interpreter already, you can
skip past this.
'''
from boto.s3.connection import S3Connection, OrdinaryCallingFormat
from boto.s3.key import Key
from boto.s3.prefix import Prefix

host = "object.ecstestdrive.com"
port = 443
secure = port == 9021 or port == 443
access_key_id = 'user1'
secret_key = ''
debug_level = 2
calling_format = OrdinaryCallingFormat()
s3 = S3Connection(aws_access_key_id=access_key_id, aws_secret_access_key=secret_key, is_secure=secure, port=port,
                  host=host, debug=debug_level,
                  calling_format=calling_format)

bucket_name = "mybucket"
bucket = s3.create_bucket(bucket_name)

'''
Create a bunch of keys to test with.
'''
key_names = ["20151102/account-12345/bill.xml",
             "20151102/account-12345/bill.pdf",
             "20151102/account-12345/bill.html",
             "20151102/account-55555/bill.xml",
             "20151102/account-55555/bill.pdf",
             "20151102/account-55555/bill.html",
             "20151102/account-77777/bill.xml",
             "20151102/account-77777/bill.pdf",
             "20151102/account-77777/bill.html",
             "20151103/account-11111/bill.xml",
             "20151103/account-11111/bill.pdf",
             "20151103/account-11111/bill.html",
             "20151103/account-11122/bill.xml",
             "20151103/account-11122/bill.pdf",
             "20151103/account-11122/bill.html",
             "20151103/account-11133/bill.xml",
             "20151103/account-11133/bill.pdf",
             "20151103/account-11133/bill.html",
             "20141103/account-11111/bill.xml",
             "20141103/account-11111/bill.pdf",
             "20141103/account-11111/bill.html",
             "20141103/account-11122/bill.xml",
             "20141103/account-11122/bill.pdf",
             "20141103/account-11122/bill.html",
             "20141103/account-11133/bill.xml",
             "20141103/account-11133/bill.pdf",
             "20141103/account-11133/bill.html"]

for key_name in key_names:
    key = bucket.new_key(key_name)
    key.set_contents_from_string("data for key %s" % key_name, {"content-type": "text/plain"})


def bucket_list():
    """
    Start a loop to experiment with bucket listing.
    """
    running = True
    while running:
        prefix = raw_input("Prefix: ")
        delimiter = raw_input("Delimiter: ")
        marker = raw_input("Marker: ")
        results = bucket.list(prefix, delimiter, marker)
        print "%30s %10s %s" % ("LastModified", "Size", "Key")
        print "------------------------------ ---------- ------------------------------------------"
        for result in results:
            if type(result) is Key:
                print "%30s %10d %s" % (result.last_modified, result.size, result.name)
            if type(result) is Prefix:
                print "%30s %10s %s" % ("", "(PREFIX)", result.name)
        again = raw_input("Again? (y/n) ")
        running = again == "y"

bucket_list()
