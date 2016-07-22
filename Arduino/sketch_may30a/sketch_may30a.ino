#include <SoftwareSerial.h>

SoftwareSerial BTSerial(8, 9); // SoftwareSerial(RX, TX)
//boolean temp = 0;
int relay1 = 2;
int relay2 = 3;
int relay3 = 4;
int relay4 = 5;

int lay1State = 0;
int lay2State = 0;
int lay3State = 0;

void setup() {
  BTSerial.begin(9600);
  Serial.begin(9600);

  pinMode(relay1, OUTPUT);
  pinMode(relay2, OUTPUT);
  pinMode(relay3, OUTPUT);
  pinMode(relay4, OUTPUT);
}


void loop()
{

  if (BTSerial.available()) {
    byte data  =  BTSerial.read();

    Serial.write(data);

    if (-128 <= data < -40) {
      if (lay1State == 0) { // 릴레이1이 꺼져있을 경우
        digitalWrite(relay1, HIGH);
        lay1State = 1; // 릴레이1 상태를 켜진 상태로 세팅
        delay(150);
      }
      else { // 릴레이1이 켜져있을 경우
        digitalWrite(relay1, LOW);
        lay1State = 0;
        delay(150);
      }
    }

    if (-40 <= data < 40) {
      if (lay2State == 0) { // 릴레이2이 꺼져있을 경우
        digitalWrite(relay2, HIGH);
        lay2State = 1; // 릴레이2 상태를 켜진 상태로 세팅
        delay(150);
      }
      else { // 릴레이2이 켜져있을 경우
        digitalWrite(relay2, LOW);
        lay2State = 0;
        delay(150);
      }
    }

    if (data >= 40) {
      if (lay3State == 0) { // 릴레이3이 꺼져있을 경우
        digitalWrite(relay3, HIGH);
        lay3State = 1; // 릴레이3 상태를 켜진 상태로 세팅
        delay(150);
      }
      else { // 릴레이3이 켜져있을 경우
        digitalWrite(relay3, LOW);
        lay3State = 0;
        delay(150);
      }
    }

  }

}

