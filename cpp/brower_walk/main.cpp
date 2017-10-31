#include <iostream>
#include <stdint.h>
#include <time.h>
#include <ctime>
#include <chrono>
#include <math.h>
#include <vector>
#include "picosha2.h"
#include <cstdio>
#include <cinttypes>
#include <sys/time.h>
#include <thread>

#define LOG printf

using namespace std;

// 40 MB walk size
uint64_t walk_size_40MB = (1024 * 1024 * 40)/8;
uint64_t walk_size_1GB = 1024 * 1024 * 1024;
uint64_t walk_size_512MB = (1024 * 1024 * 512)/8;

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

uint64_t walk_wrapper(char* walk_label, uint64_t walk_size, uint64_t left_seed, uint64_t right_seed, uint64_t num_steps ) {

    uint64_t start_walk_creation_time = gettime();

    s[0] = left_seed;
    s[1] = right_seed;

    uint64_t *walk_path = new uint64_t[walk_size];

    for (int i = 0; i < walk_size; i++) {
        walk_path[i] = xorshift128plus();
    }

    uint64_t do_walk_start_time = gettime();

    uint64_t next_step = 0;

    for (int i = 0; i < num_steps; i++) {
        uint64_t val = walk_path[next_step];
        walk_path[next_step] = (val << 1) + (i % 2);
        uint64_t last_step = next_step;
        next_step = val % walk_size;
    }

    LOG("%s     %llu   %f       %llu   %f %llu %f\n",
        walk_label, walk_size, (float) (gettime() - start_walk_creation_time) / 1000, walk_size, (float) (gettime() - do_walk_start_time) / 1000, next_step,  (float) (gettime() - start_walk_creation_time) / 1000 );

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

	//walk_wrapper("40MB - 2^24", walk_size_40MB, 18446744073709551615LL, 13LL, pow(2, 24) );
	while(true) {
        walk_wrapper("512MB - 2^24", walk_size_512MB, 18446744073709551615LL, 13LL, pow(2, 20) );
	}

    return 0;
}
