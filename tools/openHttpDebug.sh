#!/bin/bash
# 打开apache.http调试信息

adb shell setprop log.tag.org.apache.http VERBOSE
adb shell setprop log.tag.org.apache.http.wire VERBOSE
adb shell setprop log.tag.org.apache.http.headers VERBOSE

echo "Enable Debug"
