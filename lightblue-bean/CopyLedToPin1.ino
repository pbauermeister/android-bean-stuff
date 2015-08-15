/* CopyLedToPin1.ino
** 
** LightBlueBean sketch to drive pin 1 according to LED value.
** 
** (C) 2015 by Pascal Bauermeister.
**
** Code:    https://github.com/pbauermeister/android-bean-stuff
** Project: http://www.instructables.com/id/bluetooth-police-beacon
*/

#define PIN 1  // Pin driving MOSFET

void setup() {
    pinMode(PIN, OUTPUT);
}

int cnt = 0; // counter to blip the LED

void loop() {
    uint8_t value = Bean.getLedRed();        // Read red value R of LED
    digitalWrite(PIN, value?HIGH:LOW);       // Drive high if R not zero

    Bean.setLedBlue((cnt++&15)==1 ? 5 : 0);  // This blips the LED's blue
    Bean.setLedBlue(0);                      //   value

    Bean.sleep(50);
}
