project("chineseocr")
cmake_minimum_required(VERSION 3.22.1)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/../libs/${ANDROID_ABI})

## ncnn 库
option(OCR_LITE_VULKAN "OcrLite Enable Vulkan" ON)
#set(OCR_LITE_VULKAN ON)
if (OCR_LITE_VULKAN)
    set(ncnn_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../ncnn-20221128-android-vulkan/${ANDROID_ABI}/lib/cmake/ncnn")
else ()
    set(ncnn_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../ncnn-20221128-android/${ANDROID_ABI}/lib/cmake/ncnn")
endif ()
find_package(ncnn REQUIRED)

## opencv 库
set(OpenCV_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../opencv-mobile-3.4.15-android/sdk/native/jni")
find_package(OpenCV REQUIRED)
if (OpenCV_FOUND)
    message(STATUS "OpenCV_LIBS: ${OpenCV_LIBS}")
    message(STATUS "OpenCV_INCLUDE_DIRS: ${OpenCV_INCLUDE_DIRS}")
else ()
    message(FATAL_ERROR "opencv Not Found!")
endif (OpenCV_FOUND)

file(GLOB OCR_SRC src/*.cpp)
set(OCR_COMPILE_CODE ${OCR_SRC})

add_library(OcrLite SHARED ${OCR_COMPILE_CODE})
target_link_libraries(OcrLite ncnn ${OpenCV_LIBS})