# Changelog

All changes to this project will be documented in this file.

## [3.0.1] - 2025-01-24

- Fix a crash on ExoPlayer Analytics when position < 0.0f

## [3.0.0] - 2024-07-24

- Use Analytics endpoint v2
- Use Kotlin DSL for Gradle

## [2.0.2] - 2023-10-09

- Fix a crash on ExoPlayer Analytics when position < 0.0f
- Synchronize generation of ping payload

## [2.0.1] - 2023-09-26

- Fix a crash with parsing of URL on old Android versions due to group named capture
- Display the exception in the default error handler

## [2.0.0] - 2023-09-26

- Replace Future by Callback to support Android >= 21
- Add support for Android 34
- CI: Upgrade GitHub Actions to fix warnings for documentation workflow

## [1.4.0] - 2023-08-11

- Migrate ExoPlayer to `media3`

## [1.3.1] - 2023-07-24

- Upgrade dependencies
- Fix obfuscation on release builds by updating Kotlin serialization plugin

## [1.3.0] - 2023-06-09

- Add support for api.video custom domains with `VideoInfo.fromMediaURL`

## [1.2.3] - 2023-30-03

- Fix private live stream URL parsing
- Replace `cdn.api.video` by `vod.api.video`

## [1.2.2] - 2023-02-01

- Fix a random 400 error when sending events
- Add an example to test ExoPlayer events

## [1.2.1] - 2022-09-28

- Add a specific library for ExoPlayer

## [1.2.0] - 2022-09-28

- Do not use

## [1.1.0] - 2022-05-02

- Rename main class from `PlayerAnalytics` to `ApiVideoPlayerAnalytics`
- Add a `eventTime` parameter to `ApiVideoPlayerAnalytics` methods

## [1.0.0] - 2022-01-07

- First version
