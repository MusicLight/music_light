#include <SoftwareSerial.h>

SoftwareSerial BTSerial(8, 9); // SoftwareSerial(RX, TX)

int latch = 2;
int clock = 3;
int data = 4;

byte a , b , c , d ;
byte ab, cd ;

int ABarr[] = {0b00010001, 0b00010010, 0b00010100, 0b00011000,
               0b00100001, 0b00100010, 0b00100100, 0b00101000,
               0b01000001, 0b01000010, 0b01000100, 0b01001000,
               0b10000001, 0b10000010, 0b10000100, 0b10001000
              };

int CDarr[] = {0b00010001, 0b00010010, 0b00010100, 0b00011000,
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

void ShiftLed();
void trans();


void loop()
{
  while (BTSerial.available()) {
    char data = (char)BTSerial.read();

    if (data != '/')
    {
      byte a = 0, b = 0, c = 0, d = 0;
      if (data == 'A')
      {
        byte a = BTSerial.read();
        Serial.print("A : ");
        Serial.println(a);
      }
      else if (data == 'B')
      {
        byte b = BTSerial.read();
        Serial.print("B : ");
        Serial.println(b);
      }
      else if (data == 'C')
      {
        byte c = BTSerial.read();
        Serial.print("C : ");
        Serial.println(c);
      }
      else if (data == 'D')
      {
        byte d = BTSerial.read();
        Serial.print("D : ");
        Serial.println(d);
      }
    }


    else
    {
      byte cc = c;
      Serial.print("c는몇이니 : ");
      Serial.println(cc);
      if (cc <= 15)
      { //cd = CDarr[0]; // 17
        digitalWrite(latch, LOW);
        shiftOut(data, clock, LSBFIRST, 1);
        digitalWrite(latch, HIGH);
      } else if ( cc > 15 && cc <= 30)
      { //cd = CDarr[2]; // 18
        digitalWrite(latch, LOW);
        shiftOut(data, clock, LSBFIRST, 2);
        digitalWrite(latch, HIGH);
      } else if ( cc > 30 && cc <= 45)
      { //cd = CDarr[3]; // 20
        digitalWrite(latch, LOW);
        shiftOut(data, clock, LSBFIRST, 4);
        digitalWrite(latch, HIGH);
      } else
      { //cd = CDarr[4]; // 24
        digitalWrite(latch, LOW);
        shiftOut(data, clock, LSBFIRST, 8);
        digitalWrite(latch, HIGH);
      }

      ShiftLed();
      delay(500);
    }

    /*   {
         Serial.print("AB : ");
         Serial.println(ab);
         Serial.print("CD : ");
         Serial.println(cd);
         digitalWrite(latch, LOW);
         shiftOut(data, clock, MSBFIRST, ab);
         shiftOut(data, clock, MSBFIRST, cd);
         digitalWrite(latch, HIGH);
         Serial.println("\\\\\\\\");
         delay(500);
       }*/
  }
}

void trans() {
  /* {
     {
       if (a <= 15)
       {
         if (b <= 15)
         {
           ab = ABarr[0]; // 17
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         }
         else if ( b > 15 && b <= 30)
         {
           ab = ABarr[1]; // 18
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         }
         else if ( b > 30 && b <= 45)
         { ab = ABarr[2]; // 20
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         }
         else
         { ab = ABarr[3]; // 24
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         }
       }
       else if ( a > 15 && a <= 30)
       {
         if (b <= 15)
         { ab = ABarr[4]; // 33
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         }
         else if ( b > 15 && b <= 30)
         { ab = ABarr[5]; // 34
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         } else if ( b > 30 && b <= 45)
         { ab = ABarr[6]; // 36
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         } else
         { ab = ABarr[7]; // 40
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         }
       }
       else if ( a > 30 && a <= 45 )
       {
         if (b <= 15)
         { ab = ABarr[8]; // 65
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         } else if ( b > 15 && b <= 30)
         { ab = ABarr[9]; // 66
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         } else if ( b > 30 && b <= 45)
         { ab = ABarr[10]; // 68
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         } else
         { ab = ABarr[11]; // 72
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         }
       }
       else
       {
         if (b <= 15)
         { ab = ABarr[12]; // 129
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         } else if ( b > 15 && b <= 30)
         { ab = ABarr[13]; // 130
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         } else if ( b > 30 && b <= 45)
         { ab = ABarr[14]; // 132
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         } else
         { ab = ABarr[15]; // 136
           digitalWrite(latch, LOW);
           shiftOut(data, clock, LSBFIRST, ab);
           digitalWrite(latch, HIGH);
         }
       }
     }
     Serial.print("ab : ");
     Serial.println(ab);
    }*/
  {

    /* else if ( c > 15 && c <= 30)
      {
       if (d <= 15)
       { cd = CDarr[5]; // 33
         digitalWrite(latch, LOW);
         shiftOut(data, clock, LSBFIRST, cd);
         digitalWrite(latch, HIGH);
       } else if ( d > 15 && d <= 30)
       { cd = CDarr[6]; // 34
         digitalWrite(latch, LOW);
         shiftOut(data, clock, LSBFIRST, cd);
         digitalWrite(latch, HIGH);
       } else if ( d > 30 && d <= 45)
       { cd = CDarr[7]; // 36
         digitalWrite(latch, LOW);
         shiftOut(data, clock, LSBFIRST, cd);
         digitalWrite(latch, HIGH);
       } else
       { cd = CDarr[8]; // 40
         digitalWrite(latch, LOW);
         shiftOut(data, clock, LSBFIRST, cd);
         digitalWrite(latch, HIGH);
       }
      }
      else if ( c > 30 && c <= 45 )
      {
       if (d <= 15)
       { cd = CDarr[9]; // 65
         digitalWrite(latch, LOW);
         shiftOut(data, clock, LSBFIRST, cd);
         digitalWrite(latch, HIGH);
       } else if ( d > 15 && d <= 30)
       { cd = CDarr[10]; // 66
         digitalWrite(latch, LOW);
         shiftOut(data, clock, LSBFIRST, cd);
         digitalWrite(latch, HIGH);
       } else if ( d > 30 && d <= 45)
       { cd = CDarr[11]; // 68
         digitalWrite(latch, LOW);
         shiftOut(data, clock, LSBFIRST, cd);
         digitalWrite(latch, HIGH);
       } else
       { cd = CDarr[12]; // 72
         digitalWrite(latch, LOW);
         shiftOut(data, clock, LSBFIRST, cd);
         digitalWrite(latch, HIGH);
       }
      }
      else
      {
       if (d <= 15)
       { cd = CDarr[13]; // 129
         digitalWrite(latch, LOW);
         shiftOut(data, clock, LSBFIRST, cd);
         digitalWrite(latch, HIGH);
       } else if ( d > 15 && d <= 30)
       { cd = CDarr[14]; // 130
         digitalWrite(latch, LOW);
         shiftOut(data, clock, LSBFIRST, cd);
         digitalWrite(latch, HIGH);
       } else if ( d > 30 && d <= 45)
       { cd = CDarr[15]; // 132
         digitalWrite(latch, LOW);
         shiftOut(data, clock, LSBFIRST, cd);
         digitalWrite(latch, HIGH);
       } else
       { cd = CDarr[16]; // 136
         digitalWrite(latch, LOW);
         shiftOut(data, clock, LSBFIRST, cd);
         digitalWrite(latch, HIGH);
       }
      }
      }
      Serial.print("cd : ");
      Serial.println(cd);
      }*/
  }
}

void ShiftLed() {

  /* digitalWrite(latch, LOW);
    shiftOut(data, clock, LSBFIRST, ab);
    shiftOut(data, clock, LSBFIRST, cd);
    digitalWrite(latch, HIGH);*/

  digitalWrite(latch, LOW);
  shiftOut(data, clock, LSBFIRST, 0x00);
  shiftOut(data, clock, LSBFIRST, 0x00);
  digitalWrite(latch, HIGH);

  Serial.println("\\\\\\\\");
  //byte ab = 0, cd = 0;
}
