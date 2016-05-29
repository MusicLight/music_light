const int relaypin = 2; // 핀을 전역변수로 설정


void setup() {

  pinMode(relaypin, OUTPUT); // 핀 아웃풋 설정

  Serial.begin(9600); // 시리얼 초기화 함수

  Serial.println("arduino ready"); // 셋업함수가 끝나면 바로 문자열 보냄

}



void loop() {

  char serialdata; // 시리얼 값이 8비트 (1바이트임) 으로 char 형 변수 선언

  if (Serial.available() > 0) serialdata = Serial.read();

  // 내장된 시리얼 함수를 통해 데이터가 오면 serialdata 에 값 저장

  if (serialdata == '1') { // 시리얼 입력 값이 1이면

    digitalWrite(relaypin, HIGH); // 2번핀에 HIGH 신호 보냄

    Serial.println("relay power on"); // 시리얼로 켰다고 신호 보냄

  }

  else if (serialdata == '0') { // 시리얼 입력 값이 0 이면

    digitalWrite(relaypin, LOW); // 2번핀에 LOW 신호 보냄

    Serial.println("relay power off"); // 시리얼로 껐다고 신호 보냄

  }

}
