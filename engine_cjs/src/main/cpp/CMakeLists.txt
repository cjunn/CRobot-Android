cmake_minimum_required(VERSION 3.22.1)

project("engine")

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../libs/${ANDROID_ABI})
ADD_SUBDIRECTORY(${CMAKE_CURRENT_SOURCE_DIR}/core)
ADD_SUBDIRECTORY(${CMAKE_CURRENT_SOURCE_DIR}/quickjs)
ADD_SUBDIRECTORY(${CMAKE_CURRENT_SOURCE_DIR}/cjs)

add_library(engine SHARED main.cpp)

find_library(log-lib log)
target_link_libraries(engine cjs core ${log-lib} quickjs)