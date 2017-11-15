//
// Created by paul on 11/14/17.
//

#ifndef TESTCRYPTO_LOG_H
#define TESTCRYPTO_LOG_H


#include <iostream>
#include <android/log.h>

int log(std::string str) {
    __android_log_print(ANDROID_LOG_VERBOSE, "TestCrypto", "%s", str.c_str());
    return 0;
}

#endif //TESTCRYPTO_LOG_H
