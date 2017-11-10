cmake_minimum_required(VERSION 3.8)
project(brower_walk)

set(CMAKE_CXX_STANDARD 11)

set(SOURCE_FILES
        main.cpp
        picosha2.h)

add_executable(brower_walk ${SOURCE_FILES})