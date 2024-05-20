# Installation & Test Execution Guide

## 1. Setting Up Dependencies
Before launching tests, you need to install the required dependencies.

At first clone client repositor
```bash
git clone https://github.com/regulaforensics/FaceSDK-web-java-client.git 
cd FaceSDK-web-java-client
```
On MacOS
```bash
./gradlew build
```
On Windows 
```bash
gradlew.bat build
```
Then you need to copy client.jar that was generated in FaceSDK-web-java-client\client\build\libs
to facesdk_java_tests\libs

```bash
cd ..
```

On MacOS
```bash
./gradlew build
```
On Windows
```bash
gradlew.bat build
```
## 2. Generating report
You can access the run using report portal on http://172.20.40.141:8080/
Happy Testing!
