import io
import GetConnection

objectNames = ["testo1", "testo2", "testo3"]
testBucketName = "testtest"
fileInRam = io.BytesIO(b'data stored in RAM')

if __name__ == '__main__':
    s3 = GetConnection.getConnection()
    # put objects
    for objectName in objectNames:
        # response: class dict
            # ResponseMetadata: class dict
                # RequestId
                # HTTPStatusCode
                # ...
            # ETag
        response = s3.put_object(
            Body=objectName + ' raw data',
            Bucket=testBucketName,
            Key=objectName,
        )
    # upload a file from disk
    # Callback (function) -- A method which takes a number of bytes transferred to be periodically called
    # during the upload you can use it to monitor progress or other things
    # see example in 03_LargeObject, download function
    response = s3.upload_file("GetConnection.py", testBucketName, 'diskFile', Callback=None)
    # upload a file-like object stored in RAM, the object must be in binary mode
    response = s3.upload_fileobj(fileInRam, testBucketName, 'RAMFile', Callback=None)
    # get object, you can use Range= to get a part of the object
    response = s3.get_object(
        Bucket=testBucketName,
        Key="RAMFile",
        Range='bytes=0-18',
    )
    raw = response["Body"].read(amt=18)
    print("raw data get from the RAMFile" + raw.decode())
    # list objects
    # response: class dict
        # ResponseMetadata: class dict
            # RequestId
            # HTTPStatusCode
            # ...
        # Contents: class list
            #ETag:
            #Key:
            # ...
        # ...
    response = s3.list_objects_v2(
        Bucket=testBucketName,
        MaxKeys=10,
    )
    for i, object in enumerate(response["Contents"]):
        print("-------------------------------------")
        print("object " + str(i))
        print("key: " + object["Key"])
        print("ETag: " + object["ETag"])
        print("Size: " + str(object["Size"]))
        print("StorageClass: " + object["StorageClass"])
    # delete object
    response = s3.delete_object(
        Bucket=testBucketName,
        Key='RAMFile',
    )


