#include <SoftwareSerial.h>

SoftwareSerial BTSerial(8, 9); // SoftwareSerial(RX, TX)

int relay1 = 2;
int relay2 = 3;
int relay3 = 4;
int relay4 = 5;

void setup() {
  BTSerial.begin(9600);
  Serial.begin(9600);

  pinMode(relay1, OUTPUT);
  digitalWrite(relay1, HIGH);
  pinMode(relay2, OUTPUT);
  digitalWrite(relay2, HIGH);
  pinMode(relay3, OUTPUT);
  digitalWrite(relay3, HIGH);
  pinMode(relay4, OUTPUT);
  digitalWrite(relay4, HIGH);
}

int i = 0;

void loop()
{
  byte recv[1000];

  if (BTSerial.available()) {
    int recvchar =  BTSerial.read();
    recv[i] = recvchar;
    Serial.println(recv[i]);
    i++;

    switch (recv[i]) {
      case 0 ... 5 :
        digitalWrite(relay1, LOW);
        delay(150);
        digitalWrite(relay1, HIGH);
        delay(150);
        break;

      case 6 ... 10 :
        digitalWrite(relay2, LOW);
        delay(150);
        digitalWrite(relay2, HIGH);
        delay(150);
        break;

      case 11 ... 101 :
        digitalWrite(relay3, LOW);
        delay(150);
        digitalWrite(relay3, HIGH);
        delay(150);
        break;
    }
  } 
}
