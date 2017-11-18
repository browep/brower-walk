#include <jni.h>
#include "brower_walk.h"

extern "C"
JNIEXPORT void JNICALL
Java_com_github_browep_browerwalk_MainActivity_startMiner(JNIEnv *env, jobject instance) {

    mine();

}