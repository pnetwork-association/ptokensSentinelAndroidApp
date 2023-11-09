#!/bin/bash

# Setup plugdev permissions for the device
#
# Source:
#   - https://developer.android.com/studio/run/device
#   - http://www.janosgyerik.com/adding-udev-rules-for-usb-debugging-android-devices/
#
lsusb | grep -i google | awk '{print "/dev/bus/usb/"$2"/"$4}' | tr -d ':' | xargs sudo chown "$USER:plugdev"
adb kill-server
adb devices