# android-bean-stuff

This is an Android app to control BLE Beans (https://punchthrough.com/bean/).

![Screenshot](./screenshot.png)

It is built above the LightBlue Bean Android SDK (https://github.com/PunchThrough/Bean-Android-SDK).

Current features:
* Discover BT devices.
* Select device to be used and remembered.
* Push button turns on+off the LED.

Plans:
* Firmware to indicate per-bean layout (buttons, displays).
* Libraries AndroidSlidingUpPanel and sdk (of Bean-Android-SDK) to be referred to, instead of included.

In addition, the bean-police-beacon/ folder contains resources to
build a mini USB police rotating beacon, containing a Lightblue Bean,
and that can be controlled via this Android app:
* ./bean-police-beacon/CopyLedToPin1.ino -- Arduino sketch for the Bean
* The full tutorial is at http://www.instructables.com/id/Bluetooth-Police-Beacon
