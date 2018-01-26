TARGET = brower_walk

# Compiler
CC = nvcc
# C++ related flags
CPP_FLAGS = -Xcompiler="-fpermissive -std=c++11"

inc = $(wildcard *.h)
src = $(wildcard *.cpp)
obj = $(src:.cpp=.o)
cuda_src = $(wildcard *.cu)
cuda_obj = $(cuda_src:.cu=.o)

$(TARGET): $(obj) $(cuda_obj)
	$(CC) -o $@ $^ $(CPP_FLAGS)

%.o: %.cpp $(inc)
	$(CC) -c $< -o $@ $(CPP_FLAGS)

%.o: %.cu $(inc)
	$(CC) -c $< -o $@

.PHONY: clean
clean:
	git clean -xdf