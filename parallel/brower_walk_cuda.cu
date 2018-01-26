#include "brower_walk_cuda.h"
#include <stdio.h>
#include <inttypes.h>

#define BLOCKS_NUM        1
#define THREADS_PER_BLOCK 1

__global__
void brower_walk_init(uint64_cu walk_size, uint64_cu s0, uint64_cu s1, uint64_cu *walk_path)
{
    for (int i = 0; i < walk_size; i++) {
        uint64_cu x = s0;
        uint64_cu const y = s1;
        s0 = y;
        x ^= x << 23; // a
        s1 = x ^ y ^ (x >> 18) ^ (y >> 5); // b, c
        walk_path[i] = s1 + y;
    }
}

__global__
void brower_walk(uint64_cu num_steps, uint64_cu walk_size, uint64_cu * walk_path, uint64_cu * consumed_steps)
{
    uint64_cu next_step = walk_size - 1;

    for (int i = 0; i < num_steps; i++) {
        uint64_t val = walk_path[next_step];
        uint64_t new_val = (val << 1) + (i % 2);
        walk_path[next_step] = new_val;

        consumed_steps[i] = new_val;

        next_step = val % walk_size;
    }
}

static void cuda_errors_handle(void)
{
    cudaError_t errSync  = cudaGetLastError();
    cudaError_t errAsync = cudaDeviceSynchronize();
    if (errSync != cudaSuccess)
    {
        printf("Sync kernel error: %s\n", cudaGetErrorString(errSync));
        exit(0);
    }
    if (errAsync != cudaSuccess)
    {
        printf("Async kernel error: %s\n", cudaGetErrorString(errAsync));
        exit(0);
    }
}

void brower_walk_init_wrapper(uint64_cu walk_size,
                              uint64_cu s0,
                              uint64_cu s1,
                              uint64_cu ** walk_path_gpu)
{
    cudaMalloc(walk_path_gpu, walk_size*sizeof(uint64_cu)); 

    brower_walk_init<<<BLOCKS_NUM, THREADS_PER_BLOCK>>>(walk_size, s0, s1, *walk_path_gpu);

    // Wait for GPU to finish.
    cudaDeviceSynchronize();
    cuda_errors_handle();
}

void brower_walk_wrapper(uint64_cu   num_steps,
                         uint64_cu   walk_size,
                         uint64_cu * walk_path_gpu,
                         uint64_cu * consumed_steps)
{
    uint64_cu * consumed_steps_gpu;
    cudaMalloc(&consumed_steps_gpu, num_steps*sizeof(uint64_cu)); 

    brower_walk<<<BLOCKS_NUM, THREADS_PER_BLOCK>>>(num_steps, walk_size, walk_path_gpu, consumed_steps_gpu);

    cudaMemcpy(consumed_steps, consumed_steps_gpu, num_steps*sizeof(uint64_cu), cudaMemcpyDeviceToHost);

    cudaFree(walk_path_gpu);
    cudaFree(consumed_steps_gpu);
    cuda_errors_handle();
}

