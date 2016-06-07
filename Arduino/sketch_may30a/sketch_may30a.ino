#include <SoftwareSerial.h>

SoftwareSerial BTSerial(8, 9); // SoftwareSerial(RX, TX)
byte buffer[1024]; // 데이터를 수신 받을 버퍼
int bufferPosition; // 버퍼에 데이타를 저장할 때 기록할 위치
boolean temp = 0;
int relay1 = 2;
int relay2 = 3;
int relay3 = 4;
int relay4 = 5;


void setup() {
  BTSerial.begin(9600);
  Serial.begin(9600);
  bufferPosition = 0;

  pinMode(relay1, OUTPUT);
  pinMode(relay2, OUTPUT);
  pinMode(relay3, OUTPUT);
  pinMode(relay4, OUTPUT);

}



void loop() {

  if (BTSerial.available()) { // 블루투스로 데이터 수신
    byte data = BTSerial.read(); // 수신 받은 데이터 저장
    Serial.write(data); // 수신된 데이터 시리얼 모니터로 출력
    buffer[bufferPosition++] = data; // 수신 받은 데이터를 버퍼에 저장

    if (data == 'a') { // 블루투스를 통해 'a' 이 들어오면
      if (temp == 0) {
        digitalWrite(relay1, HIGH);
        temp = 1;
      } else {
        digitalWrite(relay1, LOW);
        temp = 0;
      }
    }

    if (data == '\n') { // 문자열 종료 표시
      buffer[bufferPosition] = '\0';
      bufferPosition = 0;
    }
  }

}
