#include <SoftwareSerial.h>

SoftwareSerial BTSerial(8, 9); // SoftwareSerial(RX, TX)
//boolean temp = 0;
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


void loop()
{

  while (BTSerial.available()) {
    byte data  =  BTSerial.read();

    Serial.write(data);

    if (-128 <= data < -40) {
      digitalWrite(relay1, LOW);
    }
    else if (-40 <= data < 40) {
      digitalWrite(relay2, LOW);
    }

    else if (data <= 40) {
      digitalWrite(relay3, LOW);
    }

  }

}

