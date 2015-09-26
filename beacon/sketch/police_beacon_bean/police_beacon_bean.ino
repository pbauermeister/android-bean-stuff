/* police_beacon_bean.ino
**
** (C) 2015 by Pascal Bauermeister.
**
** LightBlueBean sketch to drive pin 1 according to LED red value.
** - Pin 1 is supposed to drive a load (beacon motor).
** - LED is supposed to be driven by a phone.
**
** Also, if the bean is shaken, the LED red value and hence the load
** are turned off.
**
** Code:    https://github.com/pbauermeister/android-bean-stuff
** Project: http://www.instructables.com/id/bluetooth-police-beacon
*/

#define PIN 1             // pin driving MOSFET (beacon motor)
#define SHAKE_THRESH  20  // acceleration diff between two samplings

/////////////////////////////////////////////////////////////////////////
// ACCELERATION VECTOR CLASS
/////////////////////////////////////////////////////////////////////////

struct Vector {
  int16_t x, y, z;
  void reset();
  int isSet();
  void copyFrom(Vector &other);
  void initFromSensor();
};

void Vector::reset() {
  x = y = z = 0;
};

int Vector::isSet() {
  return x != 0 || y != 0 || z != 0;
};

void Vector::copyFrom(Vector &other) {
  x = other.x;
  y = other.y;
  z = other.z;
}

void Vector::initFromSensor() {
  AccelerationReading accel = Bean.getAcceleration();
  x = accel.xAxis / 4;
  y = accel.yAxis / 4;
  z = accel.zAxis / 4;
  // Note: the values lie between 0 and around 250. To convert to
  // milli-g, please see:
  // http://legacy.punchthrough.com/bean/the-arduino-reference/accelerationreading/
};

/////////////////////////////////////////////////////////////////////////
// GLOBAL OBJECTS
/////////////////////////////////////////////////////////////////////////

static Vector last, current;
static int cnt = 0; // counter to blip the LED

/////////////////////////////////////////////////////////////////////////
// HELPERS
/////////////////////////////////////////////////////////////////////////

void handleAcceleration() {
  Bean.setLedGreen(255);    // blip LED.Green
  current.initFromSensor(); // sample acceleration
  Bean.setLedGreen(0);

  if (last.isSet()) {
    int16_t diffZ = abs(current.z-last.z);
    int16_t diffY = abs(current.y-last.y);
    int shaken = diffZ > SHAKE_THRESH || diffY > SHAKE_THRESH;
    if (shaken)
      Bean.setLed(0, 0, 0); //   reset LED

    // For test: red if shaken
    //Bean.setLed(shaken ? 255 : 0, 0, 0);
  }
  last.copyFrom(current);
}

/////////////////////////////////////////////////////////////////////////
// ARDUINO LIFECYCLE
/////////////////////////////////////////////////////////////////////////

void setup() {
    pinMode(PIN, OUTPUT);
    last.reset();
}

void loop() {
  // Use LED.Red (set by phone) to drive load
  int isOn = Bean.getLedRed() > 0;      // has phone turned Red on?
  Bean.setLedRed(isOn ? 10 : 0);        // fade back Red
  digitalWrite(PIN, isOn ? HIGH : LOW); // drive motor if Red not zero

  // Blip LED.Blue
  Bean.setLedBlue((cnt++&15)==1 ? 5 : 0);
  Bean.setLedBlue(0);

  // Acceleration handling
  if (!isOn) last.reset();
  else handleAcceleration();

  // Done
  Bean.sleep(50);
}

/////////////////////////////////////////////////////////////////////////
