# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.6.0)


enable_language(ASM)
add_library(fftw3f STATIC IMPORTED)
set_target_properties(fftw3f PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/math/fftw3/lib/libfftw3f.a)
add_library(fftw3f_threads STATIC IMPORTED)
set_target_properties(fftw3f_threads PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/math/fftw3/lib/libfftw3f_threads.a)

add_subdirectory(math_ext)

add_library(scope-lib   SHARED
         wavedata.cpp
         surface.cpp
         mem.cpp
         Wave2Bmp.cpp
         Wave2XYBmp.cpp
        math/math.cpp
         math/MathDualCalc.cpp
         math/MathFFT.cpp
         math/MathExpr.cpp
         measure/measure.cpp
         measure/MeasureCalc.cpp
         measure/MeasureItem.cpp
        hwdev.cpp
         XDmaDevice.cpp
        XDmaDevBitmap.cpp
        CV4L2Camera.cpp
        memcpy-advsimd.S
        ANativeWindowEx.cpp
        XDmaDevUser.cpp
        )



target_link_libraries(scope-lib
                      android
                      jnigraphics
                      fftw3f
                      fftw3f_threads
                      math_ext
                     log )