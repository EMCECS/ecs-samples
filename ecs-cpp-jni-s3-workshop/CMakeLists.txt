# CMakeList.txt : CMake project for ecs-cpp-jni-s3-workshop, include source and define
# project specific logic here.
#
cmake_minimum_required (VERSION 3.8)

project ("ecs-cpp-jni-s3-workshop")

find_package(Java REQUIRED)
message(STATUS "JAVA_HOME= $ENV{JAVA_HOME}")
message(STATUS "")

find_package(JNI REQUIRED)
message(STATUS "JNI_INCLUDE_DIRS= ${JNI_INCLUDE_DIRS}")
message(STATUS "")

if (NOT MSVC)
	set(CMAKE_CXX_STANDARD 17)
	set(CMAKE_CXX_STANDARD_REQUIRED ON)
	find_library(STD_CPP_FS stdc++fs)
endif()

include_directories(${JNI_INCLUDE_DIRS} ${_classDir} ${_stubDir})

# Add source to this project's executable.
add_library(ecss3factorylib STATIC "lib/ecss3factory.cpp" "lib/ecss3client.cpp")
target_link_libraries(ecss3factorylib ${JNI_LIBRARIES} )

add_executable (example-getversion "examples/01_getversion.cpp")
target_link_libraries(example-getversion ${JNI_LIBRARIES} ecss3factorylib)

add_executable (example-listbucket "examples/02_listbucket.cpp")
target_link_libraries(example-listbucket ${JNI_LIBRARIES} ecss3factorylib)

if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET ecss3factorylib PROPERTY CXX_STANDARD 20)
  set_property(TARGET example-getversion PROPERTY CXX_STANDARD 20)
  set_property(TARGET example-listbucket PROPERTY CXX_STANDARD 20)
endif()

# TODO: Add tests and install targets if needed.
