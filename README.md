<!--<documentation_excluded>-->
[![badge](https://img.shields.io/twitter/follow/api_video?style=social)](https://twitter.com/intent/follow?screen_name=api_video)
&nbsp; [![badge](https://img.shields.io/github/stars/apivideo/api.video-android-player-analytics?style=social)](https://github.com/apivideo/api.video-android-player-analytics)
&nbsp; [![badge](https://img.shields.io/discourse/topics?server=https%3A%2F%2Fcommunity.api.video)](https://community.api.video)
![](https://github.com/apivideo/.github/blob/main/assets/apivideo_banner.png)
<h1 align="center">api.video Android player analytics</h1>

[api.video](https://api.video) is the video infrastructure for product builders. Lightning fast
video APIs for integrating, scaling, and managing on-demand & low latency live streaming features in
your app.

## Table of contents

- [Table of contents](#table-of-contents)
- [Project description](#project-description)
- [Getting started](#getting-started)
    - [Installation](#installation)
      - [Gradle](#gradle)
    - [Code sample](#code-sample)
- [Sample application](#sample-application)
- [Documentation](#documentation)
- [FAQ](#faq)

<!--</documentation_excluded>-->
<!--<documentation_only>
---
title: api.video Android player analytics
meta:
  description: The official api.video Android player analytics library for api.video. [api.video](https://api.video/) is the video infrastructure for product builders. Lightning fast video APIs for integrating, scaling, and managing on-demand & low latency live streaming features in your app.
---

</documentation_only>-->

## Project description

This library sends player events from the player to api.video.

## Getting started

### Installation

#### Gradle

For ExoPlayer, add the following code in your module `build.gradle`:

```groovy
dependencies {
    implementation 'video.api.player.analytics:android-player-analytics-exoplayer:3.0.0'
}
```

### Code sample

Register the ExoPlayer player analytics agent with:

```kotlin
import androidx.media3.exoplayer.ExoPlayer
import video.api.player.analytics.exoplayer.extensions.addApiVideoAnalyticsListener

val exoplayer = ExoPlayer.Builder(context).build()
val listener =
    exoplayer.addApiVideoAnalyticsListener() // Register the ApiVideo exoplayer analytics listener so it sends player events to api.video.

// Remove the analytics listener when you don't need it anymore.
exoplayer.removeAnalyticsListener(listener)
```

For a custom domain collector, use:

```kotlin
val listener =
    exoplayer.addApiVideoAnalyticsListener(collectorUrl = "https://collector.mycustomdomain.com") // Register the player analytics listener so it sends player events to api.video.
```

## Sample application

A demo application demonstrates how to use player.
See [`/example`](https://github.com/apivideo/api.video-android-player-analytics/tree/main/example)
folder.

While running the example, you can set your video Id:

1. Enter a new media Id
2. Press on `Load` button

## Documentation

A complete [Android player analytics documentation](https://apivideo.github.io/api.video-android-player-analytics/) is
available.

## FAQ

If you have any questions, ask us in the [community](https://community.api.video) or use [Issues](https://github.com/apivideo/api.video-android-player-analytics/issues).
