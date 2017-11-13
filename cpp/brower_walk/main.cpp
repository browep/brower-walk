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
#include "sha256.h"

#define LOG printf

using namespace std;

uint64_t WALK_SIZE = (1024 * 1024 * 512)/8;
uint64_t NUM_STEPS = pow(2, 19);

/* The state must be seeded so that it is not zero */
uint64_t s[2];

uint64_t xorshift128plus(void) {
	uint64_t x = s[0];
	uint64_t const y = s[1];
	s[0] = y;
	x ^= x << 23; // a
	s[1] = x ^ y ^ (x >> 18) ^ (y >> 5); // b, c
	return s[1] + y;
}

uint64_t gettime() {

    struct timeval tv;

    gettimeofday(&tv, NULL);

    unsigned long long millisecondsSinceEpoch =
        (unsigned long long)(tv.tv_sec) * 1000 +
        (unsigned long long)(tv.tv_usec) / 1000;

    return millisecondsSinceEpoch;
}

void tohex(unsigned char * in, size_t insz, char * out, size_t outsz)
{
    unsigned char * pin = in;
    const char * hex = "0123456789ABCDEF";
    char * pout = out;
    for(; pin < in+insz; pout +=3, pin++){
        pout[0] = hex[(*pin>>4) & 0xF];
        pout[1] = hex[ *pin     & 0xF];
        pout[2] = ':';
        if (pout + 3 - out > outsz){
            /* Better to truncate output string than overflow buffer */
            /* it would be still better to either return a status */
            /* or ensure the target buffer is large enough and it never happen */
            break;
        }
    }
    pout[-1] = 0;
}

uint64_t walk_wrapper(unsigned char block_header[], size_t block_header_size) {


    const string &basic_string = sha256("foo");
    LOG("test, block header hash: %s\n", basic_string.c_str());

    LOG("block header hash: %s\n", sha256bytes(block_header, block_header_size));

    s[0] = 0;
    s[1] = 0;

    uint64_t start_walk_creation_time = gettime();

    auto *walk_path = new uint64_t[WALK_SIZE];

    for (int i = 0; i < WALK_SIZE; i++) {
        walk_path[i] = xorshift128plus();
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
