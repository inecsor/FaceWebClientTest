# Installation & Test Execution Guide

## 1. Setting Up Dependencies
To install all the required dependencies, navigate to project's root directory where the `package.json` file is located and install via these commands:

```Bash
cd facesdk_js_tests
npm install
```
```sh
git clone https://github.com/regulaforensics/FaceSDK-web-js-client.git
cd FaceSDK-web-js-client
npm install
npm run build
npm link
```
```Bash
cd ..
npm link @regulaforensics/facesdk-webclient
```
## 2. Running the Tests
Navigate to the test directory and execute the tests with the following commands:

```sh
npm test
```

## 3. Reports
You can view the run using report portal on http://172.20.40.141:8080/


Happy Testing!
