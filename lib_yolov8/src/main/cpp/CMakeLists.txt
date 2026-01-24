project("yolov8")
cmake_minimum_required(VERSION 3.22.1)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/../libs/${ANDROID_ABI})

set(OpenCV_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../opencv-mobile-4.13.0-android/sdk/native/jni)
find_package(OpenCV REQUIRED core imgproc)

set(ncnn_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../ncnn-20260113-android-vulkan/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)

file(GLOB YOLO8_SRC src/*.cpp)
set(YOLO8_COMPILE_CODE ${YOLO8_SRC})

add_library(YoloV8 SHARED ${YOLO8_COMPILE_CODE})
target_link_libraries(YoloV8 ncnn ${OpenCV_LIBS})