#include <Wire.h>
#include "Adafruit_TCS34725.h"
#include <Adafruit_GFX.h>
#include <LiquidCrystal_I2C.h>
#include <SPI.h>

// RGB SENSOR
Adafruit_TCS34725 tcs = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_50MS, TCS34725_GAIN_4X);

// DISPLAY I2C
LiquidCrystal_I2C lcd(0x27, 16, 2);

// Morse massive for decoding
const char* morseCodes[] = 
{
  ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..",    // A-I
  ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.",  // J-R
  "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..",         // S-Z
  "-----", ".----", "..---", "...--", "....-", ".....",             // 0-5
  "-....", "--...", "---..", "----."                                // 6-9
};

const char morseChars[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

// LEDs thresholds
uint16_t GREEN_THRESHOLD  = 700;
uint16_t RED_THRESHOLD    = 700;
uint16_t BLUE_THRESHOLD   = 700;

// ==========================================================
//                   FASTER NON USER DECODER
// ==========================================================
class MorseDecoderV1 {
  public:
    void begin() {
      lastColorTime = millis();
    }

    void reset() {
      currentSymbol = "";
      decodedMessage = "";
      packedMessage = "";
      lastIsSpace = false;
      position = 0;
      lastColorTime = millis();
    }

    void update() {
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("DECODER OUT:");
      lcd.setCursor(0, 1);
      if (decodedMessage.length() < 16)
      {
        lcd.print(decodedMessage);
      }
      else
      {
        lcd.print(decodedMessage.substring(position, position + 16));
        position++;
        if (position > decodedMessage.length())
        {
          position = 0;
        }
      }

      uint16_t redValue, greenValue, blueValue, clearValue;
      tcs.getRawData(&redValue, &greenValue, &blueValue, &clearValue);

      bool redIsDetected = (redValue > RED_THRESHOLD);
      bool greenIsDetected = (greenValue > GREEN_THRESHOLD);
      bool blueIsDetected = (blueValue > BLUE_THRESHOLD);

      if ((redIsDetected) && (redValue > greenValue) && (redValue > blueValue)) 
      {
        redDetectionHandler();
      }
      else if ((greenIsDetected) && (greenValue > redValue) && (greenValue > blueValue))
      {
        greenDetectionHandler();
      }
      else if ((blueIsDetected) && (blueValue > redValue) && (blueValue > greenValue))
      {
        blueDetectionHandler();
      }

      timeoutHandler();

      delay(125); 
    }

  private:
    String currentSymbol = "";
    String decodedMessage = "";
    String packedMessage = "";
    unsigned long lastColorTime = 0;
    bool lastIsSpace = false;
    unsigned int position = 0;

    const unsigned int COLOR_DELAY = 250;
    const unsigned int COLOR_TIMEOUT = 1000;
    const unsigned int PACKAGE_SIZE = 8;

    void redDetectionHandler() 
    {
      unsigned long currentColorTime = millis();
      if (currentColorTime - lastColorTime < COLOR_DELAY) return;
      lastColorTime = currentColorTime;
      currentSymbol += "-";
      lastIsSpace = false;
    }

    void greenDetectionHandler() 
    {
      unsigned long currentColorTime = millis();
      if (currentColorTime - lastColorTime < COLOR_DELAY) return;
      lastColorTime = currentColorTime;
      currentSymbol += ".";
      lastIsSpace = false;
    }

    void blueDetectionHandler() 
    {
      unsigned long currentColorTime = millis();
      if (currentColorTime - lastColorTime < COLOR_DELAY) return;
      lastColorTime = currentColorTime;

      if (lastIsSpace)
      {
          decodedMessage += " ";
          lastIsSpace = false;
          decodeSymbolHandler(true);
          return;
      }

      if (currentSymbol.length() > 0) 
      {
        decodeSymbolHandler(false);
      }

      lastIsSpace = true;
    }

    void decodeSymbolHandler(bool isSpace)
    {
      if (isSpace) 
      {
        packedMessage += "_";
        decodedMessage += ' ';
        currentSymbol = "";
      }
      else
      {
        char decodedSymbol = '?';
        for (int index = 0; index < 36; index++) 
        {
          if (currentSymbol == morseCodes[index])
          {
            decodedSymbol = morseChars[index];
            break;
          }
        }

        if (decodedSymbol != '?')
        {
          packedMessage += decodedSymbol;
          decodedMessage += decodedSymbol;
        }

        currentSymbol = "";
      }

      if (packedMessage.length() == PACKAGE_SIZE)
      {
        Serial.print(packedMessage);
        packedMessage = "";
      }
    }

    void timeoutHandler()
    {
      unsigned long currentTime = millis();
      if (currentTime - lastColorTime > COLOR_TIMEOUT)
      {
        if (packedMessage.length() == PACKAGE_SIZE)
        {
          Serial.print(packedMessage);
          packedMessage = "";
        }
        lastIsSpace = false;
      }
    }
};

// ===================================================================================
//                              SLOWER USER DECODER
// ===================================================================================
class MorseDecoderV2 {
  public:
    void begin() {
      lastColorTime = millis();
    }

    void reset() {
      currentSymbol = "";
      decodedMessage = "";
      lastIsSpace = false;
      position = 0;
      lastColorTime = millis();
    }

    void update() {
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("DECODER OUT:");
      lcd.setCursor(0, 1);

      if (decodedMessage.length() < 16) 
      {
        lcd.print(decodedMessage);
      }
      else 
      {
        lcd.print(decodedMessage.substring(position, position + 16));
        position++;
        if (position > decodedMessage.length())
        {
          position = 0;
        }
      }

      uint16_t redValue, greenValue, blueValue, clearValue;
      tcs.getRawData(&redValue, &greenValue, &blueValue, &clearValue);

      bool redIsDetected = (redValue > RED_THRESHOLD);
      bool greenIsDetected = (greenValue > GREEN_THRESHOLD);
      bool blueIsDetected = (blueValue > BLUE_THRESHOLD);

      if ((redIsDetected) && (redValue > greenValue) && (redValue > blueValue)) 
      {
        redDetectionHandler();
      }
      else if ((greenIsDetected) && (greenValue > redValue) && (greenValue > blueValue))
      {
        greenDetectionHandler();
      }
      else if ((blueIsDetected) && (blueValue > redValue) && (blueValue > greenValue))
      {
        blueDetectionHandler();
      }

      timeoutHandler();

      delay(250); 
    }

  private:
    String currentSymbol = "";
    String decodedMessage = "";
    unsigned long lastColorTime = 0;
    bool lastIsSpace = false;
    unsigned int position = 0;

    const unsigned int COLOR_DELAY = 500;
    const unsigned int COLOR_TIMEOUT = 1000;

    void redDetectionHandler() 
    {
      unsigned long currentColorTime = millis();
      if (currentColorTime - lastColorTime < COLOR_DELAY) return;
      lastColorTime = currentColorTime;
      currentSymbol += "-";
      lastIsSpace = false;
    }

    void greenDetectionHandler() 
    {
      unsigned long currentColorTime = millis();
      if (currentColorTime - lastColorTime < COLOR_DELAY) return;
      lastColorTime = currentColorTime;
      currentSymbol += ".";
      lastIsSpace = false;
    }

    void blueDetectionHandler() 
    {
      unsigned long currentColorTime = millis();
      if (currentColorTime - lastColorTime < COLOR_DELAY) return;
      lastColorTime = currentColorTime;

      if (lastIsSpace)
      {
          decodedMessage += " ";
          lastIsSpace = false;
          decodeSymbolHandler(true);
          return;
      }

      if (currentSymbol.length() > 0) 
      {
        decodeSymbolHandler(false);
      }

      lastIsSpace = true;
    }

    void decodeSymbolHandler(bool isSpace)
    {
      if (isSpace) 
      {
        Serial.print('_');
        decodedMessage += ' ';
        currentSymbol = "";
      }
      else
      {
        char decodedSymbol = '?';
        for (int index = 0; index < 36; index++) 
        {
          if (currentSymbol == morseCodes[index])
          {
            decodedSymbol = morseChars[index];
            break;
          }
        }

        Serial.print(decodedSymbol);

        if (decodedSymbol != '?')
        {
          decodedMessage += decodedSymbol;
        }

        currentSymbol = "";
      }
    }

    void timeoutHandler()
    {
      unsigned long currentTime = millis();
      if (currentTime - lastColorTime > COLOR_TIMEOUT)
      {
        if (currentSymbol.length() > 0)
        {
          decodeSymbolHandler(false);
        }
        lastIsSpace = false;
      }
    }
};

#define BUTTON_PIN 2

volatile bool buttonPressed = false;
unsigned long lastDebounce = 0;
const unsigned long DEBOUNCE_MS = 200;

int mode = 0;               

MorseDecoderV1 decoder1;
MorseDecoderV2 decoder2;

// ISR — только ставим флаг
void handleButtonISR() {
  buttonPressed = true;
}

void setup() {
  Serial.begin(115200);
  pinMode(BUTTON_PIN, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(BUTTON_PIN), handleButtonISR, FALLING);

  decoder1.begin();
  decoder2.begin();
}

void loop() {
  if (buttonPressed) {
    buttonPressed = false;              
    unsigned long now = millis();
    if (now - lastDebounce >= DEBOUNCE_MS) {
      if (digitalRead(BUTTON_PIN) == LOW) {
        lastDebounce = now;
        mode = (mode + 1) % 2;

        decoder1.reset();
        decoder2.reset();

        delay(100);
      }
    }
  }

  if (mode == 0) {
    decoder1.update();
  } else {
    decoder2.update();
  }
}
