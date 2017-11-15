#include <jni.h>
#include "brower_walk.h"



extern "C"
JNIEXPORT jstring JNICALL
Java_com_github_browep_browerwalk_MainActivity_stringFromJNI(JNIEnv *env, jobject instance) {

    return env->NewStringUTF("from native");
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_github_browep_browerwalk_MainActivity_startMiner(JNIEnv *env, jobject obj,
                                                          jobject minerCallback) {
    jclass clazz = env->FindClass("com/github/browep/browerwalk/MinerCallback");
    jmethodID onHashMethod = env->GetMethodID(clazz, "onHash", "(Ljava/lang/String;)V");

    while(true) {
        std::string finalHash = mine();
        env->CallVoidMethod(minerCallback, onHashMethod);
    }

    return env->NewStringUTF("");
}