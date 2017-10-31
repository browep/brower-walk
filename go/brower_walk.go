package main

import (
	"fmt"
	"github.com/vpxyz/xorshift"
	"github.com/shirou/gopsutil/mem"
	//	"crypto/sha256"
	"time"
	"math"
	"runtime"
)

var (
	walkLength40MB  uint64 = 1024 * 1024 * 40 / 8;
	walkLength512MB uint64 = 1024 * 1024 * 512 / 8;
	numCycles       int    = 20
	numSteps        uint64 = uint64(math.Pow(2, 20))
)

func main() {
	fmt.Printf("starting\n")
	//sum := sha256.Sum256([]byte("hello world\n"))
	//fmt.Printf("%x", sum)
	//fmt.Printf("threadid label           pathSize   pathCreationTime walkLength walkTime   lastStep totalTime\n")

	//createAndDoWalk("512MB - 2^24", walkLength512MB, uint64(math.Pow(2, 24)))
	//createAndDoWalk(" 40MB - 2^24", walkLength40MB, uint64(math.Pow(2, 24)))

	v, _ := mem.VirtualMemory()

	// almost every return value is a struct
	fmt.Printf("Total: %v, Free:%v, UsedPercent:%f%%\n", v.Total, v.Free, v.UsedPercent)

	numWorkers := runtime.NumCPU() / 2;
	numWorkers = 1
	fmt.Printf("worker count: %d\n", numWorkers)

	c := make(chan float64)

	for i := 0; i < numWorkers; i++ {
		go createAndDoWalk(i, "512MB - 2^24", walkLength512MB, numSteps, c)
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

func createPath(pathLength uint64) []uint64 {

	start := time.Now()

	pathArray := make([]uint64, pathLength)

	tmpxs := xorshift.XorShift128Plus{}
	tmpxs.Init([]uint64{18446744073709551615, 13})

	pathArrayLength := len(pathArray)

	for i := 0; i < pathArrayLength; i++ {
		pathArray[i] = tmpxs.Next()
	}

	fmt.Sprintf("path creation time: %f\n", time.Now().Sub(start).Seconds())

	return pathArray
}

func createAndDoWalk(threadId int, label string, walkLength uint64, stepCount uint64, c chan float64) {
	for ; ; {
		start := time.Now()
		var path = createPath(walkLength)
		t := time.Now()
		elapsedPathCreation := t.Sub(start)
		startWalk := time.Now()
		lastStep := doWalk(path, stepCount)
		t = time.Now()
		elapsedWalkTime := t.Sub(startWalk)
		totalTime := time.Now().Sub(start).Seconds()
		fmt.Sprintf("%d %-10s    %-10d %-10f       %-10d %-10f %d %-10f\n",
			threadId, label, walkLength, elapsedPathCreation.Seconds(), stepCount, elapsedWalkTime.Seconds(), lastStep, totalTime)

		c <- totalTime

	}

}

func doWalk(path []uint64, stepCount uint64) uint64 {

	pathSize := uint64(len(path))

	nextStep := uint64(0);
	stepsTaken := uint64(0)

	for stepsTaken < stepCount {
		valueAtLocation := path[nextStep];
		path[nextStep] = (valueAtLocation << 1) + (stepsTaken % 2)
		nextStep = valueAtLocation % pathSize
		stepsTaken++
	}

	return nextStep
}
