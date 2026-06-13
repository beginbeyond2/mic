# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.
set(CMAKE_C_COMPILER_WORKS TRUE)
set(CMAKE_CXX_COMPILER_WORKS TRUE)

project("math_ext")

set(CMAKE_VERBOSE_MAKEFILE  on)

#message("cmake path:${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/")

include_directories(${CMAKE_SOURCE_DIR}/math_ext/include)
#
#add_library(OpenCL SHARED IMPORTED)
#set_target_properties(
#        OpenCL
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libOpenCL.so"
#)
#add_library(GLES_mali SHARED IMPORTED)
#set_target_properties(
#        GLES_mali
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libGLES_mali.so"
#)
#add_library(utilscallstack SHARED IMPORTED)
#set_target_properties(
#        utilscallstack
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libutilscallstack.so"
#)
#add_library(cutils SHARED IMPORTED)
#set_target_properties(
#        cutils
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libcutils.so"
#)
#add_library(base SHARED IMPORTED)
#set_target_properties(
#        base
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libbase.so"
#)
#add_library(c++ SHARED IMPORTED)
#set_target_properties(
#        c++
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libc++.so"
#)
#add_library(utils SHARED IMPORTED)
#set_target_properties(
#        utils
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libutils.so"
#)
#add_library(processgroup SHARED IMPORTED)
#set_target_properties(
#        processgroup
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libprocessgroup.so"
#)
#add_library(cgrouprc SHARED IMPORTED)
#set_target_properties(
#        cgrouprc
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libcgrouprc.so"
#)
#add_library(vndksupport SHARED IMPORTED)
#set_target_properties(
#        vndksupport
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libvndksupport.so"
#)
#add_library(dl_android SHARED IMPORTED)
#set_target_properties(
#        dl_android
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libdl_android.so"
#)
#add_library(ld-android SHARED IMPORTED)
#set_target_properties(
#        ld-android
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/ld-android.so"
#)
#add_library(backtrace SHARED IMPORTED)
#set_target_properties(
#        backtrace
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libbacktrace.so"
#)
#add_library(unwindstack SHARED IMPORTED)
#set_target_properties(
#        unwindstack
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libunwindstack.so"
#)
#add_library(lzma SHARED IMPORTED)
#set_target_properties(
#        lzma
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/liblzma.so"
#)
#add_library(mapper40 SHARED IMPORTED)
#set_target_properties(
#        mapper40
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/android.hardware.graphics.mapper@4.0.so"
#)
#add_library(common10 SHARED IMPORTED)
#set_target_properties(
#        common10
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/android.hardware.graphics.common@1.0.so"
#)
#add_library(hidlbase SHARED IMPORTED)
#set_target_properties(
#        hidlbase
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libhidlbase.so"
#)
#add_library(common11 SHARED IMPORTED)
#set_target_properties(
#        common11
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/android.hardware.graphics.common@1.1.so"
#)
#add_library(common12 SHARED IMPORTED)
#set_target_properties(
#        common12
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/android.hardware.graphics.common@1.2.so"
#)
#add_library(gralloctypes SHARED IMPORTED)
#set_target_properties(
#        gralloctypes
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libgralloctypes.so"
#)
#add_library(graphics_common_v2_ndk_platform SHARED IMPORTED)
#set_target_properties(
#        graphics_common_v2_ndk_platform
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/android.hardware.graphics.common-V2-ndk_platform.so"
#)
#add_library(common_v2_ndk_platform SHARED IMPORTED)
#set_target_properties(
#        common_v2_ndk_platform
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/android.hardware.common-V2-ndk_platform.so"
#)
#add_library(hardware SHARED IMPORTED)
#set_target_properties(
#        hardware
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libhardware.so"
#)
#add_library(dmabufheap SHARED IMPORTED)
#set_target_properties(
#        dmabufheap
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libdmabufheap.so"
#)
#
#add_library(nativeWindow SHARED IMPORTED)
#set_target_properties(
#        nativeWindow
#        PROPERTIES
#        IMPORTED_LOCATION
#        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libnativeWindow.so"
#)



# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        math_ext

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp
        ClUtils.cpp
        OpenCLCommon.cpp
        OpenCLWrapper.h
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log
        android
        )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        math_ext
        android
        # Links the target library to the log library
        # included in the NDK.

#        dmabufheap
#        hardware
#        common_v2_ndk_platform
#        graphics_common_v2_ndk_platform
#        gralloctypes
#        common12
#        common11
#        hidlbase
#        common10
#        mapper40
#        lzma
#        unwindstack
#        backtrace
#        ld-android
#        dl_android
#        vndksupport
#        cgrouprc
#        processgroup
#        utils
#        c++
#        base
#        cutils
#        utilscallstack
#        GLES_mali
#        OpenCL
        ${opencl-lib}
        ${log-lib})