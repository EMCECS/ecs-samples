import io
import requests
import GetConnection

testNamespace = "testns1"
testBucketName = "testtest"
testObjectName = "testLarge"

if __name__ == '__main__':
    s3 = GetConnection.getConnection()
    # put method:  boto3.client.generate_presigned_post is not supported in current version
    # fot the rest, please use boto3.client.generate_presigned_url
    # example for get_object
    url = s3.generate_presigned_url(
        ClientMethod='get_object',
        Params={
            'Bucket': testBucketName,
            'Key': testObjectName,
        }
    )
    response = requests.get(url)
    print("raw data get from the url: " + response.content.decode())
