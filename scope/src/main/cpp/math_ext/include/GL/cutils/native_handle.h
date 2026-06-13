//
// Created by liwb on 2025-3-5.
//

#ifndef OPENCLIMAGE_NATIVE_HANDLE_H
#define OPENCLIMAGE_NATIVE_HANDLE_H

#include <stdalign.h>

#ifdef __cplusplus
extern "C" {
#endif

#define NATIVE_HANDLE_MAX_FDS 1024
#define NATIVE_HANDLE_MAX_INTS 1024

/* Declare a char array for use with native_handle_init */
#define NATIVE_HANDLE_DECLARE_STORAGE(name, maxFds, maxInts) \
    alignas(native_handle_t) char (name)[                            \
      sizeof(native_handle_t) + sizeof(int) * ((maxFds) + (maxInts))]

typedef struct native_handle
{
    int version;        /* sizeof(native_handle_t) */
    int numFds;         /* number of file-descriptors at &data[0] */
    int numInts;        /* number of ints at &data[numFds] */
#if defined(__clang__)
    #pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wzero-length-array"
#endif
    int data[0];        /* numFds + numInts ints */
#if defined(__clang__)
#pragma clang diagnostic pop
#endif
} native_handle_t;

typedef const native_handle_t* buffer_handle_t;

/*
 * native_handle_close
 *
 * closes the file descriptors contained in this native_handle_t
 *
 * return 0 on success, or a negative error code on failure
 *
 */
int native_handle_close(const native_handle_t* h);

/*
 * native_handle_init
 *
 * Initializes a native_handle_t from storage.  storage must be declared with
 * NATIVE_HANDLE_DECLARE_STORAGE.  numFds and numInts must not respectively
 * exceed maxFds and maxInts used to declare the storage.
 */
native_handle_t* native_handle_init(char* storage, int numFds, int numInts);

/*
 * native_handle_create
 *
 * creates a native_handle_t and initializes it. must be destroyed with
 * native_handle_delete(). Note that numFds must be <= NATIVE_HANDLE_MAX_FDS,
 * numInts must be <= NATIVE_HANDLE_MAX_INTS, and both must be >= 0.
 *
 */
native_handle_t* native_handle_create(int numFds, int numInts);

/*
 * native_handle_clone
 *
 * creates a native_handle_t and initializes it from another native_handle_t.
 * Must be destroyed with native_handle_delete().
 *
 */
native_handle_t* native_handle_clone(const native_handle_t* handle);

/*
 * native_handle_delete
 *
 * frees a native_handle_t allocated with native_handle_create().
 * This ONLY frees the memory allocated for the native_handle_t, but doesn't
 * close the file descriptors; which can be achieved with native_handle_close().
 *
 * return 0 on success, or a negative error code on failure
 *
 */
int native_handle_delete(native_handle_t* h);


#ifdef __cplusplus
}
#endif


#endif //OPENCLIMAGE_NATIVE_HANDLE_H
