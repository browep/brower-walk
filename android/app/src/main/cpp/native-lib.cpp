#include <jni.h>
#include "brower_walk.h"



extern "C"
JNIEXPORT jstring JNICALL
Java_com_github_browep_browerwalk_MainActivity_stringFromJNI(JNIEnv *env, jobject instance) {

    return env->NewStringUTF("from native");
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_github_browep_browerwalk_MainActivity_startMiner(JNIEnv *env, jobject instance) {

    while (true) {
        mine();
    }

    return env->NewStringUTF("done");
}