# BrowerWalk
#### A memory-bound proof-of-work algorithm for cryptocurrencies

BrowerWalk is a memory bound proof-of-work algorithm that is resistant to ASIC and GPUs.  It is bounded by the read/write speed and size of the system's Random Access Memory.  The algorithm relies on creating a very large scratchpad ( 512MB currently ) from a 128bit seed and the xorshift128+ random number generator. 

High level pseudo-code

````
long64 PATH_SIZE = 1024 *1024 * 512; # 512MB scratch pad ( the path )
long64 NUM_WALK_STEPS = 2^20

function bool brower_walk ( byte[] input_data, byte[] difficulty  )

	/* sha256 the input data */
	byte[] seed = sha256(input_data)
	
	/* create random number generator */
	ulong64 left_seed =  new ulong64(seed[0..63] XOR seed[128..191])
	ulong64 right_seed = new ulong64(seed[64..127] XOR seed[192..256])
	val rng = new XorShiftRandomNumGen(left_seed, right_seed)
	
	/* create a scrath pad ( path ) of 512MB */
	val path = new ulong64[PATH_SIZE / 8]
	
	/* fill the path with random data */
	for ( i in 0 to PATH_SIZE ) do
		path[i] = rng.nextLong()
	endfor
	
	/* walk the path 2^20 times using the val at each step to 
	* determine the next step, start with last value
	*/
	long64 next_step = PATH_SIZE -1;
	for ( i in 0 to NUM_WALK_STEPS ) do
		val locationVal = path[next_step]
		# bit shift and add random data to that location
		path[next_step] = (locationVal << 1) + ( i % 2 );
		next_step = locationVal % PATH_SIZE
	endfor
	
	/* grab 256 bits of data starting at next step and wrapping around */
	byte[] path_result = new byte[4]
	for (i in 0 to 4) do 
		path_result[i] = path[next_step]
		
		next_step++            /* increment along path */
		next_step %= PATH_SIZE /* wrap in case we are at the end of the path */

	endfor
	
	byte[256] hash_result = sha256(path_result)
	
	return hash_result < difficulty
		
````
