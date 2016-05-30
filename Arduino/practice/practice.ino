int relay1 = 2;
int relay2 = 3;
int relay3 = 4;
int relay4 = 5;


void setup() {
  Serial.begin(9600);

  pinMode(relay1, OUTPUT);
  pinMode(relay2, OUTPUT);
  pinMode(relay3, OUTPUT);
  pinMode(relay4, OUTPUT);

  delay(5000);
}


void loop() {
  Serial.println("HIGH");
  digitalWrite(relay1, HIGH); // HIGH인가
  delay(2000);

  Serial.println("LOW");
  digitalWrite(relay1, LOW); // LOW인가
  delay(2000);

  Serial.println("HIGH");
  digitalWrite(relay2, HIGH); // HIGH인가
  delay(2000);

  Serial.println("LOW");
  digitalWrite(relay2, LOW); // LOW인가
  delay(2000);
}
