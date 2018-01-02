#define H0 0x6a09e667
#define H1 0xbb67ae85
#define H2 0x3c6ef372
#define H3 0xa54ff53a
#define H4 0x510e527f
#define H5 0x9b05688c
#define H6 0x1f83d9ab
#define H7 0x5be0cd19


uint rotr(uint x, int n) {
  if (n < 32) return (x >> n) | (x << (32 - n));
  return x;
}

uint ch(uint x, uint y, uint z) {
  return (x & y) ^ (~x & z);
}

uint maj(uint x, uint y, uint z) {
  return (x & y) ^ (x & z) ^ (y & z);
}

uint sigma0(uint x) {
  return rotr(x, 2) ^ rotr(x, 13) ^ rotr(x, 22);
}

uint sigma1(uint x) {
  return rotr(x, 6) ^ rotr(x, 11) ^ rotr(x, 25);
}

uint gamma0(uint x) {
  return rotr(x, 7) ^ rotr(x, 18) ^ (x >> 3);
}

uint gamma1(uint x) {
  return rotr(x, 17) ^ rotr(x, 19) ^ (x >> 10);
}


__kernel void sha256_crypt_kernel(uint ulen, __global char *plain_key, __global uint *digest){
  int t, gid, msg_pad;
  int stop, mmod;
  uint i, item, total;
  uint W[80], temp, A,B,C,D,E,F,G,H,T1,T2;
  int current_pad;

  uint K[64]={
0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
};

  msg_pad=0;

  total = ulen%64>=56?2:1 + ulen/64;

//  printf("ulen: %u total:%u\n", ulen, total);

  digest[0] = H0;
  digest[1] = H1;
  digest[2] = H2;
  digest[3] = H3;
  digest[4] = H4;
  digest[5] = H5;
  digest[6] = H6;
  digest[7] = H7;
  for(item=0; item<total; item++)
  {

    A = digest[0];
    B = digest[1];
    C = digest[2];
    D = digest[3];
    E = digest[4];
    F = digest[5];
    G = digest[6];
    H = digest[7];

#pragma unroll
    for (t = 0; t < 80; t++){
    W[t] = 0x00000000;
    }
    msg_pad=item*64;
    if(ulen > msg_pad)
    {
      current_pad = (ulen-msg_pad)>64?64:(ulen-msg_pad);
    }
    else
    {
      current_pad =-1;    
    }

  //  printf("current_pad: %d\n",current_pad);
    if(current_pad>0)
    {
      i=current_pad;

      stop =  i/4;
  //    printf("i:%d, stop: %d msg_pad:%d\n",i,stop, msg_pad);
      for (t = 0 ; t < stop ; t++){
        W[t] = ((uchar)  plain_key[msg_pad + t * 4]) << 24;
        W[t] |= ((uchar) plain_key[msg_pad + t * 4 + 1]) << 16;
        W[t] |= ((uchar) plain_key[msg_pad + t * 4 + 2]) << 8;
        W[t] |= (uchar)  plain_key[msg_pad + t * 4 + 3];
        //printf("W[%u]: %u\n",t,W[t]);
      }
      mmod = i % 4;
      if ( mmod == 3){
        W[t] = ((uchar)  plain_key[msg_pad + t * 4]) << 24;
        W[t] |= ((uchar) plain_key[msg_pad + t * 4 + 1]) << 16;
        W[t] |= ((uchar) plain_key[msg_pad + t * 4 + 2]) << 8;
        W[t] |=  ((uchar) 0x80) ;
      } else if (mmod == 2) {
        W[t] = ((uchar)  plain_key[msg_pad + t * 4]) << 24;
        W[t] |= ((uchar) plain_key[msg_pad + t * 4 + 1]) << 16;
        W[t] |=  0x8000 ;
      } else if (mmod == 1) {
        W[t] = ((uchar)  plain_key[msg_pad + t * 4]) << 24;
        W[t] |=  0x800000 ;
      } else /*if (mmod == 0)*/ {
        W[t] =  0x80000000 ;
      }
      
      if (current_pad<56)
      {
        W[15] =  ulen*8 ;
        //printf("ulen avlue 2 :w[15] :%u\n", W[15]);
      }
    }
    else if(current_pad <0)
    {
      if( ulen%64==0)
        W[0]=0x80000000;
      W[15]=ulen*8;
      //printf("ulen avlue 3 :w[15] :%u\n", W[15]);
    }

    for (t = 0; t < 64; t++) {
      if (t >= 16)
        W[t] = gamma1(W[t - 2]) + W[t - 7] + gamma0(W[t - 15]) + W[t - 16];
      T1 = H + sigma1(E) + ch(E, F, G) + K[t] + W[t];
      T2 = sigma0(A) + maj(A, B, C);
      H = G; G = F; F = E; E = D + T1; D = C; C = B; B = A; A = T1 + T2;
    }
    digest[0] += A;
    digest[1] += B;
    digest[2] += C;
    digest[3] += D;
    digest[4] += E;
    digest[5] += F;
    digest[6] += G;
    digest[7] += H;

  //  for (t = 0; t < 80; t++)
  //    {
  //    printf("W[%d]: %u\n",t,W[t]);
  //    }
  }


}

#ifndef uint32_t
#define uint32_t unsigned int
#endif

#ifndef uint64_t
#define uint64_t unsigned long
#endif

#ifndef size_t
#define size_t unsigned long
#endif

#define PICOHASH_SHA256_BLOCK_LENGTH 64
#define PICOHASH_SHA256_DIGEST_LENGTH 32

typedef struct {
    uint64_t length;
    uint32_t state[PICOHASH_SHA256_DIGEST_LENGTH / 4];
    uint32_t curlen;
    unsigned char buf[PICOHASH_SHA256_BLOCK_LENGTH];
} _picohash_sha256_ctx_t;

#define _picohash_sha256_ch(x, y, z) (z ^ (x & (y ^ z)))
#define _picohash_sha256_maj(x, y, z) (((x | y) & z) | (x & y))
#define _picohash_sha256_s(x, y)                                                                                                   \
    (((((uint32_t)(x)&0xFFFFFFFFUL) >> (uint32_t)((y)&31)) | ((uint32_t)(x) << (uint32_t)(32 - ((y)&31)))) & 0xFFFFFFFFUL)
#define _picohash_sha256_r(x, n) (((x)&0xFFFFFFFFUL) >> (n))
#define _picohash_sha256_sigma0(x) (_picohash_sha256_s(x, 2) ^ _picohash_sha256_s(x, 13) ^ _picohash_sha256_s(x, 22))
#define _picohash_sha256_sigma1(x) (_picohash_sha256_s(x, 6) ^ _picohash_sha256_s(x, 11) ^ _picohash_sha256_s(x, 25))
#define _picohash_sha256_gamma0(x) (_picohash_sha256_s(x, 7) ^ _picohash_sha256_s(x, 18) ^ _picohash_sha256_r(x, 3))
#define _picohash_sha256_gamma1(x) (_picohash_sha256_s(x, 17) ^ _picohash_sha256_s(x, 19) ^ _picohash_sha256_r(x, 10))
#define _picohash_sha256_rnd(a, b, c, d, e, f, g, h, i)                                                                            \
    t0 = h + _picohash_sha256_sigma1(e) + _picohash_sha256_ch(e, f, g) + K[i] + W[i];                                              \
    t1 = _picohash_sha256_sigma0(a) + _picohash_sha256_maj(a, b, c);                                                               \
    d += t0;                                                                                                                       \
    h = t0 + t1;

void _picohash_sha256_compress(__private _picohash_sha256_ctx_t *ctx, __private unsigned char *buf)
{
    uint32_t K[64] = {
        0x428a2f98UL, 0x71374491UL, 0xb5c0fbcfUL, 0xe9b5dba5UL, 0x3956c25bUL, 0x59f111f1UL, 0x923f82a4UL, 0xab1c5ed5UL,
        0xd807aa98UL, 0x12835b01UL, 0x243185beUL, 0x550c7dc3UL, 0x72be5d74UL, 0x80deb1feUL, 0x9bdc06a7UL, 0xc19bf174UL,
        0xe49b69c1UL, 0xefbe4786UL, 0x0fc19dc6UL, 0x240ca1ccUL, 0x2de92c6fUL, 0x4a7484aaUL, 0x5cb0a9dcUL, 0x76f988daUL,
        0x983e5152UL, 0xa831c66dUL, 0xb00327c8UL, 0xbf597fc7UL, 0xc6e00bf3UL, 0xd5a79147UL, 0x06ca6351UL, 0x14292967UL,
        0x27b70a85UL, 0x2e1b2138UL, 0x4d2c6dfcUL, 0x53380d13UL, 0x650a7354UL, 0x766a0abbUL, 0x81c2c92eUL, 0x92722c85UL,
        0xa2bfe8a1UL, 0xa81a664bUL, 0xc24b8b70UL, 0xc76c51a3UL, 0xd192e819UL, 0xd6990624UL, 0xf40e3585UL, 0x106aa070UL,
        0x19a4c116UL, 0x1e376c08UL, 0x2748774cUL, 0x34b0bcb5UL, 0x391c0cb3UL, 0x4ed8aa4aUL, 0x5b9cca4fUL, 0x682e6ff3UL,
        0x748f82eeUL, 0x78a5636fUL, 0x84c87814UL, 0x8cc70208UL, 0x90befffaUL, 0xa4506cebUL, 0xbef9a3f7UL, 0xc67178f2UL};
    uint32_t S[8], W[64], t, t0, t1;
    int i;

    /* copy state into S */
    for (i = 0; i < 8; i++)
        S[i] = ctx->state[i];

    /* copy the state into 512-bits into W[0..15] */
    for (i = 0; i < 16; i++)
        W[i] =
            (uint32_t)buf[4 * i] << 24 | (uint32_t)buf[4 * i + 1] << 16 | (uint32_t)buf[4 * i + 2] << 8 | (uint32_t)buf[4 * i + 3];

    /* fill W[16..63] */
    for (i = 16; i < 64; i++)
        W[i] = _picohash_sha256_gamma1(W[i - 2]) + W[i - 7] + _picohash_sha256_gamma0(W[i - 15]) + W[i - 16];

    /* Compress */
    for (i = 0; i < 64; ++i) {
        _picohash_sha256_rnd(S[0], S[1], S[2], S[3], S[4], S[5], S[6], S[7], i);
        t = S[7];
        S[7] = S[6];
        S[6] = S[5];
        S[5] = S[4];
        S[4] = S[3];
        S[3] = S[2];
        S[2] = S[1];
        S[1] = S[0];
        S[0] = t;
    }

    /* feedback */
    for (i = 0; i < 8; i++)
        ctx->state[i] = ctx->state[i] + S[i];
}

void _picohash_sha256_do_final(__private _picohash_sha256_ctx_t *ctx, __private void *digest, size_t len)
{
    __private unsigned char *out = digest;
    size_t i;

    /* increase the length of the message */
    ctx->length += ctx->curlen * 8;

    /* append the '1' bit */
    ctx->buf[ctx->curlen++] = (unsigned char)0x80;

    /* if the length is currently above 56 bytes we append zeros
     * then compress.  Then we can fall back to padding zeros and length
     * encoding like normal.
     */
    if (ctx->curlen > 56) {
        while (ctx->curlen < 64) {
            ctx->buf[ctx->curlen++] = (unsigned char)0;
        }
        _picohash_sha256_compress(ctx, ctx->buf);
        ctx->curlen = 0;
    }

    /* pad upto 56 bytes of zeroes */
    while (ctx->curlen < 56) {
        ctx->buf[ctx->curlen++] = (unsigned char)0;
    }

    /* store length */
    for (i = 0; i != 8; ++i)
        ctx->buf[56 + i] = ctx->length >> (56 - 8 * i);
    _picohash_sha256_compress(ctx, ctx->buf);

    /* copy output */
    for (i = 0; i != len / 4; ++i) {
        out[i * 4] = ctx->state[i] >> 24;
        out[i * 4 + 1] = ctx->state[i] >> 16;
        out[i * 4 + 2] = ctx->state[i] >> 8;
        out[i * 4 + 3] = ctx->state[i];
    }
}

void _picohash_sha256_init(__private _picohash_sha256_ctx_t *ctx)
{
    ctx->curlen = 0;
    ctx->length = 0;
    ctx->state[0] = 0x6A09E667UL;
    ctx->state[1] = 0xBB67AE85UL;
    ctx->state[2] = 0x3C6EF372UL;
    ctx->state[3] = 0xA54FF53AUL;
    ctx->state[4] = 0x510E527FUL;
    ctx->state[5] = 0x9B05688CUL;
    ctx->state[6] = 0x1F83D9ABUL;
    ctx->state[7] = 0x5BE0CD19UL;
}

void _picohash_sha256_update(__private _picohash_sha256_ctx_t *ctx, __private void *data, size_t len)
{
    __private unsigned char *in = data;
    size_t n;

    while (len > 0) {
        if (ctx->curlen == 0 && len >= PICOHASH_SHA256_BLOCK_LENGTH) {
            _picohash_sha256_compress(ctx, in);
            ctx->length += PICOHASH_SHA256_BLOCK_LENGTH * 8;
            in += PICOHASH_SHA256_BLOCK_LENGTH;
            len -= PICOHASH_SHA256_BLOCK_LENGTH;
        } else {
            n = PICOHASH_SHA256_BLOCK_LENGTH - ctx->curlen;
            if (n > len)
                n = len;
            //memcpy(ctx->buf + ctx->curlen, in, (size_t)n);
            for (size_t i = 0; i < n; i++)
                *(ctx->buf + ctx->curlen + i) = in[i];
            ctx->curlen += n;
            in += n;
            len -= n;
            if (ctx->curlen == 64) {
                _picohash_sha256_compress(ctx, ctx->buf);
                ctx->length += 8 * PICOHASH_SHA256_BLOCK_LENGTH;
                ctx->curlen = 0;
            }
        }
    }
}

void _picohash_sha256_final(__private _picohash_sha256_ctx_t *ctx, __private void *digest)
{
    _picohash_sha256_do_final(ctx, digest, PICOHASH_SHA256_DIGEST_LENGTH);
}

long long getLongLong(__private unsigned char *ca, bool differentEndian) {
    long long retVal;

    if (differentEndian) {
        for (int i = 0; i < 4; i++) {
            unsigned char _tmpCh = ca[i];
            ca[i] = ca[7 - i];
            ca[7 - i] = _tmpCh;
        }
    }
    
    retVal = *((__private long long *) ca);

    return retVal;
}

#define UINT64_BYTE_COUNT 8

uint64_t xorByteArray(__private const char bytes[32], int left_start, int right_start) {

    __private char result[UINT64_BYTE_COUNT];

    for (int i = 0; i < UINT64_BYTE_COUNT; i++) {
        result[i] = (bytes[i + left_start] ^ bytes[i + right_start]);
    }

    return getLongLong((__private unsigned char *) result, true);
}

void uint64ToByteArr(const uint64_t val, __private char result[UINT64_BYTE_COUNT]) {
    for (int i = 0; i < 8; i++) {
        result[i] = (val >> (64 - 8 * (i+1))) & 0xFF; //result[i] = p[7 - i];
    }
}

#define WALK_SIZE ((1024 * 1024 * 512) / 8)
#define NUM_STEPS (1 << 19)

__kernel void walk_wrapper(__global unsigned char block_header_glb[], size_t block_header_size) {

    // this should be optimized via shared memory
    __private unsigned char block_header[100];
    for (int i = 0; i < 100; i++)
        block_header[i] = block_header_glb[i];

    __private _picohash_sha256_ctx_t block_header_context[1];
    __private char header_digest[PICOHASH_SHA256_DIGEST_LENGTH];

    _picohash_sha256_init(block_header_context);
    _picohash_sha256_update(block_header_context, block_header, block_header_size);
    _picohash_sha256_final(block_header_context, header_digest);

    uint64_t s0 = xorByteArray(header_digest, 0, 16);
    uint64_t s1 = xorByteArray(header_digest, 8, 24);

    __private uint64_t walk_path[WALK_SIZE];

    for (int i = 0; i < WALK_SIZE; i++) {
        uint64_t x = s0;
        uint64_t const y = s1;
        s0 = y;
        x ^= x << 23; // a
        s1 = x ^ y ^ (x >> 18) ^ (y >> 5); // b, c
        walk_path[i] = s1 + y;
    }

    uint64_t next_step = WALK_SIZE - 1;

    __private _picohash_sha256_ctx_t path_context[1];
    _picohash_sha256_init(path_context);

    __private char new_val_byte_array[UINT64_BYTE_COUNT];

    for (int i = 0; i < NUM_STEPS; i++) {
        uint64_t val = walk_path[next_step];
        uint64_t new_val = (val << 1) + (i % 2);
        walk_path[next_step] = new_val;

        uint64ToByteArr(new_val, new_val_byte_array);
        _picohash_sha256_update(path_context, new_val_byte_array, UINT64_BYTE_COUNT);

        next_step = val % WALK_SIZE;
    }

    __private char path_digest[PICOHASH_SHA256_DIGEST_LENGTH];
    _picohash_sha256_final(path_context, path_digest);
}
