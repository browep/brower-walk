cmake_minimum_required(VERSION 3.8)
project(brower_walk)

set(CMAKE_CXX_STANDARD 11)
add_compile_options(-fpermissive)

set(SOURCE_FILES
        main.cpp
        sha256.h sha256.cpp picohash.h)

add_executable(brower_walk ${SOURCE_FILES})