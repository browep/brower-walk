sudo apt-get update -y
sudo apt-get upgrade -y linux-aws
sudo reboot
sudo apt-get install -y gcc make linux-headers-$(uname -r)
sudo apt-get install -y python-setuptools python-dev build-essential libssl-dev libffi-dev
cat << EOF | sudo tee --append /etc/modprobe.d/blacklist.conf
blacklist vga16fb
blacklist nouveau
blacklist rivafb
blacklist nvidiafb
blacklist rivatv
EOF
cat /etc/modprobe.d/blacklist.conf
sudo vim /etc/default/grub #GRUB_CMDLINE_LINUX="modprobe.blacklist=nouveau"
sudo update-grub
wget http://us.download.nvidia.com/XFree86/Linux-x86_64/367.106/NVIDIA-Linux-x86_64-367.106.run
chmod +x NVIDIA-Linux-x86_64-367.106.run
sudo /bin/bash ./NVIDIA-Linux-x86_64-367.106.run
sudo reboot
nvidia-smi
sudo apt-get install ocl-icd-opencl-dev
sudo apt-get install nvidia-opencl-icd-384
sudo apt-get install opencl-headers
wget https://pypi.python.org/packages/37/2e/0c142179d00bac23bd354b99fe5a575fe7c322f2acd3fc19f1d1f7b7c2ce/pyopencl-2017.2.2.tar.gz
tar -xvf pyopencl-2017.2.2.tar.gz
cd pyopencl-2017.2.2/
sudo easy_install pip
sudo pip install mako
./configure.py
sudo python setup.py install
sudo apt install clinfo
clinfo
