package utils

/*
 * Copyright 2016 EMC Corporation. All Rights Reserved.
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
import (
	"fmt"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/credentials"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"github.com/jacobstr/confer"
)

// GetS3Client is to get S3 client to ECS server
func GetS3Client(config *confer.Config) (*s3.S3, error) {

	// Get Config
	s3Config := &aws.Config{
		Credentials: credentials.NewStaticCredentials(config.GetString("s3.access_key"), config.GetString("s3.secret_key"), ""),
		Endpoint:    aws.String(config.GetString("s3.endpoint")),
		Region:      aws.String(config.GetString("s3.region")),
	}

	// Set log level
	var logLevel aws.LogLevelType
	logLevelStr := config.GetString("LogLevel")
	switch logLevelStr {
	case "LogDebugWithSigning":
		logLevel = aws.LogDebugWithSigning
	case "LogDebugWithHTTPBody":
		logLevel = aws.LogDebugWithHTTPBody
	case "LogDebugWithRequestRetries":
		logLevel = aws.LogDebugWithRequestRetries
	case "LogDebugWithRequestErrors":
		logLevel = aws.LogDebugWithRequestErrors
	}
	s3Config.WithLogLevel(logLevel)

	// Create Session
	newSession, err := session.NewSession(s3Config)
	if err != nil {
		return nil, fmt.Errorf("Failed to create S3 session")
	}

	// Create S3 Client
	return s3.New(newSession), nil
}
