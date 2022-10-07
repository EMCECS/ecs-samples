The sample demonstrates how to invoke ECS S3 Java SDK in C++. It's created to fit in some specific customer use scenario, however, we always recommend to directly use ECS S3 Java SDK if possible or seek alternative S3 SDKs available in C++ from other vendors.
std::filesystem is used to simplify the jar discovery thus compiler needs to support C++17 or later. The code is generally platform independent with compiler and operating system properly configured.

Setup:
------
1. Unzip ECS S3 Java SDK package and fill in the path to below: 
	ECSS3Factory::libPath in lib/ecss3factory.h
2. Fill in S3 connection info to below:
	ECSS3Factory::S3_URI,S3_ACCESS_KEY_ID,S3_SECRET_KEY in lib/ecss3factory.h

Build:
------
#cmake --build .
