#include <iostream>
#include <stdint.h>
#include <time.h>
#include <ctime>
#include <chrono>
#include <math.h>
#include <vector>
#include <cstdio>
#include <cinttypes>
#include <sys/time.h>
#include "data.h"
#include <thread>
#include <cstring>
#include "sha256.h"
#include "picohash.h"

#define LOG printf

static const int UINT64_BYTE_COUNT = 8;
using namespace std;

uint64_t WALK_SIZE = (1024 * 1024 * 512)/8;
uint64_t NUM_STEPS = pow(2, 19);


uint64_t xorshift128plus(uint64_t* s) {
	uint64_t x = s[0];
	uint64_t const y = s[1];
	s[0] = y;
	x ^= x << 23; // a
	s[1] = x ^ y ^ (x >> 18) ^ (y >> 5); // b, c
	return s[1] + y;
}

uint64_t xorByteArray(const char bytes[32], int left_start, int right_start) {

    char result[UINT64_BYTE_COUNT];

    for (int i = 0; i < UINT64_BYTE_COUNT; i++) {
        result[i] = (bytes[i + left_start] ^ bytes[i + right_start]);
    }

    LOG("arr: %s\n", bytesToHex(reinterpret_cast<const unsigned char *>(result), 8).c_str());

    return result[0]<<(8*7) | result[1]<<(8*6)
           | result[2]<<(8*5)
           | result[3]<<(8*4)
           | result[4]<<(8*3)
           | result[5]<<(8*2)
           | result[6]<<(8*1)
           | result[7];
}

uint64_t gettime() {

    struct timeval tv;

    gettimeofday(&tv, NULL);

    unsigned long long millisecondsSinceEpoch =
        (unsigned long long)(tv.tv_sec) * 1000 +
        (unsigned long long)(tv.tv_usec) / 1000;

    return millisecondsSinceEpoch;
}

uint64_t walk_wrapper(unsigned char block_header[], size_t block_header_size) {

    LOG("block header hash: %s\n", sha256(block_header, block_header_size).c_str());

    picohash_ctx_t ctx;
    char header_digest[PICOHASH_SHA256_DIGEST_LENGTH];

    picohash_init_sha256(&ctx);
    picohash_update(&ctx, block_header, block_header_size);
    picohash_final(&ctx, header_digest);

    LOG("block header hash: %s\n",
        bytesToHex(reinterpret_cast<const unsigned char *>(header_digest), SHA256::DIGEST_SIZE).c_str());

    uint64_t s[2];

    s[0] = xorByteArray(header_digest, 0, 16);
    s[1] = xorByteArray(header_digest, 8, 24);

    LOG("s0: %llu\n" , s[0]);
    LOG("s1: %llu\n" , s[1]);

    uint64_t start_walk_creation_time = gettime();

    auto *walk_path = new uint64_t[WALK_SIZE];

    for (int i = 0; i < WALK_SIZE; i++) {
        walk_path[i] = xorshift128plus(s);
    }

    uint64_t do_walk_start_time = gettime();

    uint64_t next_step = 0;

    for (int i = 0; i < NUM_STEPS ; i++) {
        uint64_t val = walk_path[next_step];
        uint64_t newVal = (val << 1) + (i % 2);
        walk_path[next_step] = newVal;
        next_step = val % WALK_SIZE;
    }

    LOG("%f       %f %llu %f\n",
        (float) (gettime() - start_walk_creation_time) / 1000, (float) (gettime() - do_walk_start_time) / 1000, next_step,  (float) (gettime() - start_walk_creation_time) / 1000 );

    delete[] walk_path;

    return next_step;
}

int main()
{

    //vector<unsigned char> hash(32);
//    picosha2::hash256(src_str.begin(), src_str.end(), hash.begin(), hash.end());

  //  string hex_str = picosha2::bytes_to_hex_string(hash.begin(), hash.end());

    //cout << hex_str << endl;

    //unsigned int concurentThreadsSupported = thread::hardware_concurrency();
    //LOG("threads: %s", concurentThreadsSupported);

	LOG("label           pathSize   pathCreationTime walkLength walkTime   lastStep  totalTime\n");

    walk_wrapper(block_header, sizeof(block_header));

    return 0;
}
