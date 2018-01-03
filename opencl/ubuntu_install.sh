sudo apt-get update
sudo apt-get install gcc
sudo apt-get install opencl-headers python-pip python-dev python-numpy python-mako
wget https://pypi.python.org/packages/37/2e/0c142179d00bac23bd354b99fe5a575fe7c322f2acd3fc19f1d1f7b7c2ce/pyopencl-2017.2.2.tar.gz
tar -vxzf pyopencl-2017.2.2.tar.gz 
cd pyopencl-2017.2.2/
./configure.py 
sudo apt-get install build-essential libssl-dev libffi-dev python-dev
sudo apt install ocl-icd-opencl-dev
sudo python setup.py install

