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

import com.emc.object.s3.S3Client;
import com.emc.object.s3.bean.AccessControlList;
import com.emc.object.s3.bean.CanonicalUser;
import com.emc.object.s3.bean.Grant;
import com.emc.object.s3.bean.Group;
import com.emc.object.s3.bean.Permission;


public class _50_BucketRetention {

    public static void main(String[] args) throws Exception {
        // create the ECS S3 Client
        S3Client s3 = ECSS3Factory.getS3Client();

        AccessControlList acl = new AccessControlList();
        CanonicalUser owner = new CanonicalUser();
        owner.setDisplayName(ECSS3Factory.S3_ACCESS_KEY_ID);
        owner.setId(ECSS3Factory.S3_ACCESS_KEY_ID);
        acl.setOwner(owner);
        Grant grant = new Grant();
        grant.setGrantee(owner);
        grant.setPermission(Permission.FULL_CONTROL);
        CanonicalUser other = new CanonicalUser();
        acl.getGrants().add(grant);
        other.setDisplayName(ECSS3Factory.S3_ACCESS_KEY_ID_2);
        other.setId(ECSS3Factory.S3_ACCESS_KEY_ID_2);
        grant = new Grant();
        grant.setGrantee(other);
        grant.setPermission(Permission.READ);
        acl.getGrants().add(grant);
        grant = new Grant();
        grant.setGrantee(other);
        grant.setPermission(Permission.WRITE);
        acl.getGrants().add(grant);
        grant = new Grant();
        grant.setGrantee(Group.ALL_USERS);
        grant.setPermission(Permission.READ);
        acl.getGrants().add(grant);
        s3.setBucketAcl(ECSS3Factory.S3_BUCKET, acl);

    }
}
