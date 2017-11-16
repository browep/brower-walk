# BrowerWalk
#### A memory-bound proof-of-work algorithm for cryptocurrencies

BrowerWalk is a memory bound proof-of-work algorithm that is resistant to ASIC and GPUs.  It is bounded by the read/write speed and size of the system's Random Access Memory.  The algorithm relies on creating a very large scratchpad ( 512MB currently ) from a 128bit seed and the xorshift128+ random number generator. 

High level pseudo-code

````
int NUM_BYTES_PER_ULONG64 = 8
long64 PATH_SIZE = 1024 *1024 * 512 / NUM_BYTES_PER_ULONG64; /*( 512MB scratch pad ( the path ) */
long64 NUM_WALK_STEPS = 2^19

function bool brower_walk ( byte[] input_data, byte[] difficulty  )

	/* sha256 the input data */
	byte[] seed = sha256(input_data)
	
	/* create random number generator with a 128bit seed by XOR'ing the first quarter 
	    with the thrird quarter and the  second quarter with the fourth */
	ulong64 left_seed =  new ulong64(seed[0..63] XOR seed[128..191])
	ulong64 right_seed = new ulong64(seed[64..127] XOR seed[192..256])
	val rng = new XorShiftRandomNumGen(left_seed, right_seed)
	
	/* create a scratch pad ( path ) of 512MB */
	val path = new ulong64[PATH_SIZE]
	
	/* fill the path with random data */
	for ( i in 0 to PATH_SIZE ) do
		path[i] = rng.nextLong()
	endfor
	
	/* walk the path 2^19 times using the val at each step to 
	* determine the next step, start with last value
	*/
	
	sha256Digest digest;
	
	long64 next_step = PATH_SIZE -1;
	for ( i in 0 to NUM_WALK_STEPS ) do
		val locationVal = path[next_step]
		/* bit shift and add random data to that location */
		val newVal = (locationVal << 1) + ( i % 2 )
		path[next_step] = newVal
		digest.update(newVal)
		next_step = locationVal % PATH_SIZE
	endfor
		
	byte[256] hash_result = digest.doFinal
	
	return hash_result < difficulty /* if result is less than difficuly then mining success */
	
end
		
````

## Description ##

BrowerWalk uses a 512MB scratch pad filled with pseudo random data that is then "walked" through randomly where the next step is determined by the data in the current step.  Using less than 512MB memory will result in a performance penalty as generating the entirety of data may be required if not stored. 

## Strengths ##


#### ASIC Resistance ####

Current Application Specific Integrated Circuits for cryptocurrency mining focus on fast implementations and parallelization with very little onboard memory.  No algorithm is immune to creation of an ASIC but some can make them less viable as they would need to compete with the economies of scale for consumer hardware.  BrowerWalk strives to achieve this by relying on DRAM which is already widely available.  A faster CPU does not reliably speed up the process as evidenced by the test runs on hardware with different CPU clocks and architecture.

#### GPU Resistance ####

GPUs can be thought of as a type of ASIC that is optimized for rendering graphics.  They use multiple cores ( often hundreds or thousands for newer models ) which are responsible for rendering regions of pixels.  This lends itself to excellent parallelization if there is a small memory footprint for each which for graphics rendering there usually is.  Any algorithm that has a low memory bound where each GPU core can have its own memory will be faster or more effecient on a GPU setup.  In the BrowerWalk algorithm the you are bound by the minimum of cores or number of 512MB blocks you can fit in system memory, more formally `MAX_PROCESSES = MIN( NUM_CORES, SYSTEM_MEMORY_MBS / 512 )`

Scaling your cores to hundreds will not improve results if the maximum memory does not scale with it.  This leads to an optimum hardware configruration of 1 CPU Core per 512MB.  The is in line with mobile devices and low end hardware.


## Weaknesses ##

#### Slow verification times

The algorithm requires you to read and write 512 MB of data to system memory.  This takes approxiamtely 0.35s on fast hardware.  Compare that to a single SHA256 call for Bitcoin's verification algorithm which could take as little as milliseconds.  Verifying blocks can take a long time and so nodes in a network may take a long to time to catch up to the latest blocks.  Verification however can be parallelized to mutlple machines.  

A slow verification can lead to DDOS attacks that make a node waste time on SPAM blocks.  DDOS tools such as banning nodes or IP addreses would need to be employed.

## Implemenations 

Included in this repository are four implemenations; C++, Java, Golang and an Android implementation with a C++/NDK implementation.  

For more instructions on building and deploying each.  See the following:

[Java Instructions](./JAVA_INSTRUCTIONS.md) 
[C++ Instrctions](./CPP_INSTRUCTIONS.md)
[Go Instructions](./GO_INTRUCTIONS.md)
[Android Instructions(./ANDROID_INSTRUCTIONS.md)



