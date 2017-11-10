package main

import (
	"fmt"
	"github.com/vpxyz/xorshift"
	"github.com/shirou/gopsutil/mem"
	"time"
	"math"
	"runtime"
	"encoding/binary"
	"crypto/sha256"
	"encoding/hex"
)

var (
	walkLength512MB uint64 = 1024 * 1024 * 512 / 8;
	numSteps               = uint64(math.Pow(2, 19))
)

func main() {
	fmt.Printf("starting\n")

	blockerHeaderFilePath := "../java/src/main/resources/block_header.bin"
	blockHeader, err := Asset(blockerHeaderFilePath)
	if err != nil {
		fmt.Printf("could not find %s", blockerHeaderFilePath)
		return
	}

	v, _ := mem.VirtualMemory()

	// almost every return value is a struct
	fmt.Printf("Total: %v, Free:%v, UsedPercent:%f%%\n", v.Total, v.Free, v.UsedPercent)

	numWorkers := runtime.NumCPU() / 2;
	numWorkers = 1
	fmt.Printf("worker count: %d\n", numWorkers)

	c := make(chan float64)

	for i := 0; i < numWorkers; i++ {
		go createAndDoWalk(i, blockHeader, numSteps, c)
	}

	totalWalks := float64(0);
	startTime := time.Now();
	for ; ; {
		<-c
		totalWalks++;
		fmt.Printf("workers: %d avg: %f seconds per walk\n", numWorkers, time.Now().Sub(startTime).Seconds()/totalWalks)
	}

	fmt.Printf("done")

}

func createPath(blockHeader []byte) []uint64 {

	start := time.Now()

	sum := sha256.Sum256(blockHeader)
	fmt.Printf("%x\n", sum)

	fmt.Printf("leftArr:  %x\n", sum[0:8])
	fmt.Printf("rightArr:  %x\n", sum[16:24])
	fmt.Printf("leftArr:  %x\n", sum[8:16])
	fmt.Printf("rightArr:  %x\n", sum[24:32])

	s0 := xorByteArray(sum[0:8], sum[16:24])
	s1 := xorByteArray(sum[8:16], sum[24:32])

	fmt.Printf("s0: %d\n", s0)
	fmt.Printf("s1: %d\n", s1)

	pathArray := make([]uint64, walkLength512MB)

	rng := xorshift.XorShift128Plus{}
	rng.Init([]uint64{s0, s1})

	pathArrayLength := len(pathArray)

	for i := 0; i < pathArrayLength; i++ {
		pathArray[i] = rng.Next()
	}

	fmt.Sprintf("path creation time: %f\n", time.Now().Sub(start).Seconds())

	return pathArray
}

func createAndDoWalk(threadId int, blockHeader []byte, stepCount uint64, c chan float64) {
	//for ; ; {
	start := time.Now()
	var path = createPath(blockHeader)
	t := time.Now()
	elapsedPathCreation := t.Sub(start)

	startWalk := time.Now()
	lastStep := doWalk(path)
	t = time.Now()
	elapsedWalkTime := t.Sub(startWalk)
	totalTime := time.Now().Sub(start).Seconds()

	fmt.Printf("%d %-10f       %-10d %-10f %s %-10f\n",
		threadId, elapsedPathCreation.Seconds(), stepCount, elapsedWalkTime.Seconds(), hex.EncodeToString(lastStep), totalTime)

	c <- totalTime

	//}

}

func doWalk(path []uint64) []byte {

	pathLength := uint64(len(path))
	nextStep := pathLength -1 ;

	sha_256 := sha256.New();
	byteBuffer := make([]byte, 8)

	for i := uint64(0); i < numSteps; i++ {
		valueAtLocation := path[nextStep];
		newValueAtLocation := (valueAtLocation << 1) + (i % 2)
		path[nextStep] = newValueAtLocation

		binary.BigEndian.PutUint64(byteBuffer, newValueAtLocation)

		sha_256.Write(byteBuffer)

		nextStep = valueAtLocation % pathLength

		if (i < 10) {
			fmt.Printf("next step: %d\n" , nextStep)
		}
	}

	return sha_256.Sum(nil)
}

func xorByteArray(leftArr []byte, rightArr []byte) uint64 {

	resArr := make([]byte, len(leftArr))

	for i := 0; i < len(rightArr) && i < len(leftArr); i++ {
		resArr[i] = leftArr[i] ^ rightArr[i]
	}

	fmt.Printf("result arr: %x\n", resArr)

	return binary.BigEndian.Uint64(resArr)
}
