/* CopyLedToPin1.ino
** 
** LightBlueBean sketch to drive pin 1 according to LED red value.
** Pin 1 is supposed to drive a load (beacon motor).
** LED is supposed to be driven by a phone.
**
** Also, if the bean is shaken, the LED red value and the load are
** turned off.
** 
** (C) 2015 by Pascal Bauermeister.
**
** Code:    https://github.com/pbauermeister/android-bean-stuff
** Project: http://www.instructables.com/id/bluetooth-police-beacon
*/

#define PIN 1  // Pin driving MOSFET

#define SHAKE_THRESH  20

/////////////////////////////////////////////////////////////////////////
// ACCELERATION VECTOR CLASS
/////////////////////////////////////////////////////////////////////////

class Vector {
public:
  int16_t x, y, z;
  void reset();
  int isSet();
  void copyFrom(Vector &other);
  void initFromSensor();

private:
  int16_t convertToMg(int16_t rawAcceleration, uint8_t sensitivity);
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
};

/////////////////////////////////////////////////////////////////////////
// HELPERS
/////////////////////////////////////////////////////////////////////////

Vector last, current;

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

int cnt = 0; // counter to blip the LED

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
