BUILD
=====

make # go is installed

make docker-build # docker is installed

make clean # cleanup


CONFIG
======

Set access_key and secret_key in config.yaml

RUN
===

`bin/<command>`

Eclipse
=======

* Install goclipse plugin by following https://github.com/GoClipse/goclipse/blob/latest/documentation/Installation.md
* New a go project in Eclipse
* Copy go-samples/ecs-go-s3-workshop to the location of the go project
* Edit `Go Build Targets` in project's properties: Build Command for Build Target "build": "make -C .." and set PATH env variable to include go binary and system commands
* Edit `Go Build Targets` in project's properties: Build Command for Build Target "build-tests": "make -C .. clean" and set PATH env variable to include system commands (NOTE: we leverage "build-tests" target to do cleanup because goclipse doesn't support customized build name)
* Click "Build Targets" --> "build" to build
* Click "Build Targets" --> "build-tests" to cleanup

(FYI, this go sample project is generally built by command line documented in BUILD section, the above steps are just for your reference. In addition, to use godef for symbol definition, need to add $GOROOT and {project}/vendor directories to GOPATH in Eclipse, but it'll result in build failure)
