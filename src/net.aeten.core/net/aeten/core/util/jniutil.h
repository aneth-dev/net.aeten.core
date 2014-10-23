#include <jni.h>
#include <errno.h>
#include <string.h>

#define NO_CLASS_DEF_FOUND_ERROR "java/lang/NoClassDefFoundError"
#define UNKNOWN_HOST_EXCEPTION "java/net/UnknownHostException"
#define INTERRUPTED_EXCEPTION "java/lang/InterruptedException"
#define INTERRUPTED_IO_EXCEPTION "java/io/InterruptedIOException"

static jint throw_no_class_def_error(JNIEnv *env, const char *message) {
	jclass exClass = (*env)->FindClass(env, NO_CLASS_DEF_FOUND_ERROR);
	return (*env)->ThrowNew(env, exClass, message);
}

static jint throw_error(JNIEnv *env, const char* className) {
	if (errno == EINTR)
		className = INTERRUPTED_EXCEPTION;
	jclass exClass = (*env)->FindClass(env, className);
	if (exClass == NULL) {
		return throw_no_class_def_error(env, className);
	}
	const char * message = strerror(errno);
	return (*env)->ThrowNew(env, exClass, message);
}
