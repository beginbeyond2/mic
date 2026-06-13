# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)



add_library(property-lib  SHARED
            src/main/cpp/aes_generic.cpp
            src/main/cpp/cmicsigcrypto.cpp
            src/main/cpp/convert_2_35.cpp
            src/main/cpp/Int128.cpp
            src/main/cpp/micsigcrypto.cpp
            src/main/cpp/smart.cpp
            src/main/cpp/Property.cpp
)
target_link_libraries(property-lib
                      log )