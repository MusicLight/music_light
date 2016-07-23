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

char buffer[1000];
char bufferIndex = 0;

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
  if (BTSerial.available()) {
    //int data  =  BTSerial.read();
    buffer[bufferIndex] = BTSerial.read();
    //bufferIndex++;
    int data = atoi(buffer);

    Serial.print(data);

    switch (data) {
      case 0 ... 30 :
        digitalWrite(relay1, LOW);
        delay(150);
        digitalWrite(relay1, HIGH);
        delay(150);
        break;

      case 31 ... 60 :
        digitalWrite(relay2, LOW);
        delay(150);
        digitalWrite(relay2, HIGH);
        delay(150);
        break;

      case 61 ... 900 :
        digitalWrite(relay3, LOW);
        delay(150);
        digitalWrite(relay3, HIGH);
        delay(150);
        break;

    }


    /*   if (0 <= data < 3) {
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

          if (3 <= data < 5) {
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

          if (data >= 5) {
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
          } */
  }    // 배열을 받긴 하지만 배열안에 있는 값을 못읽는상태 ( 다 0으로 읽음 )
}
