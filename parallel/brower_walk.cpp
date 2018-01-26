#include <iostream>
#include <ctime>
#include <chrono>
#include <math.h>
#include <vector>
#include <cinttypes>
#ifndef _MSC_VER
#include <sys/time.h>
#endif
#include "data.h"
#include "log.h"
#include <thread>
#include <cstring>
#include "picohash.h"
#include "brower_walk_cuda.h"

#include <string>
#include <sstream>

template<typename T>
std::string to_string(T value) {
    std::ostringstream os;
    os << value;
    return os.str();
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

#ifndef _MSC_VER
uint64_t gettime() {

    struct timeval tv;

    gettimeofday(&tv, NULL);

    unsigned long long millisecondsSinceEpoch =
            (unsigned long long) (tv.tv_sec) * 1000 +
            (unsigned long long) (tv.tv_usec) / 1000;

    return millisecondsSinceEpoch;
}
#endif

std::string bytesToHexStr(const unsigned char *digest, char *buf, unsigned int size) {
    buf[2 * size] = 0;
    for (int i = 0; i < size; i++)
        sprintf(buf + i * 2, "%02x", digest[i]);
    return std::string(buf);
}

float walk_wrapper(unsigned char block_header[], size_t block_header_size) {

    // CUDA/OPENCL notes: this can be done in the CPU and main memory
    picohash_ctx_t block_header_context;
    char header_digest[PICOHASH_SHA256_DIGEST_LENGTH];

    picohash_init_sha256(&block_header_context);
    picohash_update(&block_header_context, block_header, block_header_size);
    picohash_final(&block_header_context, header_digest);

    uint64_t s0 = xorByteArray(header_digest, 0, 16);
    uint64_t s1 = xorByteArray(header_digest, 8, 24);

    // CUDA/OPENCL notes: this memory should be allocated in the system RAM
    auto *consumed_steps = new uint64_t[NUM_STEPS];

#ifndef _MSC_VER
    uint64_t start_path_creation_time = gettime();
#endif

    // CUDA/OPENCL notes: this memory should be allocated in the GPU
    uint64_t * walk_path_gpu;

    // CUDA/OPENCL notes: this is writing the memory in the GPU and should run as a GPU process
    brower_walk_init_wrapper(WALK_SIZE, s0, s1, &walk_path_gpu);

    // CUDA/OPENCL notes: this function profiling is not necessary and can be omitted
#ifndef _MSC_VER
    uint64_t do_walk_start_time = gettime();
#endif

    // CUDA/OPENCL notes: steps through the memory of the GPU
    brower_walk_wrapper(NUM_STEPS, WALK_SIZE, walk_path_gpu, consumed_steps);

    // CUDA/OPENCL notes: done with GPU processes and memory.  it can be cleaned up.  The rest of the function can be
    // run in the CPU and main memory
    char new_val_byte_array[UINT64_BYTE_COUNT];
    picohash_ctx_t path_context;

    picohash_init_sha256(&path_context);

    for (int i=0; i < NUM_STEPS; i++) {
        uint64ToByteArr(consumed_steps[i], new_val_byte_array);
        picohash_update(&path_context, new_val_byte_array, UINT64_BYTE_COUNT);
    }

    char path_digest[PICOHASH_SHA256_DIGEST_LENGTH];
    char path_digest_str[2*PICOHASH_SHA256_DIGEST_LENGTH+1];

    picohash_final(&path_context, path_digest);

    const string &final_hash = bytesToHexStr(reinterpret_cast<const unsigned char *>(path_digest),
                                             path_digest_str,
                                             PICOHASH_SHA256_DIGEST_LENGTH);
    log("final hash: " + final_hash);

#ifdef _MSC_VER
    return 0.0;
#else
    log("path creation time: " + to_string((float) (do_walk_start_time - start_path_creation_time) / 1000));
    log("walk time: " + to_string((float) (gettime() - do_walk_start_time) / 1000));
    float total_time = (float) (gettime() - start_path_creation_time) / 1000;
    log("total time: " + to_string(total_time) + "\n");

    return total_time;
#endif
}

void uint64ToByteArr(const uint64_t val, char result[UINT64_BYTE_COUNT]) {
    auto *p = (char *) &val;
    for (int i = 0; i < 8; i++) {
        result[i] = p[7 - i];
    }
}

float mine() {
    return walk_wrapper(block_header, sizeof(block_header));
}


