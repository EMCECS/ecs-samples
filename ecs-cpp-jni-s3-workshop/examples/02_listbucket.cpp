/*
 * Copyright (c) 2022 DELL/EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of DELL/EMC Corporation
 * or is licensed to DELL/EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of DELL/EMC.
 */

#include "../lib/ecss3client.h"

int main() {
	ECSS3Client s3client = ECSS3Client();
	ECSS3Factory* s3factory = s3client.getECSS3Factory();
	string bucket = "ecs-cpp-jini-s3-bucket";
	s3client.createBucket(bucket);

	jobject listBucketsResult = s3client.listBuckets();

	if (listBucketsResult == nullptr) {
		cout << "failed to list bucket";
		return -1;
	}

	//Print listed buckets
	jclass jc_ListBucketsResult = s3factory->findClass("com/emc/object/s3/bean/ListBucketsResult");

	jmethodID jmgetBuckets = s3factory->getMethodID(jc_ListBucketsResult, "getBuckets", "()Ljava/util/List;");
	jobject bucketList = s3factory->getJNIEnv()->CallObjectMethod(listBucketsResult, jmgetBuckets);

	jclass jcList = s3factory->findClass("java/util/List");
	jmethodID jm_List_getsize = s3factory->getMethodID(jcList, "size", "()I");
	jint size = s3factory->getJNIEnv()->CallIntMethod(bucketList, jm_List_getsize);
	cout << "Total number of buckets:" << size << endl;

	jmethodID jmget = s3factory->getJNIEnv()->GetMethodID(jcList, "get", "(I)Ljava/lang/Object;");

	jclass jc_Bucket = s3factory->findClass("com/emc/object/s3/bean/Bucket");
	jmethodID jm_Bucket_getName = s3factory->getMethodID(jc_Bucket, "getName", "()Ljava/lang/String;");
	for (int i = 0; i < size; i++) {
		jobject myobj = s3factory->getJNIEnv()->CallObjectMethod(bucketList, jmget, i);

		jstring bucketName = (jstring)s3factory->getJNIEnv()->CallObjectMethod(myobj, jm_Bucket_getName);
		const char* str = s3factory->getJNIEnv()->GetStringUTFChars(bucketName, 0);
		if (bucket.compare(str) == 0) {
			cout << str << "\t----(bucket created and found!)" << endl;
		}
		else {
			cout << str << endl;
		}
	}
	return 0;
}