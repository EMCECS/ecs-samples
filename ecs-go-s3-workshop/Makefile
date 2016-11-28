BUILD_OS ?= 
export GOPATH ?= /tmp
export PATH := $(PATH):$(GOPATH)/bin

all: deps install check test

deps:
	go get -v github.com/constabulary/gb/...
	go get -v github.com/golang/lint/golint
	if [ ! -d "./vendor" ]; then \
		mkdir -p vendor; \
		$(GOPATH)/bin/gb vendor fetch github.com/aws/aws-sdk-go; \
		$(GOPATH)/bin/gb vendor fetch github.com/jacobstr/confer; \
		$(GOPATH)/bin/gb vendor fetch gopkg.in/check.v1; \
	fi

install:
	GOOS=$(BUILD_OS) $(GOPATH)/bin/gb build cmd/...

test:
	$(GOPATH)/bin/gb test utils/... -gocheck.v -test.short

check:
	go vet ./src/cmd/...
	go vet ./src/utils/...
	$(GOPATH)/bin/golint ./src/cmd/...
	$(GOPATH)/bin/golint ./src/utils/...

clean:
	@rm -rf bin pkg vendor src/github.com src/golang.org

docker-build:
	@rm -rf bin/ tmp/
	@mkdir -p bin tmp
	docker run --rm \
		-e BUILD_OS=$(shell uname -s | tr A-Z a-z) \
		-v "$$PWD/src":/ecs-samples/src \
		-v "$$PWD/Makefile":/ecs-samples/Makefile \
		-v "$$PWD/tmp":/ecs-samples/bin \
		-w /ecs-samples \
		golang:1.7 \
		make deps install check test
	@cp tmp/* bin/
	@rm -rf tmp/

.PHONY: deps install check test clean docker-build
