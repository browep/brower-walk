sudo apt-get update
sudo apt-get -y install gcc
sudo apt-get -y install opencl-headers python-pip python-dev python-numpy python-mako
wget https://pypi.python.org/packages/37/2e/0c142179d00bac23bd354b99fe5a575fe7c322f2acd3fc19f1d1f7b7c2ce/pyopencl-2017.2.2.tar.gz
tar -vxzf pyopencl-2017.2.2.tar.gz 
cd pyopencl-2017.2.2/
./configure.py 
sudo apt-get -y install build-essential libssl-dev libffi-dev python-dev
sudo apt -y install ocl-icd-opencl-dev
sudo python setup.py install
# CUDA INSTALL
wget http://developer.download.nvidia.com/compute/cuda/7_0/Prod/local_installers/cuda_7.0.28_linux.run
chmod +x cuda_7.0.28_linux.run 
mkdir nvidia_installers
./cuda_7.0.28_linux.run -extract=`pwd`/nvidia_installers
sudo apt-get -y install linux-image-extra-virtual
sudo vi /etc/modprobe.d/blacklist-nouveau.conf
echo options nouveau modeset=0 | sudo tee -a /etc/modprobe.d/nouveau-kms.conf
sudo update-initramfs -u

ch aws
    4  sudo apt-get update -y
    5  sudo apt-get upgrade -y linux-aws
    6  sudo reboot
    7  sudo yum install -y gcc kernel-devel-$(uname -r)
    8  sudo apt-get install -y gcc make linux-headers-$(uname -r)
    9  cat << EOF | sudo tee --append /etc/modprobe.d/blacklist.conf
   10  blacklist vga16fb
   11  blacklist nouveau
   12  blacklist rivafb
   13  blacklist nvidiafb
   14  blacklist rivatv
   15  EOF
   16  cat /etc/modprobe.d/blacklist.conf 
   17  sudo vim /etc/default/grub
   18  sudo grub2-mkconfig -o /boot/grub2/grub.cfg
   19  sudo update-grub
   20  wget http://us.download.nvidia.com/XFree86/Linux-x86_64/367.106/NVIDIA-Linux-x86_64-367.106.run
   21  chmod +x NVIDIA-Linux-x86_64-367.106.run 
   22  sudo /bin/bah ./NVIDIA-Linux-x86_64-367.106.run 
   23  sudo /bin/bash ./NVIDIA-Linux-x86_64-367.106.run 
   24  sudo reboot
   25  nvidia-smi 
   26  sudo apt-get install nvidia-libopencl1
   27  sudo apt-get install ocl-icd-opencl-dev
   28  sudo apt-get install nvidia-opencl-icd
   29  sudo apt-get install nvidia-opencl-icd-384
   30  sudo apt-get install opencl-headers
   31  wget https://pypi.python.org/packages/37/2e/0c142179d00bac23bd354b99fe5a575fe7c322f2acd3fc19f1d1f7b7c2ce/pyopencl-2017.2.2.tar.gz
   32  tar -xvf pyopencl-2017.2.2.tar.gz 
   33  cd pyopencl-2017.2.2/
   34  ls -l
   35  ./configure.py 
   36  su -c "python ez_setup.py" # this will install setuptools
   37  sudo python ez_setup.py
   38  which pip
   39  sudo apt-get install pip
   40  sudo apt-get install python-setuptools python-dev build-essential 
   41  pwd
   42  ./configure.py 
   43  sudo python setup.py install
   44  cd ..
   45  pip
   46  sudo easy_install pip
   47  sudo pip install maki
   48  sudo pip install mako
   49  cd -
   50  sudo python setup.py install
   51  sudo apt-get install build-essential libssl-dev libffi-dev python-dev
   52  sudo python setup.py install
   53  cd ..
   54  python
   55  nvidia-smi 
   56  clinfo
   57  sudo apt install clinfo
   58  clinfo
   59  sudo apt install clinfopwd
   60  pwd
   61  sudo su
   62  history

