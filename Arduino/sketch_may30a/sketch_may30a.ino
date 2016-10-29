#include <SoftwareSerial.h>

SoftwareSerial BTSerial(8, 9); // SoftwareSerial(RX, TX)

int latch = 2;
int clock = 3;
int data = 4;

byte a, b, c, d, e, f, g, h, i, j, k, l;

int LEDarr[16] = {0b00010001, 0b00010010, 0b00010100, 0b00011000,
                  0b00100001, 0b00100010, 0b00100100, 0b00101000,
                  0b01000001, 0b01000010, 0b01000100, 0b01001000,
                  0b10000001, 0b10000010, 0b10000100, 0b10001000
                 };

/*int pull[16] = {0b00010001, 0b00010011, 0b00010111, 0b00011111,
                 0b00110001, 0b00110011, 0b00110111, 0b00111111,
                 0b01110001, 0b01110011, 0b01110111, 0b01111111,
                 0b11110001, 0b11110011, 0b11110111, 0b11111111
                };  밑에꺼까지다켜지는것실험할경우 */


void setup() {
  BTSerial.begin(9600);
  Serial.begin(9600);

  pinMode(latch, OUTPUT);
  pinMode(clock, OUTPUT);
  pinMode(data, OUTPUT);

  digitalWrite(latch, LOW);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  digitalWrite(latch, HIGH);

}

void trans();

byte array[15];

void loop()
{
  while (BTSerial.available()) {
    char data = (char)BTSerial.read();
    if (data != '/')
    {
      if (data == 'A')
      {
        byte a = BTSerial.read();
        array[0] = a;

        Serial.print("A : ");
        Serial.println(array[0]);
      }
      else if (data == 'B')
      {
        byte b = BTSerial.read();
        array[1] = b;
        Serial.print("B : ");
        Serial.println(array[1]);
      }
      else if (data == 'C')
      {
        byte c = BTSerial.read();
        array[2] = c;
        Serial.print("C : ");
        Serial.println(array[2]);
      }
      else if (data == 'D')
      {
        byte d = BTSerial.read();
        array[3] = d;
        Serial.print("D : ");
        Serial.println(array[3]);
      }
      else if (data == 'E')
      {
        byte e = BTSerial.read();
        array[4] = e;
        Serial.print("E : ");
        Serial.println(array[4]);
      }
      else if (data == 'F')
      {
        byte f = BTSerial.read();
        array[5] = f;
        Serial.print("F : ");
        Serial.println(array[5]);
      }
      else if (data == 'G')
      {
        byte g = BTSerial.read();
        array[6] = g;
        Serial.print("G : ");
        Serial.println(array[6]);
      }
      else if (data == 'H')
      {
        byte h = BTSerial.read();
        array[7] = h;
        Serial.print("H : ");
        Serial.println(array[7]);
      }
      else if (data == 'I')
      {
        byte i = BTSerial.read();
        array[8] = i;
        Serial.print("I : ");
        Serial.println(array[8]);
      }
      else if (data == 'J')
      {
        byte j = BTSerial.read();
        array[9] = j;
        Serial.print("J : ");
        Serial.println(array[9]);
      }
      else if (data == 'K')
      {
        byte k = BTSerial.read();
        array[10] = k;
        Serial.print("K : ");
        Serial.println(array[10]);
      }
      else if (data == 'L')
      {
        byte l = BTSerial.read();
        array[11] = l;
        Serial.print("L : ");
        Serial.println(array[11]);
      }
    }


    else
    {
      trans();
      byte a = 0, b = 0, c = 0, d = 0, e = 0, f = 0, g = 0, h = 0, i = 0, j = 0, k = 0, l = 0;
      array[15] = {0,};

    }
  }
}




void trans() {

  /**************** 레지스터 1번 ****************/
  switch (array[0])
  {
    case 0 ... 7 :
      {
        if (array[1] < 8)
        {
          int ab = LEDarr[0];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/1/B/1 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else if ( array[1] >= 8 && array[1] < 11)
        {
          int ab = LEDarr[1];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/1/B/2 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }

        else if ( array[1] >= 11 && array[1] < 14)
        {
          int ab = LEDarr[2];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/1/B/3 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else //if ( array[1] > 191 && array[1] <= 255)
        {
          int ab = LEDarr[3];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/1/B/4 : ");
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
          int ab = LEDarr[4];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/2/B/1 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else if ( array[1] >= 8 && array[1] < 11)
        {
          int ab = LEDarr[5];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/2/B/2 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }

        else if ( array[1] >= 11 && array[1] < 14)
        {
          int ab = LEDarr[6];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/2/B/3 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else //if ( array[1] > 191 && array[1] <= 255)
        {
          int ab = LEDarr[7];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/2/B/4 : ");
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
          int ab = LEDarr[8];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/3/B/1 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else if ( array[1] >= 8 && array[1] < 11)
        {
          int ab = LEDarr[9];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/3/B/2  : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }

        else if ( array[1] >= 11 && array[1] < 14)
        {
          int ab = LEDarr[10];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/3/B/3  : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else //if ( array[1] > 191 && array[1] <= 255)
        {
          int ab = LEDarr[11];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/3/B/3  : ");
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
          int ab = LEDarr[12];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/4/B/1  : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else if ( array[1] >= 8 && array[1] < 11)
        {
          int ab = LEDarr[13];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/4/B/2 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }

        else if ( array[1] >= 11 && array[1] < 14)
        {
          int ab = LEDarr[14];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/4/B/3 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        else //if ( array[1] > 191 && array[1] <= 255)
        {
          int ab = LEDarr[15];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ab);
          Serial.println("A/4/B/4 : ");
          Serial.println(array[0]);
          Serial.println(array[1]);
        }
        array[0] = {0,};
        array[1] = {0,};
      }
      break;
  }

  /**************** 레지스터 2번 ****************/
  switch (array[2])
  {
    case 0 ... 7 :
      {
        if (array[3] < 8)
        {
          int cd = LEDarr[0];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/1/D/1 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else if ( array[3] >= 8 && array[3] < 11)
        {
          int cd = LEDarr[1];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/1/D/2 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }

        else if ( array[3] >= 11 && array[3] < 14)
        {
          int cd = LEDarr[2];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/1/D/3 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else //if ( array[3] > 191 && array[3] <= 255)
        {
          int cd = LEDarr[3];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/1/D/4 : ");
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
          int cd = LEDarr[4];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/2/D/1 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else if ( array[3] >= 8 && array[3] < 11)
        {
          int cd = LEDarr[5];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/2/D/2  : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }

        else if ( array[3] >= 11 && array[3] < 14)
        {
          int cd = LEDarr[6];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/2/D/3  : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else //if ( array[3] > 191 && array[3] <= 255)
        {
          int cd = LEDarr[7];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/2/D/4  : ");
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
          int cd = LEDarr[8];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/3/D/1  : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else if ( array[3] >= 8 && array[3] < 11)
        {
          int cd = LEDarr[9];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/3/D/2 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }

        else if ( array[3] >= 11 && array[3] < 14)
        {
          int cd = LEDarr[10];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/3/D/3 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else //if ( array[3] > 191 && array[3] <= 255)
        {
          int cd = LEDarr[11];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/3/D/4: ");
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
          int cd = LEDarr[12];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/4/D/1  : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else if ( array[3] >= 8  && array[3] < 11)
        {
          int cd = LEDarr[13];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/4/D/2  : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }

        else if ( array[3] >= 11 && array[3] < 14)
        {
          int cd = LEDarr[14];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/4/D/3 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else //if ( array[3] > 191 && array[3] <= 255)
        {
          int cd = LEDarr[15];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("C/4/D/4 : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        array[2] = {0,};
        array[3] = {0,};
      }
      break;
  }

  /**************** 레지스터 3번 ****************/
  switch (array[4])
  {
    case 0 ... 7 :
      {
        if (array[5] < 8)
        {
          int ef = LEDarr[0];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, cd);
          Serial.println("E/1/F/1 : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }
        else if ( array[5] >= 8 && array[5] < 11)
        {
          int ef = LEDarr[1];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/1/F/2 : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }

        else if ( array[5] >= 11 && array[5] < 14)
        {
          int ef = LEDarr[2];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/1/F/3 : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }
        else //if ( array[5] > 191 && array[5] <= 255)
        {
          int ef = LEDarr[3];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/1/F/4 : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }
        array[4] = {0,};
        array[5] = {0,};
      }
      break;

    case 8 ... 10 :
      {
        if (array[5] < 8)
        {
          int ef = LEDarr[4];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/2/F/1 : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }
        else if ( array[5] >= 8 && array[5] < 11)
        {
          int ef = LEDarr[5];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/2/F/2 : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }

        else if ( array[5] >= 11 && array[5] < 14)
        {
          int ef = LEDarr[6];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/2/F/3 : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }
        else //if ( array[5] > 191 && array[5] <= 255)
        {
          int ef = LEDarr[7];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/2/F/4 : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }
        array[4] = {0,};
        array[5] = {0,};
      }
      break;

    case 11 ... 13 :
      {
        if (array[5] < 8)
        {
          int ef = LEDarr[8];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/3/F/1  : ");
          Serial.println(array[2]);
          Serial.println(array[3]);
        }
        else if ( array[5] >= 8 && array[5] < 11)
        {
          int ef = LEDarr[9];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/3/F/2 : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }

        else if ( array[5] >= 11 && array[5] < 14)
        {
          int ef = LEDarr[10];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/3/F/3 : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }
        else //if ( array[3] > 191 && array[3] <= 255)
        {
          int ef = LEDarr[11];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/3/F/4: ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }
        array[4] = {0,};
        array[5] = {0,};
      }
      break;

    case 14 ... 30 :
      {
        if (array[5] < 8)
        {
          int ef = LEDarr[12];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/4/F/1  : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }
        else if ( array[5] >= 8  && array[5] < 11)
        {
          int ef = LEDarr[13];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/4/F/2  : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }

        else if ( array[5] >= 11 && array[5] < 14)
        {
          int ef = LEDarr[14];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/4/F/3 : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }
        else //if ( array[5] > 191 && array[5] <= 255)
        {
          int ef = LEDarr[15];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ef);
          Serial.println("E/4/F/4 : ");
          Serial.println(array[4]);
          Serial.println(array[5]);
        }
        array[4] = {0,};
        array[5] = {0,};
      }
      break;
  }

  /**************** 레지스터 4번 ****************/
  switch (array[6])
  {
    case 0 ... 7 :
      {
        if (array[7] < 8)
        {
          int gh = LEDarr[0];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/1/H/1 : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }
        else if ( array[7] >= 8 && array[7] < 11)
        {
          int gh = LEDarr[1];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/1/H/2 : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }

        else if ( array[7] >= 11 && array[7] < 14)
        {
          int gh = LEDarr[2];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/1/H/3 : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }
        else //if ( array[5] > 191 && array[5] <= 255)
        {
          int gh = LEDarr[3];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/1/H/4 : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }
        array[6] = {0,};
        array[7] = {0,};
      }
      break;

    case 8 ... 10 :
      {
        if (array[7] < 8)
        {
          int gh = LEDarr[4];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/2/H/1 : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }
        else if ( array[7] >= 8 && array[7] < 11)
        {
          int gh = LEDarr[5];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/2/H/2 : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }

        else if ( array[7] >= 11 && array[7] < 14)
        {
          int gh = LEDarr[6];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/2/H/3 : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }
        else //if ( array[7] > 191 && array[7] <= 255)
        {
          int gh = LEDarr[7];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/2/H/4 : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }
        array[6] = {0,};
        array[7] = {0,};
      }
      break;

    case 11 ... 13 :
      {
        if (array[7] < 8)
        {
          int gh = LEDarr[8];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/3/H/1  : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }
        else if ( array[7] >= 8 && array[7] < 11)
        {
          int gh = LEDarr[9];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/3/H/2 : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }

        else if ( array[7] >= 11 && array[7] < 14)
        {
          int gh = LEDarr[10];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/3/H/3 : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }
        else //if ( array[3] > 191 && array[3] <= 255)
        {
          int gh = LEDarr[11];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/3/H/4: ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }
        array[6] = {0,};
        array[7] = {0,};
      }
      break;

    case 14 ... 30 :
      {
        if (array[7] < 8)
        {
          int gh = LEDarr[12];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/4/H/1  : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }
        else if ( array[7] >= 8  && array[7] < 11)
        {
          int gh = LEDarr[13];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/4/H/2  : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }

        else if ( array[7] >= 11 && array[7] < 14)
        {
          int gh = LEDarr[14];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/4/H/3 : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }
        else //if ( array[5] > 191 && array[5] <= 255)
        {
          int gh = LEDarr[15];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, gh);
          Serial.println("G/4/H/4 : ");
          Serial.println(array[6]);
          Serial.println(array[7]);
        }
        array[6] = {0,};
        array[7] = {0,};
      }
      break;
  }

  /**************** 레지스터 5번 ****************/
  switch (array[8])
  {
    case 0 ... 7 :
      {
        if (array[9] < 8)
        {
          int ij = LEDarr[0];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/1/J/1 : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }
        else if ( array[9] >= 8 && array[9] < 11)
        {
          int ij = LEDarr[1];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/1/J/2 : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }

        else if ( array[9] >= 11 && array[9] < 14)
        {
          int ij = LEDarr[2];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/1/J/3 : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }
        else //if ( array[5] > 191 && array[5] <= 255)
        {
          int ij = LEDarr[3];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/1/J/4 : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }
        array[8] = {0,};
        array[9] = {0,};
      }
      break;

    case 8 ... 10 :
      {
        if (array[9] < 8)
        {
          int ij = LEDarr[4];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/2/J/1 : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }
        else if ( array[9] >= 8 && array[9] < 11)
        {
          int ij = LEDarr[5];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/2/J/2 : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }

        else if ( array[9] >= 11 && array[9] < 14)
        {
          int ij = LEDarr[6];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/2/J/3 : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }
        else //if ( array[5] > 191 && array[5] <= 255)
        {
          int ij = LEDarr[7];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/2/J/4 : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }
        array[8] = {0,};
        array[9] = {0,};
      }
      break;

    case 11 ... 13 :
      {
        if (array[9] < 8)
        {
          int ij = LEDarr[8];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/3/J/1  : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }
        else if ( array[9] >= 8 && array[9] < 11)
        {
          int ij = LEDarr[9];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/3/J/2 : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }

        else if ( array[9] >= 11 && array[9] < 14)
        {
          int ij = LEDarr[10];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/3/J/3 : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }
        else //if ( array[3] > 191 && array[3] <= 255)
        {
          int ij = LEDarr[11];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/3/J/4: ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }
        array[8] = {0,};
        array[9] = {0,};
      }
      break;

    case 14 ... 30 :
      {
        if (array[9] < 8)
        {
          int ij = LEDarr[12];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/4/J/1  : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }
        else if ( array[9] >= 8  && array[9] < 11)
        {
          int ij = LEDarr[13];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/4/J/2  : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }

        else if ( array[9] >= 11 && array[9] < 14)
        {
          int ij = LEDarr[14];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/4/J/3 : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }
        else //if ( array[5] > 191 && array[5] <= 255)
        {
          int ij = LEDarr[15];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, ij);
          Serial.println("I/4/J/4 : ");
          Serial.println(array[8]);
          Serial.println(array[9]);
        }
        array[8] = {0,};
        array[9] = {0,};
      }
      break;
  }

  /**************** 레지스터 6번 ****************/
  switch (array[10])
  {
    case 0 ... 7 :
      {
        if (array[11] < 8)
        {
          int kl = LEDarr[0];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/1/L/1 : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }
        else if ( array[11] >= 8 && array[11] < 11)
        {
          int kl = LEDarr[1];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/1/L/2 : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }

        else if ( array[11] >= 11 && array[11] < 14)
        {
          int kl = LEDarr[2];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/1/L/3 : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }
        else //if ( array[5] > 191 && array[5] <= 255)
        {
          int kl = LEDarr[3];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/1/L/4 : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }
        array[10] = {0,};
        array[11] = {0,};
      }
      break;

    case 8 ... 10 :
      {
        if (array[11] < 8)
        {
          int kl = LEDarr[4];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/2/L/1 : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }
        else if ( array[11] >= 8 && array[11] < 11)
        {
          int kl = LEDarr[5];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/2/L/2 : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }

        else if ( array[11] >= 11 && array[11] < 14)
        {
          int kl = LEDarr[6];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/2/L/3 : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }
        else //if ( array[5] > 191 && array[5] <= 255)
        {
          int kl = LEDarr[7];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/2/L/4 : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }
        array[10] = {0,};
        array[11] = {0,};
      }
      break;

    case 11 ... 13 :
      {
        if (array[11] < 8)
        {
          int kl = LEDarr[8];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/3/L/1  : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }
        else if ( array[11] >= 8 && array[11] < 11)
        {
          int kl = LEDarr[9];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/3/L/2 : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }

        else if ( array[11] >= 11 && array[11] < 14)
        {
          int kl = LEDarr[10];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/3/L/3 : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }
        else //if ( array[3] > 191 && array[3] <= 255)
        {
          int kl = LEDarr[11];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/3/L/4: ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }
        array[10] = {0,};
        array[11] = {0,};
      }
      break;

    case 14 ... 30 :
      {
        if (array[11] < 8)
        {
          int kl = LEDarr[12];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/4/L/1  : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }
        else if ( array[11] >= 8  && array[11] < 11)
        {
          int kl = LEDarr[13];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/4/L/2  : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }

        else if ( array[11] >= 11 && array[11] < 14)
        {
          int kl = LEDarr[14];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/4/L/3 : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }
        else //if ( array[5] > 191 && array[5] <= 255)
        {
          int kl = LEDarr[15];
          digitalWrite(latch, LOW);
          shiftOut(data, clock, LSBFIRST, kl);
          Serial.println("K/4/L/4 : ");
          Serial.println(array[10]);
          Serial.println(array[11]);
        }
        array[10] = {0,};
        array[11] = {0,};
      }
      break;
  }

  digitalWrite(latch, HIGH);
  delay(500);

  digitalWrite(latch, LOW);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  digitalWrite(latch, HIGH);
  delay(10);

  Serial.println("\\\\\\\\");
  int ab = 0, cd = 0, ef = 0, gh = 0, ij = 0, kl = 0;
  int array[15] = {0,};
}

