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
      case 0 ... 3 :
        digitalWrite(relay1, LOW);
        delay(150);
        digitalWrite(relay1, HIGH);
        break;

      case 4 ... 6 :
        digitalWrite(relay2, LOW);
        delay(150);
        digitalWrite(relay2, HIGH);
        break;

      case 7 ... 9 :
        digitalWrite(relay3, LOW);
        delay(150);
        digitalWrite(relay3, HIGH);
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
  }    //연속적으로 릴레이는 켜지나, 배열받는것은 아직
}
