int latch = 2;
int clock = 3;
int data = 4;


void setup() {

  pinMode(latch, OUTPUT);
  pinMode(clock, OUTPUT);
  pinMode(data, OUTPUT);

  Serial.begin(9600);

}


//byte arr[6] = {0b1111,  0b1010, 0b0000, 0b0101, 0b1111, 0b0000};
//byte arr2[6] = {0b0000,  0b1010, 0b1111, 0b0101, 0b1111, 0b0000};

byte aaa[3] = {0b10101010, 0b01010101, 0b10101010};
//byte bbb[3] = {0b00000000, 0b11111111, 0b00000000};

void loop()
{
  for (int i = 0; i < 3; i++)
  {
    digitalWrite(latch, LOW);
    Serial.println(i);
    shiftOut(data, clock, MSBFIRST, aaa[i]);
    Serial.println(aaa[i]);
  //  shiftOut(data, clock, MSBFIRST, bbb[i]);
  //  Serial.println(bbb[i]);
    digitalWrite(latch, HIGH);
    Serial.println();
    delay(5000);
  }

}




