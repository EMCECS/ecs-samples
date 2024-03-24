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
	jobject listDataNodesResult = s3client.listDataNodes();

	if (listDataNodesResult == nullptr) {
		cout << "failed to list data nodes";
		return -1;
	}

	jclass jc_ListDataNode = s3factory->findClass("com/emc/object/s3/bean/ListDataNode");
	jmethodID jm_ListDataNode_getVersionInfo = s3factory->getMethodID(jc_ListDataNode, "getVersionInfo", "()Ljava/lang/String;");
	jstring versionInfo = (jstring)s3factory->getJNIEnv()->CallObjectMethod(listDataNodesResult, jm_ListDataNode_getVersionInfo);
	cout << "ECS Version:" << s3factory->getJNIEnv()->GetStringUTFChars(versionInfo, 0) << endl;

	jmethodID jm_ListDataNode_getDataNodes = s3factory->getMethodID(jc_ListDataNode, "getDataNodes", "()Ljava/util/List;");
	jobject dataNodeList = s3factory->getJNIEnv()->CallObjectMethod(listDataNodesResult, jm_ListDataNode_getDataNodes);

	jclass jc_List = s3factory->findClass("java/util/List");
	jmethodID jm_List_getsize = s3factory->getMethodID(jc_List, "size", "()I");
	jint size = s3factory->getJNIEnv()->CallIntMethod(dataNodeList, jm_List_getsize);
	cout << "Total number of Nodes:" << size << endl;

	jmethodID jm_List_get = s3factory->getJNIEnv()->GetMethodID(jc_List, "get", "(I)Ljava/lang/Object;");
	for (int i = 0; i < size; i++) {
		jstring nodeName = (jstring)s3factory->getJNIEnv()->CallObjectMethod(dataNodeList, jm_List_get, i);
		const char* str = s3factory->getJNIEnv()->GetStringUTFChars(nodeName, 0);
		cout << str << endl;
	}

	return 0;
}