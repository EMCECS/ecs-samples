import GetConnection

testNamespace = "testns1"
testBucketName = "testtest"
testObjectName = "testLarge"

if __name__ == '__main__':
    s3 = GetConnection.getConnection()
    # get bucket versioning status
    response = s3.get_bucket_versioning(Bucket=testBucketName)
    print("The versioning state: " + response.get('Status', "null"))
    # currently, ecs does not support MFA delete
    # print("MFA delete is enable " + response.get('MFADelete', "null"))
    # put bucket versioning status
    response = s3.put_bucket_versioning(
        Bucket=testBucketName,
        VersioningConfiguration={
            'Status': 'Enabled',
        },
    )
    # list versions
    response = s3.list_object_versions(
        Bucket=testBucketName,
        # Specifies the key to start with when listing objects in a bucket
        # KeyMarker=string,
        MaxKeys=5,
        # prefix of the target objects, please refer to 04_listObjects
        # Prefix=string,
        # Specifies the object version you want to start listing from
        # VersionIdMarker=string,
    )
    for i, ob in enumerate(response["Versions"]):
        print("-------------------------------------")
        print("object " + str(i))
        print("key: " + ob["Key"])
        print("VersionId " + ob["VersionId"])
        print("ETag: " + ob["ETag"])
        print("Size: " + str(ob["Size"]))
        print("StorageClass: " + ob["StorageClass"])


