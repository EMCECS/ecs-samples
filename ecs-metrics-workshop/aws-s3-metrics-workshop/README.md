AWS Signature v4 auth requires passing -region specification otherwise it won't work. See example below:
aws s3 ls s3://ravirajamani --debug >> README.md

2019-05-26 17:38:06,445 - MainThread - botocore.auth - DEBUG - StringToSign:
GET


Sun, 26 May 2019 17:38:06 GMT
/ravirajamani/
2019-05-26 17:38:06,448 - MainThread - botocore.endpoint - DEBUG - Sending http request: <PreparedRequest [GET]>
2019-05-26 17:38:06,781 - MainThread - botocore.parsers - DEBUG - Response headers: {'Server': 'AmazonS3', 'Date': 'Sun, 26 May 2019 17:38:06 GMT', 'x-amz-request-id': '539C951595BF6B5A', 'Transfer-Encoding': 'chunked', 'x-amz-id-2': 'X0DS8ROEV4yESl1iy3fxBwvvP/X/1J2ZERmwfWPcrEn1WaeKM0HMKrzPw85w4P8tuIsGqH4DrKo=', 'x-amz-bucket-region': 'us-east-2', 'Connection': 'close', 'Content-Type': 'application/xml', 'x-amz-region': 'us-east-2'}
2019-05-26 17:38:06,781 - MainThread - botocore.parsers - DEBUG - Response body:
b'<?xml version="1.0" encoding="UTF-8"?>\n<Error><Code>InvalidRequest</Code><Message>The authorization mechanism you have provided is not supported. Please use AWS4-HMAC-SHA256.</Message><RequestId>539C951595BF6B5A</RequestId><HostId>X0DS8ROEV4yESl1iy3fxBwvvP/X/1J2ZERmwfWPcrEn1WaeKM0HMKrzPw85w4P8tuIsGqH4DrKo=</HostId></Error>'
2019-05-26 17:38:06,782 - MainThread - botocore.hooks - DEBUG - Event needs-retry.s3.ListObjects: calling handler <botocore.retryhandler.RetryHandler object at 0x7fc7f0c89c18>
2019-05-26 17:38:06,782 - MainThread - botocore.retryhandler - DEBUG - No retry needed.
2019-05-26 17:38:06,782 - MainThread - botocore.hooks - DEBUG - Event needs-retry.s3.ListObjects: calling handler <bound method S3RegionRedirector.redirect_from_error of <botocore.utils.S3RegionRedirector object at 0x7fc7f0cba198>>
2019-05-26 17:38:06,782 - MainThread - botocore.hooks - DEBUG - Event after-call.s3.ListObjects: calling handler <function decode_list_object at 0x7fc7f1758d90>
2019-05-26 17:38:06,782 - MainThread - botocore.hooks - DEBUG - Event after-call.s3.ListObjects: calling handler <function enhance_error_msg at 0x7fc7f256b158>
2019-05-26 17:38:06,783 - MainThread - awscli.clidriver - DEBUG - Exception caught in main()
Traceback (most recent call last):
  File "/usr/lib/python3/dist-packages/awscli/clidriver.py", line 186, in main
    return command_table[parsed_args.command](remaining, parsed_args)
  File "/usr/lib/python3/dist-packages/awscli/customizations/commands.py", line 190, in __call__
    parsed_globals)
  File "/usr/lib/python3/dist-packages/awscli/customizations/commands.py", line 187, in __call__
    return self._run_main(parsed_args, parsed_globals)
  File "/usr/lib/python3/dist-packages/awscli/customizations/s3/subcommands.py", line 472, in _run_main
    bucket, key, parsed_args.page_size, parsed_args.request_payer)
  File "/usr/lib/python3/dist-packages/awscli/customizations/s3/subcommands.py", line 499, in _list_all_objects
    for response_data in iterator:
  File "/usr/lib/python3/dist-packages/botocore/paginate.py", line 102, in __iter__
    response = self._make_request(current_kwargs)
  File "/usr/lib/python3/dist-packages/botocore/paginate.py", line 174, in _make_request
    return self._method(**current_kwargs)
  File "/usr/lib/python3/dist-packages/botocore/client.py", line 251, in _api_call
    return self._make_api_call(operation_name, kwargs)
  File "/usr/lib/python3/dist-packages/botocore/client.py", line 537, in _make_api_call
    raise ClientError(parsed_response, operation_name)
botocore.exceptions.ClientError: An error occurred (InvalidRequest) when calling the ListObjects operation: You are attempting to operate on a bucket in a region that requires Signature Version 4.  You can fix this issue by explicitly providing the correct region location using the --region argument, the AWS_DEFAULT_REGION environment variable, or the region variable in the AWS CLI configuration file.  You can get the bucket's location by running "aws s3api get-bucket-location --bucket BUCKET".
2019-05-26 17:38:06,784 - MainThread - awscli.clidriver - DEBUG - Exiting with rc 255

An error occurred (InvalidRequest) when calling the ListObjects operation: You are attempting to operate on a bucket in a region that requires Signature Version 4.  You can fix this issue by explicitly providing the correct region location using the --region argument, the AWS_DEFAULT_REGION environment variable, or the region variable in the AWS CLI configuration file.  You can get the bucket's location by running "aws s3api get-bucket-location --bucket BUCKET".



aws s3 ls s3://ravirajamani --region=us-east-2 --debug >> README.md

2019-05-26 17:38:40,719 - MainThread - botocore.auth - DEBUG - CanonicalRequest:
GET
/ravirajamani
delimiter=%2F&encoding-type=url&prefix=
host:s3.us-east-2.amazonaws.com
x-amz-content-sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
x-amz-date:20190526T173840Z

host;x-amz-content-sha256;x-amz-date
e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
2019-05-26 17:38:40,720 - MainThread - botocore.auth - DEBUG - StringToSign:
AWS4-HMAC-SHA256
20190526T173840Z
20190526/us-east-2/s3/aws4_request
dc1192c29dff4801d50e76cdbe2a7aa0ed7281664a51ad98931c69fb938dbc55
2019-05-26 17:38:40,720 - MainThread - botocore.auth - DEBUG - Signature:
7fcd5f82d95c42ba484700bc8e7732417c92eaad46aa7d34e2901def30917f5e
2019-05-26 17:38:40,722 - MainThread - botocore.endpoint - DEBUG - Sending http request: <PreparedRequest [GET]>
2019-05-26 17:38:41,063 - MainThread - botocore.parsers - DEBUG - Response headers: {'Server': 'AmazonS3', 'x-amz-bucket-region': 'us-east-2', 'Date': 'Sun, 26 May 2019 17:38:42 GMT', 'x-amz-request-id': '87EE58B465396C4F', 'x-amz-id-2': 'C5vDT3dESqoxe3eeSRrZcr1CEVXyTETdVxC+eXTYJcHoSwVOuer+dcN7/lqXbZltJ6zqZtHN1II=', 'Content-Type': 'application/xml', 'Transfer-Encoding': 'chunked'}
2019-05-26 17:38:41,063 - MainThread - botocore.parsers - DEBUG - Response body:
b'<?xml version="1.0" encoding="UTF-8"?>\n<ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/"><Name>ravirajamani</Name><Prefix></Prefix><Marker></Marker><MaxKeys>1000</MaxKeys><Delimiter>/</Delimiter><EncodingType>url</EncodingType><IsTruncated>false</IsTruncated><Contents><Key>DynamicLogin.zip</Key><LastModified>2018-02-18T18:51:57.000Z</LastModified><ETag>&quot;1f1b1f5e9e96ad0a526d31d66b21245c&quot;</ETag><Size>697624</Size><Owner><ID>dd6d5e8e5d3414d384f1e1dc7d76d3199a66b06befedabd10f66cbe50f6cd5a0</ID></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>Screen+Shot+2017-11-27+at+1.15.42+PM.png</Key><LastModified>2017-11-27T22:57:08.000Z</LastModified><ETag>&quot;f3be3d10d5fbe143df6932554494bbbb&quot;</ETag><Size>189087</Size><Owner><ID>dd6d5e8e5d3414d384f1e1dc7d76d3199a66b06befedabd10f66cbe50f6cd5a0</ID></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>Screen+Shot+2017-11-27+at+1.29.01+PM.png</Key><LastModified>2017-11-27T22:56:33.000Z</LastModified><ETag>&quot;205ed7799f0a9860c8d6296498d8d13d&quot;</ETag><Size>197041</Size><Owner><ID>dd6d5e8e5d3414d384f1e1dc7d76d3199a66b06befedabd10f66cbe50f6cd5a0</ID></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>Screen+Shot+2018-02-21+at+8.34.00+AM.png</Key><LastModified>2018-02-21T16:49:41.000Z</LastModified><ETag>&quot;554b9cbe29c4eddfec6e02874de88336&quot;</ETag><Size>167108</Size><Owner><ID>dd6d5e8e5d3414d384f1e1dc7d76d3199a66b06befedabd10f66cbe50f6cd5a0</ID></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>logout_form.html</Key><LastModified>2018-02-18T18:54:53.000Z</LastModified><ETag>&quot;df12d231c95bf32d7a0c916e5d67b47b&quot;</ETag><Size>85</Size><Owner><ID>dd6d5e8e5d3414d384f1e1dc7d76d3199a66b06befedabd10f66cbe50f6cd5a0</ID></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>main_page.html</Key><LastModified>2018-02-18T19:01:14.000Z</LastModified><ETag>&quot;196506de969ce07d659f1cab1093d301&quot;</ETag><Size>404374</Size><Owner><ID>dd6d5e8e5d3414d384f1e1dc7d76d3199a66b06befedabd10f66cbe50f6cd5a0</ID></Owner><StorageClass>STANDARD</StorageClass></Contents><CommonPrefixes><Prefix>DynamicLogin/</Prefix></CommonPrefixes></ListBucketResult>'
2019-05-26 17:38:41,065 - MainThread - botocore.hooks - DEBUG - Event needs-retry.s3.ListObjects: calling handler <botocore.retryhandler.RetryHandler object at 0x7f24f83a8c50>
2019-05-26 17:38:41,065 - MainThread - botocore.retryhandler - DEBUG - No retry needed.
2019-05-26 17:38:41,065 - MainThread - botocore.hooks - DEBUG - Event needs-retry.s3.ListObjects: calling handler <bound method S3RegionRedirector.redirect_from_error of <botocore.utils.S3RegionRedirector object at 0x7f24f835b2e8>>
2019-05-26 17:38:41,065 - MainThread - botocore.hooks - DEBUG - Event after-call.s3.ListObjects: calling handler <function decode_list_object at 0x7f24f8e77d90>
2019-05-26 17:38:41,065 - MainThread - botocore.hooks - DEBUG - Event after-call.s3.ListObjects: calling handler <function enhance_error_msg at 0x7f24f863c158>

