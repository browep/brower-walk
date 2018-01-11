#!/usr/bin/env python

import hashlib
import binascii
import struct
from ctypes import c_longlong as ll

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
