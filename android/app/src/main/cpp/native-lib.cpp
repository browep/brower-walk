#include <jni.h>
#include "brower_walk.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_github_browep_browerwalk_MainActivity_startMiner(JNIEnv *env, jobject obj,
                                                          jobject minerCallback) {
    jclass clazz = env->FindClass("com/github/browep/browerwalk/MinerCallback");
    jmethodID onHashMethod = env->GetMethodID(clazz, "onHash", "(Ljava/lang/String;)V");

    while(true) {
        float totalTime = mine();
        char buf[5];
        sprintf(buf, "%f", totalTime);
        env->CallVoidMethod(minerCallback, onHashMethod, env->NewStringUTF(buf));
    }

    return env->NewStringUTF("");
}