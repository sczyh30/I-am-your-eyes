# 我是你的眼

**I am your eyes** (我是你的眼) is an intelligent APP for blind people using
Microsoft Cognitive Service and IBM Bluemix.

本应用已获得 **IBM HACKxFDU 创新奖**。

## Features

- Environment Perception (Computer Vision - Image/Video Description)
- Speech Interaction

## Build

First you need to put the Baidu Speech TTS library (`*.so` files) into the
`app/src/main/jniLibs` directory. If you need offline speech data, you need to
put the offline data into correct path and change configurations as well.

Then you can build the application in the terminal:

```bash
gradle build
```

You can also import the project into Android Studio.

## Server Code

See here: [zpschang/your_eyes_server](https://github.com/zpschang/your_eyes_server).
