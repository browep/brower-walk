#!/usr/bin/env python

import hashlib
import binascii
import struct
from ctypes import c_longlong as ll
import pyopencl as cl

WALK_PATH_BYTES = 1024 * 1024 * 512
NUM_STEPS = 2**19

platform = cl.get_platforms()[0]    # Select the first platform [0]
device = platform.get_devices()[0]  # Select the first device on this platform [0]
ctx = cl.Context([device])          # Create a context with your devicequeue = cl.CommandQueue(ctx)

fh = open('./block_header.bin', 'rb')

block_header_bytes = bytearray(fh.read())
block_header_hash = hashlib.sha256(block_header_bytes).digest()

print("block header hash: " + binascii.hexlify(block_header_hash).decode('UTF-8', 'ignore'))

s0_arr = [ a ^ b for (a,b) in zip(block_header_hash[0:8], block_header_hash[16:24])]
s1_arr = [ a ^ b for (a,b) in zip(block_header_hash[8:16], block_header_hash[24:32])]

s0_int = int.from_bytes(s0_arr, byteorder='big')
s1_int = int.from_bytes(s1_arr, byteorder='big')

print(s0_int)
print(s1_int)

queue = cl.CommandQueue(ctx,
        properties=cl.command_queue_properties.PROFILING_ENABLE)


walk_buf = cl.Buffer(ctx, cl.mem_flags.READ_WRITE, size=WALK_PATH_BYTES)
walked_steps_buf = cl.Buffer(ctx, cl.mem_flags.READ_WRITE, size=NUM_STEPS*8)
prg = cl.Program(ctx, """
        #ifndef uint64_t
        #define uint64_t unsigned long
        #endif

        __kernel void brower_walk(__global const uint64_t *walk_buf,
        __global const uint64_t *walked_steps_buf)
        {
                    int gid = get_global_id(0);

        }
        """).build()
