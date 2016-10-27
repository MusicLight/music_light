#include <SoftwareSerial.h>

SoftwareSerial BTSerial(8, 9); // SoftwareSerial(RX, TX)

int latch = 2;
int clock = 3;
int data = 4;

byte c, g, j, l;

int ABarr[16] = {0b00010001, 0b00010010, 0b00010100, 0b00011000,
                 0b00100001, 0b00100010, 0b00100100, 0b00101000,
                 0b01000001, 0b01000010, 0b01000100, 0b01001000,
                 0b10000001, 0b10000010, 0b10000100, 0b10001000
                };


void setup() {
  BTSerial.begin(9600);
  Serial.begin(9600);

  pinMode(latch, OUTPUT);
  pinMode(clock, OUTPUT);
  pinMode(data, OUTPUT);

  digitalWrite(latch, LOW);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  digitalWrite(latch, HIGH);

}

//void ShiftLed();
void trans();

byte array[100];

void loop()
{
  while (BTSerial.available()) {
    char data = (char)BTSerial.read();
    if (data != '/')
    {
      if (data == 'C')
      {
        byte c = BTSerial.read();
        array[0] = c;

        Serial.print("C : ");
        Serial.println(array[0]);
      }
      else if (data == 'G')
      {
        byte g = BTSerial.read();
        array[1] = g;
        Serial.print("G : ");
        Serial.println(array[1]);
      }
      else if (data == 'J')
      {
        byte j = BTSerial.read();
        array[2] = j;
        Serial.print("J : ");
        Serial.println(array[2]);
      }
      else if (data == 'L')
      {
        byte l = BTSerial.read();
        array[3] = l;
        Serial.print("L : ");
        Serial.println(array[3]);
      }
    }


    else
    {
      trans();
      //ShiftLed();
      byte c = 0, g = 0, j = 0, l = 0;
      array[4] = {0,};

    }
  }
}




void trans() {
  switch (array[0])
  {
    case 0 ... 7 :
      {
        if (array[1] < 8)
        {
          int ab = ABarr[0];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/1/1 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else if ( array[1] >= 8 && array[1] < 11)
        {
          int ab = ABarr[1];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/1/2 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }

        else if ( array[1] >= 11 && array[1] < 14)
        {
          int ab = ABarr[2];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/1/3 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else //if ( array[1] > 191 && array[1] <= 255)
        {
          int ab = ABarr[3];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/1/4 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        array[0] = {0,};
        array[1] = {0,};
      }

      break;

    case  8 ... 10 :
      {
        if (array[1] < 8)
        {
          int ab = ABarr[4];  digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/2/1 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else if ( array[1] >= 8 && array[1] < 11)
        {
          int ab = ABarr[5];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/2/2 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }

        else if ( array[1] >= 11 && array[1] < 14)
        {
          int ab = ABarr[6];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/2/3 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else //if ( array[1] > 191 && array[1] <= 255)
        {
          int ab = ABarr[7];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/2/4 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        array[0] = {0,};
        array[1] = {0,};
      }
      break;

    case 11 ... 13 :
      {
        if (array[1] < 8)
        {
          int ab = ABarr[8];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/3/1 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else if ( array[1] >= 8 && array[1] < 11)
        {
          int ab = ABarr[9];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/3/2  : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }

        else if ( array[1] >= 11 && array[1] < 14)
        {
          int ab = ABarr[10];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/3/3  : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else //if ( array[1] > 191 && array[1] <= 255)
        {
          int ab = ABarr[11];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/3/3  : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        array[0] = {0,};
        array[1] = {0,};
      }
      break;

    case 14 ... 30 :
      {
        if (array[1] < 8)
        {
          int ab = ABarr[12];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/4/1  : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else if ( array[1] >= 8 && array[1] < 11)
        {
          int ab = ABarr[13];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/4/2 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }

        else if ( array[1] >= 11 && array[1] < 14)
        {
          int ab = ABarr[14];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/4/3 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else //if ( array[1] > 191 && array[1] <= 255)
        {
          int ab = ABarr[15];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/4/4 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        array[0] = {0,};
        array[1] = {0,};
      }
      break;
  }

  switch (array[2])
  {
    case 0 ... 7 :
      {
        if (array[3] < 8)
        {
          int cd = ABarr[0];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/1/1 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else if ( array[3] >= 8 && array[3] < 11)
        {
          int cd = ABarr[1];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/1/2 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }

        else if ( array[3] >= 11 && array[3] < 14)
        {
          int cd = ABarr[2];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/1/31 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else //if ( array[3] > 191 && array[3] <= 255)
        {
          int cd = ABarr[3];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/1/4 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        array[2] = {0,};
        array[3] = {0,};
      }
      break;

    case 8 ... 10 :
      {
        if (array[3] < 8)
        {
          int cd = ABarr[4];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/2/1 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else if ( array[3] >= 8 && array[3] < 11)
        {
          int cd = ABarr[5];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/2/2  : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }

        else if ( array[3] >= 11 && array[3] < 14)
        {
          int cd = ABarr[6];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/2/3  : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else //if ( array[3] > 191 && array[3] <= 255)
        {
          int cd = ABarr[7];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/2/4  : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        array[2] = {0,};
        array[3] = {0,};
      }
      break;

    case 11 ... 13 :
      {
        if (array[3] < 8)
        {
          int cd = ABarr[8];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/3/1  : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else if ( array[3] >= 8 && array[3] < 11)
        {
          int cd = ABarr[9];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/3/2 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }

        else if ( array[3] >= 11 && array[3] < 14)
        {
          int cd = ABarr[10];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/3/3 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else //if ( array[3] > 191 && array[3] <= 255)
        {
          int cd = ABarr[11];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/3/4: ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        array[2] = {0,};
        array[3] = {0,};
      }
      break;

    case 14 ... 30 :
      {
        if (array[3] < 8)
        {
          int cd = ABarr[12];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/4/1  : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else if ( array[3] >= 8  && array[3] < 11)
        {
          int cd = ABarr[13];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/4/2  : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }

        else if ( array[3] >= 11 && array[3] < 14)
        {
          int cd = ABarr[14];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/4/3 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else //if ( array[3] > 191 && array[3] <= 255)
        {
          int cd = ABarr[15];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/4/4 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        array[2] = {0,};
        array[3] = {0,};
      }
      break;
  }
  digitalWrite(latch, HIGH);
  delay(500);

  digitalWrite(latch, LOW);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  digitalWrite(latch, HIGH);
  delay(10);

  Serial.println("\\\\\\\\");
  int ab = 0, cd = 0;
  int array[4] = {0,};
}


/*void ShiftLed() {

  digitalWrite(latch, LOW);
  shiftOut(data, clock, LSBFIRST, ab);
  shiftOut(data, clock, LSBFIRST, cd);
  digitalWrite(latch, HIGH);
  delay(3000);

  digitalWrite(latch, LOW);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  digitalWrite(latch, HIGH);

  Serial.println("\\\\\\\\");
  //byte ab = 0, cd = 0;
  }
*/
