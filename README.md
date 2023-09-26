[![badge](https://img.shields.io/twitter/follow/api_video?style=social)](https://twitter.com/intent/follow?screen_name=api_video)
&nbsp; [![badge](https://img.shields.io/github/stars/apivideo/api.video-android-player-analytics?style=social)](https://github.com/apivideo/api.video-android-player-analytics)
&nbsp; [![badge](https://img.shields.io/discourse/topics?server=https%3A%2F%2Fcommunity.api.video)](https://community.api.video)
![](https://github.com/apivideo/.github/blob/main/assets/apivideo_banner.png)
<h1 align="center">api.video Android player analytics module</h1>

[api.video](https://api.video) is the video infrastructure for product builders. Lightning fast
video APIs for integrating, scaling, and managing on-demand & low latency live streaming features in
your app.

# Table of contents

- [Table of contents](#table-of-contents)
- [Project description](#project-description)
- [Getting started](#getting-started)
    - [Installation](#installation)
        - [Gradle](#gradle)
    - [Permissions](#permissions)
- [Sample application](#sample-application)
- [Documentation](#documentation)
    - [Options](#options)
    - [ApiVideoPlayerAnalytics API](#apivideoplayeranalytics-api)
        - [`play(): Future<void>`](#playeventtime-float--currenttime-futurevoid)
        - [`resume(): Future<void>`](#resumeeventtime-float--currenttime-futurevoid)
        - [`ready(): Future<void>`](#readyeventtime-float--currenttime-futurevoid)
        - [`end(): Future<void>`](#endeventtime-float--currenttime-futurevoid)
        - [`seek(from: Float, to: Float): Future<void>`](#seekfrom-float-to-float-futurevoid)
        - [`pause(): Future<void>`](#pauseeventtime-float--currenttime-futurevoid)
        - [`destroy(): Future<void>`](#destroyeventtime-float--currenttime-futurevoid)
        - [`currentTime`](#currenttime)
    - [API documentation](#api-documentation)

# Project description

Android library to manually call the api.video analytics collector.

This is useful if you are using a video player for which we do not yet provide a ready-to-use
monitoring module.

# Getting started

## Installation

### Gradle

In your module `build.gradle`, add the following code in `dependencies`:

```groovy
dependencies {
    implementation 'video.api:android-player-analytics:2.0.0'
}
```

## Permissions

In your `AndroidManifest.xml`, add the following code in `<manifest>`:

```xml

<uses-permission android:name="android.permission.INTERNET" />
```

# Sample application

A demo application demonstrates how to use player analytics library.
See [`/example`](https://github.com/apivideo/api.video-android-player-analytics/tree/main/example)
folder.

# Documentation

## Options

The analytics module constructor takes a `Options` parameter that contains the following options:

|         Option name | Mandatory | Type                                                                                          | Description                                                                     |
|--------------------:|-----------|-----------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
|            mediaUrl | **        |                                                                                               |                                                                                 |
|               yes** | String    | url of the media (eg. `https://vod.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8`) |                                                                                 |
|           videoInfo | **        |                                                                                               |                                                                                 |
|               yes** | VideoInfo | information containing analytics collector url, video type (vod or live) and video id         |                                                                                 |
|            metadata | no        | ```Map<String, String>```                                                                     | object containing [metadata](https://api.video/blog/tutorials/dynamic-metadata) |
| onSessionIdReceived | no        | ```((sessionId: String) -> Unit)?```                                                          | callback called once the session id has been received                           |
|              onPing | no        | ```((message: PlaybackPingMessage) -> Unit)?```                                               | callback called before sending the ping message                                 |

Options instantiation is made with either mediaUrl or videoInfo.

Once the module is instantiated, the following methods have to be called to monitor the playback
events.

## ApiVideoPlayerAnalytics API

#### Event time or current time

If you know the event timestamp, you can use it as the `eventTime` parameter. If you don't know the
event timestamp, you can set the `currentTime` parameter with a scheduler.

#### `play(eventTime: Float = currentTime): Future<void>`

> method to call when the video starts playing for the first time (in the case of a resume after
> paused, use `resume()`)

#### `resume(eventTime: Float = currentTime): Future<void>`

> method to call when the video playback is resumed after a pause

#### `ready(eventTime: Float = currentTime): Future<void>`

> method to call once the player is ready to play the media

#### `end(eventTime: Float = currentTime): Future<void>`

> method to call when the video is ended

#### `seek(from: Float, to: Float): Future<void>`

> method to call when a seek event occurs, the `from` and `to` parameters are mandatory and should
> contains the seek start & end times in seconds

#### `pause(eventTime: Float = currentTime): Future<void>`

> method to call when the video is paused

#### `destroy(eventTime: Float = currentTime): Future<void>`

> method to call when the video player is disposed (eg. when the use closes the navigation tab)

#### `currentTime`

> field to call each time the playback time changes (it should be called often, the accuracy of the
> collected data depends on it) if you don't know event time.

## API documentation

A complete [API documentation](https://apivideo.github.io/api.video-android-player-analytics/) is
available.

