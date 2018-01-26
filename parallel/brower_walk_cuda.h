#ifndef BROWER_WALK_BROWER_WALK_CUDA_H
#define BROWER_WALK_BROWER_WALK_CUDA_H

typedef unsigned long long int uint64_cu;

void brower_walk_init_wrapper(uint64_cu    walk_size,
                              uint64_cu    s0,
                              uint64_cu    s1,
                              uint64_cu ** walk_path_gpu);

void brower_walk_wrapper(uint64_cu   num_steps,
                         uint64_cu   walk_size,
                         uint64_cu * walk_path_gpu,
                         uint64_cu * consumed_steps);

#endif //BROWER_WALK_BROWER_WALK_CUDA_H