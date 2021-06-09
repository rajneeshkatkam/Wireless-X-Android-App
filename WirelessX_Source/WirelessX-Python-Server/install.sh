#!/bin/bash
apt-get install python3-tk python3-dev
pip3 install opencv-python
pip3 install autopy
pip3 install pynput
pip3 install pyfakewebcam
apt install ffmpeg
apt install v4l2loopback-dkms v4l2loopback-utils
modprobe -r v4l2loopback
depmod -a
