#include <iostream>
#include <ctime>
#include <chrono>
#include <math.h>
#include <vector>
#include <cinttypes>
#include <sys/time.h>
#include "data.h"
#include <thread>
#include <cstring>
#include "picohash.h"
#include "log.h"

#include <string>
#include <sstream>

template <typename T>
std::string to_string(T value)
{
    std::ostringstream os ;
    os << value ;
    return os.str() ;
}


static const int UINT64_BYTE_COUNT = 8;
using namespace std;

uint64_t WALK_SIZE = (1024 * 1024 * 512) / 8;
uint64_t NUM_STEPS = pow(2, 19);

long long getLongLong(unsigned char *ca, bool differentEndian);

void uint64ToByteArr(uint64_t val, char result[UINT64_BYTE_COUNT]);

uint64_t xorByteArray(const char bytes[32], int left_start, int right_start) {

    char result[UINT64_BYTE_COUNT];

    for (int i = 0; i < UINT64_BYTE_COUNT; i++) {
        result[i] = (bytes[i + left_start] ^ bytes[i + right_start]);
    }

//    log("arr: %s\n", bytesToHexStr(reinterpret_cast<const unsigned char *>(result), 8).c_str());

    return getLongLong(reinterpret_cast<unsigned char *>(result), true);
}

long long getLongLong(unsigned char *ca, bool differentEndian) {
    long long retVal;

    if (differentEndian) {
        for (int i = 0; i < 4; i++) {
            unsigned char _tmpCh = ca[i];
            ca[i] = ca[7 - i];
            ca[7 - i] = _tmpCh;
        }
    }
    retVal = *reinterpret_cast<unsigned long long *>(ca);

    return retVal;
}

uint64_t gettime() {

    struct timeval tv;

    gettimeofday(&tv, NULL);

    unsigned long long millisecondsSinceEpoch =
            (unsigned long long) (tv.tv_sec) * 1000 +
            (unsigned long long) (tv.tv_usec) / 1000;

    return millisecondsSinceEpoch;
}

std::string bytesToHexStr(const unsigned char *digest, unsigned int size) {
    char buf[2 * size + 1];
    buf[2 * size] = 0;
    for (int i = 0; i < size; i++)
        sprintf(buf+i*2, "%02x", digest[i]);
    return std::string(buf);
}

std::string walk_wrapper(unsigned char block_header[], size_t block_header_size) {


    picohash_ctx_t block_header_context;
    char header_digest[PICOHASH_SHA256_DIGEST_LENGTH];

    picohash_init_sha256(&block_header_context);
    picohash_update(&block_header_context, block_header, block_header_size);
    picohash_final(&block_header_context, header_digest);

//    log("block header hash: " +
//        bytesToHexStr(reinterpret_cast<const unsigned char *>(header_digest), PICOHASH_SHA256_DIGEST_LENGTH));

    uint64_t s0 = xorByteArray(header_digest, 0, 16);
    uint64_t s1 = xorByteArray(header_digest, 8, 24);

//    log("s0: %llu\n", s0);
//    log("s1: %llu\n", s1);

    uint64_t start_path_creation_time = gettime();

    auto *walk_path = new uint64_t[WALK_SIZE];

    for (int i = 0; i < WALK_SIZE; i++) {
        uint64_t x = s0;
        uint64_t const y = s1;
        s0 = y;
        x ^= x << 23; // a
        s1 = x ^ y ^ (x >> 18) ^ (y >> 5); // b, c
        walk_path[i] = s1 + y;
    }

    uint64_t do_walk_start_time = gettime();

    uint64_t next_step = WALK_SIZE - 1;

    picohash_ctx_t path_context;

    picohash_init_sha256(&path_context);

    char new_val_byte_array[UINT64_BYTE_COUNT];

    for (int i = 0; i < NUM_STEPS; i++) {
        uint64_t val = walk_path[next_step];
        uint64_t new_val = (val << 1) + (i % 2);
        walk_path[next_step] = new_val;

        uint64ToByteArr(new_val, new_val_byte_array);
        picohash_update(&path_context, new_val_byte_array, UINT64_BYTE_COUNT);

        next_step = val % WALK_SIZE;

    }

    char path_digest[PICOHASH_SHA256_DIGEST_LENGTH];
    picohash_final(&path_context, path_digest);

    const string &final_hash = bytesToHexStr(reinterpret_cast<const unsigned char *>(path_digest), PICOHASH_SHA256_DIGEST_LENGTH);
    log("final hash: " +
        final_hash);

    log("path creation time: " + to_string((float)(do_walk_start_time - start_path_creation_time) / 1000));
    log("walk time: " +  to_string((float) (gettime() - do_walk_start_time) / 1000));
    log("total time: " + to_string((float) (gettime() - start_path_creation_time) / 1000) + "\n");


    delete[] walk_path;

    return final_hash;
}

void uint64ToByteArr(const uint64_t val, char result[UINT64_BYTE_COUNT]) {
    auto *p = (char *) &val;
    for (int i = 0; i < 8; i++) {
        result[i] = p[7 - i];
    }
}

std::string mine(){
    return walk_wrapper(block_header, sizeof(block_header));
}


