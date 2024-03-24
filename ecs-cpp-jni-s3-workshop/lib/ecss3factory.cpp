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

#include "ecss3factory.h"

using namespace std;

map<string, jclass> ECSS3Factory::_classInfo = {};
jobject ECSS3Factory::s3client = nullptr;

void ECSS3Factory::appendClassPath(string& option, string path) {
	for (const auto& file : std::filesystem::recursive_directory_iterator(path.c_str())) {
		if (!file.is_directory() && file.path().extension().compare(".jar") == 0) {
			option += file.path().generic_string() + os_pathsep;
		}
	}
}

int ECSS3Factory::setupJNI() {
	//==================== prepare loading of Java VM ============================
	JavaVMInitArgs vm_args;							// Initialization arguments
	JavaVMOption* options = new JavaVMOption[2];	// JVM invocation options
	string classPathOption = "-Djava.class.path=";
	string option2 = "-Dcom.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize=true";
	appendClassPath(classPathOption, libPath);
	options[0].optionString = (char*)classPathOption.c_str();
	options[1].optionString = (char*)option2.c_str();
	vm_args.version = JNI_VERSION_1_8;				// minimum Java version
	vm_args.nOptions = 2;							// number of options
	vm_args.options = options;
	vm_args.ignoreUnrecognized = false;				// invalid options make the JVM init fail

	//================= load and initialize Java VM and JNI interface ===============
	jint rc = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);

	if (rc != JNI_OK) {
		if (rc == JNI_EVERSION)
			cerr << "FATAL ERROR: JVM is oudated and doesn't meet requirements" << endl;
		else if (rc == JNI_ENOMEM)
			cerr << "FATAL ERROR: not enough memory for JVM" << endl;
		else if (rc == JNI_EINVAL)
			cerr << "FATAL ERROR: invalid ragument for launching JVM" << endl;
		else if (rc == JNI_EEXIST)
			cerr << "FATAL ERROR: the process can only launch one JVM an not more" << endl;
		else
			cerr << "FATAL ERROR:  could not create the JVM instance (error code " << rc << ")" << endl;
	}
	else {
		jint ver = env->GetVersion();
		cout << "JVM load succeeded. \nJNI Version " << ((ver >> 16) & 0x0f) << "." << (ver & 0x0f) << endl;
	}
	delete[] options;
	return rc;
}


ECSS3Factory::ECSS3Factory() {
	if (setupJNI() != JNI_OK) {
		cerr << "Failed to setup JNI!";
		exit(EXIT_FAILURE);
	};
}

ECSS3Factory::~ECSS3Factory() {
	if (jvm != nullptr) {
		jvm->DestroyJavaVM();
		jvm = nullptr;
	}
}

JNIEnv* ECSS3Factory::getJNIEnv() {
	return env;
}

jobject ECSS3Factory::getS3Client() {
	if (s3client == nullptr) {
		jclass jc_URI = findClass("java/net/URI");
		jmethodID jm_URI_Constructor = getMethodID(jc_URI, "<init>", "(Ljava/lang/String;)V");  // FIND AN OBJECT CONSTRUCTOR
		jobject uriObject = env->NewObject(jc_URI, jm_URI_Constructor, env->NewStringUTF(S3_URI.c_str()));

		jclass jc_S3Config = findClass("com/emc/object/s3/S3Config");
		jmethodID jm_S3Config_Constructor = env->GetMethodID(jc_S3Config, "<init>", "(Ljava/net/URI;)V");  // FIND AN OBJECT CONSTRUCTOR
		jobject s3Config = env->NewObject(jc_S3Config, jm_S3Config_Constructor, uriObject);
		if (s3Config == nullptr) {
			cout << "ERROR: failed create S3Config!";
			exit(EXIT_FAILURE);
		}

		jmethodID jm_S3Config_setIdentity = env->GetMethodID(jc_S3Config, "setIdentity", "(Ljava/lang/String;)V");
		env->CallVoidMethod(s3Config, jm_S3Config_setIdentity, env->NewStringUTF(S3_ACCESS_KEY_ID.c_str()));

		jmethodID jm_S3Config_setSecretKey = env->GetMethodID(jc_S3Config, "setSecretKey", "(Ljava/lang/String;)V");
		env->CallVoidMethod(s3Config, jm_S3Config_setSecretKey, env->NewStringUTF(S3_SECRET_KEY.c_str()));

		jclass jc_S3JerseyClient = findClass("com/emc/object/s3/jersey/S3JerseyClient");
		jmethodID jm_jc_S3JerseyClient_Constructor = env->GetMethodID(jc_S3JerseyClient, "<init>", "(Lcom/emc/object/s3/S3Config;)V");  // FIND AN OBJECT CONSTRUCTOR 
		s3client = env->NewObject(jc_S3JerseyClient, jm_jc_S3JerseyClient_Constructor, s3Config);
		if (s3client == nullptr) {
			if (env->ExceptionOccurred())
				env->ExceptionDescribe();
			else
				cout << "s3client is null but no exception was thrown." << endl;
			exit(EXIT_FAILURE);
		}
		cout << "s3client is succesfully created!" << endl;
	}
	return s3client;
}

jclass ECSS3Factory::findClass(string className) {
	if (_classInfo.find(className) != _classInfo.end()) {
		return _classInfo[className];
	}
	else {
		jclass myclass = env->FindClass(className.c_str());
		if (myclass == nullptr) {
			if (env->ExceptionOccurred())
				env->ExceptionDescribe();
			cerr << "ERROR: class " << className << " not found !" << endl;
			exit(EXIT_FAILURE);
		}
		else {
			_classInfo[className] = myclass;
		}
		return myclass;
	}
}

jmethodID ECSS3Factory::getMethodID(jclass clazz, string methodName, string sig) {
	jmethodID method = env->GetMethodID(clazz, methodName.c_str(), sig.c_str());
	if (method == nullptr) {
		if (env->ExceptionOccurred())
			env->ExceptionDescribe();
		cerr << "ERROR: method " << methodName << " " << sig << " not found !" << endl;
		exit(EXIT_FAILURE);
	}
	return method;
}