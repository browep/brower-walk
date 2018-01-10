#!/usr/bin/env python

import hashlib
import binascii

fh = open('./block_header.bin', 'rb')

block_header_bytes = bytearray(fh.read())
block_header_hash = hashlib.sha256(block_header_bytes).digest()

print "block header hash: " + binascii.hexlify(block_header_hash)
