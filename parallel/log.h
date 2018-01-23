//
// Created by paul on 11/14/17.
//

#ifndef BROWER_WALK_LOG_H
#define BROWER_WALK_LOG_H

#ifdef _MSC_VER
    #include <iostream>
    #include <string>
#else
    #include <stdio.h>
#endif

int log(std::string str) {
    std::cout << str << std::endl;
    return 0;
}

#endif //BROWER_WALK_LOG_H
