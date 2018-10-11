/*
 * Copyright 2013-2018 Dell Inc. or its subsidiaries. All Rights Reserved.
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
package com.emc.ecs.s3.sample;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CanonicalGrantee;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.Grantee;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.Permission;

public class _41_BucketAcl {

    public static void main(String[] args) throws Exception {
        Owner owner = new Owner(AWSS3Factory.S3_ACCESS_KEY_ID, AWSS3Factory.S3_ACCESS_KEY_ID);
        Grantee ownerGrantee = new CanonicalGrantee(AWSS3Factory.S3_ACCESS_KEY_ID);
        Grantee other = new CanonicalGrantee(AWSS3Factory.S3_ACCESS_KEY_ID_2);
        Grantee invalid = new CanonicalGrantee("invalid_access_key");

        AccessControlList acl = new AccessControlList();
        acl.setOwner(owner);
        acl.grantPermission(ownerGrantee, Permission.FullControl);
        acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
        acl.grantPermission(other, Permission.Write);

        AccessControlList invalidAcl = new AccessControlList();
        invalidAcl.setOwner(owner);
        invalidAcl.grantPermission(ownerGrantee, Permission.FullControl);
        invalidAcl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
        invalidAcl.grantPermission(invalid, Permission.Write);

        setBucketAcl( AWSS3Factory.getS3ClientWithV2Signatures(), AWSS3Factory.S3_BUCKET, acl, invalidAcl );
        setBucketAcl( AWSS3Factory.getS3ClientWithV4Signatures(), AWSS3Factory.S3_BUCKET_2, acl, invalidAcl );
    }


    /**
     * @param s3Client
     * @param bucketName
     * @param acl
     * @param invalidAcl
     */
    private static void setBucketAcl(AmazonS3 s3Client, String bucketName, AccessControlList acl, AccessControlList invalidAcl ) {
        try {
            checkBucketAcl( s3Client, bucketName );

            setAndCheckInvalidBucketAcl( s3Client, bucketName, invalidAcl );

            s3Client.setBucketAcl(bucketName, acl);

            checkBucketAcl( s3Client, bucketName );

            setAndCheckInvalidBucketAcl( s3Client, bucketName, invalidAcl );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    /**
     * @param s3Client
     * @param bucketName
     * @param invalidAcl
     */
    private static void setAndCheckInvalidBucketAcl(AmazonS3 s3Client, String bucketName, AccessControlList invalidAcl) {
        try {
            s3Client.setBucketAcl(bucketName, invalidAcl);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println();
        }

        checkBucketAcl( s3Client, bucketName );
    }


    /**
     * @param s3Client
     * @param bucketName
     */
    private static void checkBucketAcl(AmazonS3 s3Client, String bucketName) {
        try {
            AccessControlList result = s3Client.getBucketAcl(bucketName);
            System.out.println("bucket acl for " + bucketName + ":");
            System.out.println("Owner: " + result.getOwner().getDisplayName() + "/" + result.getOwner().getId());
            for (Grant grant : result.getGrantsAsList()) {
                String granteeString = "unidentified";
                if (grant.getGrantee() instanceof GroupGrantee) {
                    GroupGrantee groupGrantee = ((GroupGrantee) grant.getGrantee());
                    granteeString = groupGrantee.getIdentifier();
                } else if (grant.getGrantee() instanceof CanonicalGrantee) {
                    CanonicalGrantee canonicalGrantee = ((CanonicalGrantee) grant.getGrantee());
                    granteeString = canonicalGrantee.getDisplayName() + "/" + canonicalGrantee.getIdentifier();
                }
                System.out.println(grant.getPermission().toString()+ ": " + granteeString);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        System.out.println();
    }

}
