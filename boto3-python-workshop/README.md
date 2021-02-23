python client(boto3) example for ecs
====================================

offical boto3 doc:https://boto3.amazonaws.com/v1/documentation/api/latest/reference/services/s3.html#client

sample connection setting: GetConnection.py

05_UpdateAppend.py & 09_BucketRetention.py insert custom ecs header through Boto3's event system to enable ecs-only features

boto3 event doc:https://boto3.amazonaws.com/v1/documentation/api/latest/guide/events.html

ecs java client example v.s. boto3 example
==========================================
☒ ECSS3Factory.java   ->   GetConnection.py

☒_00_CreateBucket.java   ->   00_BucketBasics.py

☒_01_CreateObject.java   ->   01_ObjectBasics.py

☒_02_ReadObject.java   ->   01_ObjectBasics.py

☒ _03_UpdateObject.java   ->   01_ObjectBasics.py

☒ _04_DeleteObject.java   ->   01_ObjectBasics.py

☒ _05_CreateObjectWithMetadata.java   ->   02_ObjectsWithMetadata.py

☒ _06_ReadObjectWithMetadata.java   ->   02_ObjectsWithMetadata.py

☒ _07_PresignedURL.java   ->   06_PresignedURLs.py

☒ _07_UpdateObjectMetadata.java   ->   02_ObjectsWithMetadata.py

☒ _08_CreateLargeObject.java   ->   03_LargeObject.py

☒ _09_UpdateAppendExtensions.java   ->   05_UpdateAppend.py

☐ _0_GetVersion.java   ->   get ECS nodes’ version, not supported in AWS boto3

☒ _10_AtomicAppend.java   ->   05_UpdateAppend.py

☒ _11_ListObjects.java      04_ListObjects.py

☐ _12_SearchObjects.java      use metadata search in listing, not supported in AWS boto3

☒ _13_EnableDisableVersioning.java      07_Versioning.py

☒ _14_GetVersioningStatus.java      07_Versioning.py

☒ _15_ListVersions.java      07_Versioning.py

☒ _16_RestoreVersion.java      07_Versioning.py

☒ _17_BucketLifecycle.java      08_BucketLifeCycle.py

☒ _18_BucketLifecycleVersioning.java      08_BucketLifeCycle.py

☒ _50_BucketRetention.java      09_BucketRetention.py

☒ _99_DeleteBucket.java      00_BucketBasics.py
