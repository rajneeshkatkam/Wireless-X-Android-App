# WIRELESS-X

## All in one android app that wirelessly controls keyboard, mouse of Laptop/Desktop and also acts as a Webcam device.

## Git repository link
    https://github.com/rajneeshkatkam/Wireless-X

## Team Members
    1. Rajneesh Katkam
    2. Ajinkya Jumbad
    3. Prashant Ravi
    4. Aditya Pradhan

## Introduction
    The total package of Wireless-X consist of an android app being backed by a Python server. Using this app, we have tried to eliminate the need to buy a wireless mouse, wireless keyboard and a dedicated web-cam. Instead, using this app, user can use his/her android phone’s screen as his mouse, a custom build keyboard layout as his wireless keyboard and his/her smartphone’s camera as web-cam. A Python server running on target laptop will capture these commands and emulate the effects on laptop.

## Motivation
    People come from different economical background and buying a wireless mouse, or keyboard or web-cam may not be easy for everyone. Even if they can manage to buy one, it is not efficient to do so when they have already invested on a smartphone. One thing, that is very common now-a-days across all section of society is having a smartphone, either a basic or a high-end one doesn’t matter. The required hardware to emulate a wireless keyboard, mouse or external web-cam is already present in a smartphone, and we just need to tailor it to make it usable for a common end-user.

## Working
    The Wireless-X app has two components: a python server running on laptop/PC and a client running on the android app. The user acts as a client where he/she sends the mouse and keyboard actions to be performed. If the user has turned on the camera, the camera frames are also sent to the python server. There are two sockets, one socket manages the mouse and keyboard actions, then the python program uses autopy and pynput libraries to translate these requests into the actual actions. The other socket is responsible for handling the camera frames, the python program uses the v4l2loopback to set up a camera virtual device on the laptop/PC and the pyfakewebcam library to use that virtual device in order to transmit camera frames. The OpenCV library is used by both android app and python server to encode and decode the camera frames respectively.

## Installation Setup

    1. Make sure that you are in the WirelessX_Source_Code/WirelessX-Python-Server directory (where install.sh file is present)

    2. Grant the permission to execute install.sh installation script using the following command:
            $ sudo chmod a+x install.sh

    3. Execute the install.sh script to install the necessary dependencies using the following command:
            $ sudo ./install.sh

## Running the application (Strictly follow the below order to run it successfully)
    NOTE: Before proceeding make sure that the Laptop/Desktop and the android phone are connected to same WiFi/Hotspot.

    1. Navigate to the WirelessX_Source_Code/WirelessX-Python-Server directory and run the Wireless-X server using the following command:
            $ python3 Wireless-X_server.py
    
    2. Enter your linux system password (the same password you enter while executing a command as "sudo"). This is required in order to set up the virtual webcam device.               

    3. Application Installation and Setup on Android Smartphone:

        a. Install the Wireless-X apk (you can copy the apk from the folder WirelessX_Source to your smartphone) on Android smartphone and give required permissions.

        b. Now, enter the IP address displayed in the terminal (on which the server is running) into the android application.
        
        c. Click on Test Button to test the connection of smartphone with the server. If failed, Recheck if you have entered correct IP address of Laptop/ Desktop (on which the server is running).

        d. After successful connection, you would be able to control mouse, keyboard of laptop and use smartphones camera as webcam for the laptop/Desktop.

        e. Now you would be able to use this virtual webcam device on Google Chrome for video conferencing. (Tested on Google Chrome for Microsoft Teams and Google Meet).

        f. (Optional) Inorder to test if camera frames are received to the Laptop/ Desktop, use the below command while (Note: camera option should be turned on in the 
            Wireless-X apk on Android):
                $ ffplay /dev/video20 


### *Extra (Inorder to remove v42loopback devices, use below command):
            
            $ sudo modprobe -r v4l2loopback


## Steps for Debugging (If python code doesn't run after above commands):

    1. Check if your virtual device is created

        $ ls /dev | grep -P '^video\d+$'
        OR
        $ v4l2-ctl --list-devices    # TO List the virtual devices in detail

            Output should look somewhat like this:

                Wireless-X Camera (platform:v4l2loopback-000):
                    /dev/video20

                Webcam C170: Webcam C170 (usb-0000:00:1a.0-1.2):
                    /dev/video0
                    /dev/video1

    2. Inorder to test if virtual device is working:
        Copy the sample code from  https://github.com/jremmons/pyfakewebcam page and save it as python file and run it.
            
            $ python3 demo.py

        If everything worked correctly, no error should be displayed and terminal should be blank.
        Now, Open another terminal and test if virtual device output is being display by entering below command:
            
            $ ffplay /dev/video20
        
        Note: ffplay

## References

OpenCV Reference:

    https://cmsdk.com/java/android-opencv-tcp-video-streaming.html


v4l2loopback References:

    https://github.com/jremmons/pyfakewebcam
    https://github.com/jremmons/pyfakewebcam/issues/5
    https://github.com/umlaeute/v4l2loopback#DKMS


Android Reference:

    https://stackoverflow.com/questions/23024831/android-shared-preferences-for-creating-one-time-activity-example
