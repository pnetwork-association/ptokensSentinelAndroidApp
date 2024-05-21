# Android TEE wrapper

## Introduction

Given the low level nature of Trusted Execution Environments, an abstraction layer is needed in
order to facilitate the interaction for the end user. This project aims to create this layer by
creating a websocket connection to the host where the devices is attached with and will interpret
all the commands sent by the host to low levels calls.


## Architecture

The architecture consists into different layers:

 - React app: very thin layer which makes the developing on the device easier
 - App android: where the websocket client is started continously polling for the host connection
 - Java Native Interface (JNI): the command coming from the host is serialized in bytes and sent to a
   lower level call implemented in Rust
 - Core: the actual rust code which is being wrapped interacting with the underlying TEE. This is dynamically
   loaded when the Android app starts and where the commands are actually executed. Once the result is available
   it get serialized again and returned to the apps and forwarded to the host through the websocket connection.

## Setup

### Requirements

 - JAVA SDK 11 installed
 - Android SDK installed (v33)
 - Android NDK installed (v25)

### Optional

 - Android Studio

### Steps

1. Link the Rust library project into the rust folder (be sure it compiles when running `cargo build --release`):
2. Create a `local.properties` file into the `android/` folder with the path to the android SDK and NDK:

```env
sdk.dir=/opt/android-sdk
ndk.dir=/opt/android-sdk/ndk/25.2.9519653
```

3. Plug the device and run

```
npm run plugDevice
```

4. Run the reactive-app

```
npx react-native start
```

Press `a` in order to compile and install the app on the device.

5. Now the app is looking for a websocket connection to the port `3000` on localhost.
