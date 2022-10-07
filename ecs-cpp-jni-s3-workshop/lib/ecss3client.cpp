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

#include "ecss3client.h"

ECSS3Factory* ECSS3Client::s3factory = nullptr;
jobject ECSS3Client::s3client = nullptr;

ECSS3Client::ECSS3Client(){
	if (s3factory == nullptr) {
		s3factory = new ECSS3Factory();
	}
	if (s3client == nullptr) {
		s3client = s3factory->getS3Client();
		jc_S3JerseyClient = s3factory->findClass("com/emc/object/s3/jersey/S3JerseyClient");
	}
}

ECSS3Client::~ECSS3Client() {
	if (s3factory != nullptr) {
		delete s3factory;
		s3factory = nullptr;
	}
}

ECSS3Factory* ECSS3Client::getECSS3Factory() {
	return s3factory;
}

jobject ECSS3Client::listDataNodes() {
	jmethodID jm_S3JerseyClient_listDataNodes = s3factory->getMethodID(jc_S3JerseyClient, "listDataNodes", "()Lcom/emc/object/s3/bean/ListDataNode;");
	jobject listDataNode = s3factory->getJNIEnv()->CallObjectMethod(s3client, jm_S3JerseyClient_listDataNodes);
	return listDataNode;

}

jobject ECSS3Client::listBuckets() {
	jmethodID jm_S3JerseyClient_listBucket = s3factory->getMethodID(jc_S3JerseyClient, "listBuckets", "()Lcom/emc/object/s3/bean/ListBucketsResult;");
	jobject listBucketsResult = s3factory->getJNIEnv()->CallObjectMethod(s3client, jm_S3JerseyClient_listBucket);

	return  listBucketsResult;
}

void ECSS3Client::createBucket(string bucket) {
	jmethodID jm_S3JerseyClient_createBucket = s3factory->getMethodID(jc_S3JerseyClient, "createBucket", "(Ljava/lang/String;)V");
	s3factory->getJNIEnv()->CallObjectMethod(s3client, jm_S3JerseyClient_createBucket, s3factory->getJNIEnv()->NewStringUTF(bucket.c_str()));
}