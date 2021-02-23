import GetConnection

objectNames = ["testo1", "testo2", "testo3"]
testBucketName = "testtest"

if __name__ == '__main__':
    s3 = GetConnection.getConnection()
    # put objects
    for objectName in objectNames:
        response = s3.put_object(
            Body=objectName + ' raw data',
            Bucket=testBucketName,
            Key=objectName,
        )
    # list with limited prefix
    response = s3.list_objects_v2(Bucket=testBucketName, Prefix="testo")
    for i, ob in enumerate(response["Contents"]):
        print("-------------------------------------")
        print("object " + str(i))
        print("key: " + ob["Key"])
        print("ETag: " + ob["ETag"])
        print("Size: " + str(ob["Size"]))
        print("StorageClass: " + ob["StorageClass"])
    # save the Token for the next listing
    NextToken = response['NextContinuationToken']
    # list after a specific key by either providing StartAfter or ContinuationToken(more efficient)
    response = s3.list_objects_v2(Bucket=testBucketName, MaxKeys=1, Prefix="testo", StartAfter="testo1", ContinuationToken=NextToken)
    print(response["Contents"][0]["Key"])
    response = s3.list_objects_v2(Bucket=testBucketName, MaxKeys=1, Prefix="testo", ContinuationToken=NextToken)
    print(response["Contents"][0]["Key"])
