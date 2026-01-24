cmake_minimum_required(VERSION 3.4.1)

project(core)

aux_source_directory(. DIR_SRCS)

add_library(${PROJECT_NAME} STATIC ${DIR_SRCS})
unset(DIR_SRCS)