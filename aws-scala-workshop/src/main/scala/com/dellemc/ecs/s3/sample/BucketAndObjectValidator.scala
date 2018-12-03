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

import com.amazonaws.services.s3.AmazonS3

class BucketAndObjectValidator {

    protected def checkBucketExistence(s3Client: AmazonS3, bucketName: String) {
        try {
            def state = if (s3Client.doesBucketExistV2(bucketName)) "exists" else "does not exist"
            System.out.println(String.format("Bucket [%s] %s.", bucketName, state))
            System.out.println()
        } catch { case e: Exception => outputException(e) }
    }

    protected def outputException(e: Exception) = {
        System.out.println(e.getMessage())
        e.printStackTrace(System.out)
    }

}