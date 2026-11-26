@echo off
rem Compile and run the app (Windows)
cd /d "%~dp0"
if exist out rmdir /s /q out
dir /s /b src\*.java > sources.txt
javac --release 17 -d out @sources.txt
del sources.txt
java -cp out com.snr.helpdesk.Main
