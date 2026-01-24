cmake_minimum_required(VERSION 3.4.1)

aux_source_directory(. DIR_SRCS)
file(STRINGS "./VERSION" CONFIG_VERSION)
add_definitions(-DCONFIG_VERSION=\"${CONFIG_VERSION}\")
add_definitions(-DCONFIG_BIGNUM)

add_library(quickjs STATIC ${DIR_SRCS})
