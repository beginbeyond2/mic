# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)



find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )





add_library(baselib   SHARED
             src/main/cpp/utils.cpp
              )

target_link_libraries(baselib

                   ${log-lib} )