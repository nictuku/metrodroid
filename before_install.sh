#!/bin/bash
set -e
android list target
echo y | android update sdk -a --no-ui --filter android-16 --all
# echo y | android update sdk -a --no-ui --filter sys-img-armeabi-v7a-android-16 --all
android list target
echo no | avdmanager create avd --force -n test -g default --abi armeabi-v7a -k 'system-images;android-16;default;armeabi-v7a'
emulator -list-avds
emulator -avd test -no-skin -no-window &
./android-wait-for-emulator
adb shell input keyevent 82 &


