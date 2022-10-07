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
#include "ecss3factory.h"

class ECSS3Client {
	static ECSS3Factory* s3factory;
	static jobject s3client;
	jclass jc_S3JerseyClient;

public:
	ECSS3Client();
	~ECSS3Client();
	ECSS3Factory* getECSS3Factory();
	jobject listDataNodes();
	jobject listBuckets();
	void createBucket(string bucke);
};