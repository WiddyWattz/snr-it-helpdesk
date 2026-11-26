#!/bin/sh
# Compile and run the app (Linux / macOS)
set -e
cd "$(dirname "$0")"
javac --release 17 -d out $(find src -name '*.java')
java -cp out com.snr.helpdesk.Main
