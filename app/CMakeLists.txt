# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

#add_library( # Sets the name of the library.
#        native-lib
#
#        # Sets the library as a shared library.
#        SHARED
#
#        # Provides a relative path to your source file(s).
#        # Associated headers in the same location as their source
#        # file are automatically included.
#        src/main/cpp/native-lib.cpp)
#
# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
#
#find_library( # Sets the name of the path variable.
#        log-lib
#
#        # Specifies the name of the NDK library that
#        # you want CMake to locate.
#        log)
#
# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

#target_link_libraries( # Specifies the target library.
#        native-lib

        # Links the target library to the log library
        # included in the NDK.
 #       ${log-lib})


############################### SCPI #################################################

add_library(SCPI SHARED
        src/main/jni/SCPI/SCPICommandDeal.cpp
        src/main/jni/SCPI/SCPICommandCallBackJava.cpp
        src/main/jni/SCPI/SCPI_Parser/src/debug.c
        src/main/jni/SCPI/SCPI_Parser/src/error.c
        src/main/jni/SCPI/SCPI_Parser/src/fifo.c
        src/main/jni/SCPI/SCPI_Parser/src/ieee488.c
        src/main/jni/SCPI/SCPI_Parser/src/minimal.c
        src/main/jni/SCPI/SCPI_Parser/src/parser.c
        src/main/jni/SCPI/SCPI_Parser/src/units.c
        src/main/jni/SCPI/SCPI_Parser/src/utils.c
        src/main/jni/SCPI/SCPI_Parser/common/scpi-def.c
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_auto.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_calibrate.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_chan.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_curs.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_disp.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_mask.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_math.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_meas.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_menu.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_ref.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_samp.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_stor.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_test.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_tim.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_trig.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_wav.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_bus.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_cmd_production.cpp
        src/main/jni/SCPI/SCPI_Parser/common/scpi_help.cpp)

target_link_libraries(SCPI

        log)

add_library(jni_pinyinime SHARED
        src/main/cpp/android/com_keanbin_pinyinime_PinyinDecoderService.cpp
        src/main/cpp/share/dictbuilder.cpp
        src/main/cpp/share/dictlist.cpp
        src/main/cpp/share/dicttrie.cpp
        src/main/cpp/share/lpicache.cpp
        src/main/cpp/share/matrixsearch.cpp
        src/main/cpp/share/mystdlib.cpp
        src/main/cpp/share/ngram.cpp
        src/main/cpp/share/pinyinime.cpp
        src/main/cpp/share/searchutility.cpp
        src/main/cpp/share/spellingtable.cpp
        src/main/cpp/share/spellingtrie.cpp
        src/main/cpp/share/splparser.cpp
        src/main/cpp/share/userdict.cpp
        src/main/cpp/share/utf16char.cpp
        src/main/cpp/share/utf16reader.cpp
        src/main/cpp/share/sync.cpp)

include_directories(src/main/cpp/include/)

add_library(engine
        STATIC
        src/main/cpp/share/dictbuilder.cpp
        src/main/cpp/share/dictlist.cpp
        src/main/cpp/share/dicttrie.cpp
        src/main/cpp/share/lpicache.cpp
        src/main/cpp/share/mystdlib.cpp
        src/main/cpp/share/ngram.cpp
        src/main/cpp/share/searchutility.cpp
        src/main/cpp/share/spellingtable.cpp
        src/main/cpp/share/spellingtrie.cpp
        src/main/cpp/share/splparser.cpp
        src/main/cpp/share/utf16char.cpp
        src/main/cpp/share/utf16reader.cpp )

target_link_libraries(jni_pinyinime
        android
        log
        z
        engine)
