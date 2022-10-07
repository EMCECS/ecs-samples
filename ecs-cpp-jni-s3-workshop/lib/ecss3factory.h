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

#pragma once

#include <iostream>
#include <jni.h>
#include <cstring>
#include <filesystem>
#include <map>

using namespace std;

#ifdef _WIN32
	const std::string os_pathsep(";");
#else
	const std::string os_pathsep(":");
#endif

class ECSS3Factory {
	JavaVM* jvm = nullptr;				// Pointer to the JVM (Java Virtual Machine)
	JNIEnv* env = nullptr;				// Pointer to native interface
	//string libPath = ".";
	string libPath = "/tmp/test/object-client-3.5.0";
	static map<string, jclass> _classInfo;
	static jobject s3client;

	string S3_URI = "http://xxx.xxx.xxx.xxx:9020";
	string S3_ACCESS_KEY_ID = "userid";
	string S3_SECRET_KEY = "secretkey";

	void appendClassPath(string& option, string path);
	int setupJNI();

public:
	ECSS3Factory();
	~ECSS3Factory();
	JNIEnv* getJNIEnv();
	jobject getS3Client();
	jclass findClass(string className);
	jmethodID getMethodID(jclass clazz, string methodName, string sig);
};