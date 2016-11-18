BUILD_OS ?= 
all: deps install check test

deps:
	go get -v github.com/constabulary/gb/...
	go get -v github.com/golang/lint/golint

install:
	GOOS=$(BUILD_OS) gb build

test:
	gb test utils/... -gocheck.v -test.short

check:
	go vet ./src/...
	golint ./src/...

clean:
	@rm -rf bin pkg

docker-build:
	@rm -rf bin/ tmp/
	@mkdir -p bin tmp
	docker run --rm \
		-e BUILD_OS=$(shell uname -s | tr A-Z a-z) \
		-v "$$PWD/src":/ecs-samples/src \
		-v "$$PWD/vendor":/ecs-samples/vendor \
		-v "$$PWD/Makefile":/ecs-samples/Makefile \
		-v "$$PWD/tmp":/ecs-samples/bin \
		-w /ecs-samples \
		golang:1.7 \
		make deps install check test
	@cp tmp/* bin/
	@rm -rf tmp/

.PHONY: deps install check test clean
