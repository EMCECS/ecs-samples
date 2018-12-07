/*
 * Copyright 2018 Dell Inc. or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.dellemc.ecs.s3.sample

import scala.collection.JavaConversions._

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.AccessControlList
import com.amazonaws.services.s3.model.CanonicalGrantee
import com.amazonaws.services.s3.model.Grant
import com.amazonaws.services.s3.model.Grantee
import com.amazonaws.services.s3.model.GroupGrantee
import com.amazonaws.services.s3.model.Owner
import com.amazonaws.services.s3.model.Permission

object _12_ObjectAcls extends BucketAndObjectValidator {

    /**
     * Run the class.
     * 
     * @param args
     */
    def main(args: Array[String]): Unit = {
        val owner: Owner = new Owner(AWSS3Factory.S3_ACCESS_KEY_ID, AWSS3Factory.S3_ACCESS_KEY_ID)
        val ownerGrantee: Grantee = new CanonicalGrantee(AWSS3Factory.S3_ACCESS_KEY_ID)
        val other: Grantee = new CanonicalGrantee(AWSS3Factory.S3_ACCESS_KEY_ID_2)
        val invalid: Grantee = new CanonicalGrantee("invalid_access_key")

        // Create a valid acl
        val acl: AccessControlList = new AccessControlList()
        acl.setOwner(owner)
        acl.grantPermission(ownerGrantee, Permission.FullControl)
        acl.grantPermission(GroupGrantee.AllUsers, Permission.Read)
        acl.grantPermission(other, Permission.Write)

        // Create an invalid acl with a nonexistent user
        val invalidAcl: AccessControlList = new AccessControlList()
        invalidAcl.setOwner(owner)
        invalidAcl.grantPermission(ownerGrantee, Permission.FullControl)
        invalidAcl.grantPermission(GroupGrantee.AllUsers, Permission.Read)
        invalidAcl.grantPermission(invalid, Permission.Write)

        setObjectAcl( AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, AWSS3Factory.S3_OBJECT, acl, invalidAcl )
    }

    /**
     * Check the object acl.
     * Try to set an invalid acl, then check again to verify that the acl is unchanged. 
     * Set a valid acl, then check to verify that the acl is changed.
     * Again try to set an invalid acl, then check to verify that the acl is unchanged from the last valid setting. 
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     * @param key the object to use
     * @param acl a valid AccessControlList
     * @param invalidAcl an invalid AccessControlList
     */
    def setObjectAcl(s3Client: AmazonS3, bucketName: String, key: String, acl: AccessControlList, invalidAcl: AccessControlList) = {
        try {
            checkObjectAcl( s3Client, bucketName, key )

            setAndCheckInvalidObjectAcl( s3Client, bucketName, key, invalidAcl )

            s3Client.setObjectAcl(bucketName, key, acl)

            checkObjectAcl( s3Client, bucketName, key )

            setAndCheckInvalidObjectAcl( s3Client, bucketName, key, invalidAcl )
        } catch { case e: Exception => outputException(e) }
        println()
    }

    /**
     * Try to set an invalid acl, then check the acl.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     * @param key the object to use
     * @param invalidAcl an invalid AccessControlList
     */
    def setAndCheckInvalidObjectAcl(s3Client: AmazonS3, bucketName: String, key: String, invalidAcl: AccessControlList) = {
        try {
            s3Client.setObjectAcl(bucketName, key, invalidAcl)
        } catch { case e: Exception => outputException(e) }
        println()

        checkObjectAcl( s3Client, bucketName, key )
    }

    /**
     * Output the current acl.
     * 
     * @param s3Client the client to use
     * @param bucketName the bucket to use
     * @param key the object to check
     */
    def checkObjectAcl(s3Client: AmazonS3, bucketName: String, key: String) = {
        try {
            val result: AccessControlList = s3Client.getObjectAcl(bucketName, key)
            println( s"object acl for $bucketName/$key:" )
            println( s"Owner: ${result.getOwner()}" )
            println( "Grants:" )
            result.getGrantsAsList().foreach((grant: Grant) => {
                var granteeString = "unidentified"
                grant.getGrantee() match {
                    case groupGrantee: GroupGrantee => {
                        granteeString = groupGrantee.getIdentifier()
                    }
                    case canonicalGrantee: CanonicalGrantee => {
                        granteeString = s"${canonicalGrantee.getDisplayName()}/${canonicalGrantee.getIdentifier()}"
                    }
                }
                println( s"grantee=$granteeString, permission=${grant.getPermission}" )
            })
        } catch { case e: Exception => outputException(e) }
        println()
    }

}