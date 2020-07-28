# Android-Cast-Local-Sample

![build](https://github.com/KaustubhPatange/Android-Cast-Local-Sample/workflows/build/badge.svg)
[![](https://jitpack.io/v/KaustubhPatange/Android-Cast-Local-Sample.svg)](https://jitpack.io/#KaustubhPatange/Android-Cast-Local-Sample)

The sample showcase the usage of [Cask-SDK](https://developers.google.com/cast) to create an Android sender app which plays local device media files on chromecast enabled devices.

> _This sample demonstrates playing of a local media file along with a subtitle track on chromecast enabled device._

> _This sample is built on top of [Google Cast Android sample](https://github.com/googlecast/CastVideos-android) for Android (which explain the way for casting remote files only). Hence it includes some code excerpt from it._

> _This sample uses a third-party module [tinyhttpd](https://github.com/dddge/TinyDroidHttpd) which is used to start a local HTTP Server to serve external storage directory as the root folder of the server. Note: This module is updated to fit according to the sample purpose._

## Anatomy

Sample's directory structure,

```
-- app
   -- tinyhttd  (a third party module to create HTTP server written in Java)
   -- src  (all cast-specific codes written in Kotlin)
```

- App connects to a receiver device.
- Starts an internal HTTP server on the device IP address.
- Constructs a [MediaInfo](https://developers.google.com/android/reference/com/google/android/gms/cast/MediaInfo) object with the url from the HTTP server along with a subtitle track.
- Cast the created [MediaInfo](https://developers.google.com/android/reference/com/google/android/gms/cast/MediaInfo) object to the receiver device.

## Run the sample

We are using some files from a [json](https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/f.json) file which was used in the [official cast sample](https://github.com/googlecast/CastVideos-android).

- Download [sample.mp4](https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/mp4/DesigningForGoogleCast.mp4) and [sample.vtt](https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/tracks/DesigningForGoogleCast-en.vtt) (subtitle) files. Make sure to rename the files as named. Alternatively you can use [**cURL**](https://github.com/curl/curl).

```
curl -L -o sample.mp4 https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/mp4/DesigningForGoogleCast.mp4
curl -L -o sample.vtt https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/tracks/DesigningForGoogleCast-en.vtt
```

- Push the files into the root storage of your device.

```
adb push sample.mp4 /sdcard
adb push sample.vtt /sdcard
```

- Clone the sample and run the app using Android studio. You can also install this [sample-debug-app](https://github.com/KaustubhPatange/Android-Cast-Local-Sample/releases/download/0.01/app-debug.apk).

```
git clone https://github.com/KaustubhPatange/Android-Cast-Local-Sample.git
```

## Using the code

If you are building your own sender app, instead of copying the whole [tinyhttpd](https://github.com/dddge/TinyDroidHttpd) module you can add the following dependencies to your build.gradle files.

This dependencies will make [SimpleWebServer](https://github.com/KaustubhPatange/Android-Cast-Local-Sample/blob/master/app/tinyhttpd/src/main/java/io/github/dkbai/tinyhttpd/nanohttpd/webserver/SimpleWebServer.java) class available for use.

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

```gradle
dependencies {
    implementation 'com.github.KaustubhPatange:Android-Cast-Local-Sample:Tag'
}
```

## References

- [Google Cast Sample for Android](https://github.com/googlecast/CastVideos-android)
- [Official Cast SDK Docs](https://developers.google.com/cast/docs)

## License

- [The Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)

```
Copyright 2020 Kaustubh Patange

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
