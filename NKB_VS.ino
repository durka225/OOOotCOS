#define LED_PIN 2
#define RED_PIN 25
#define GREEN_PIN 26
#define BLUE_PIN 27

#include <vector>
#include <string>
#include <map>
#include <WiFi.h>
#include <WebServer.h>
#include <ArduinoJson.h>
#include <cstring>
#include <cctype>

using namespace std;

const char* ssid = "Galaxy S25 Ultra";
const char* password = "qv7tb2e72hf6qn9";

WebServer server(80);

string message = "";
short repeatCount = 0;

void handlRoot() {
  server.send(200, "text/plain", "ESP32 работает!");
}

void handlJson() {
  if (server.hasArg("plain") == false) {
    server.send(400, "text/plain", "Body not received");
    return;
  }
  
  String body = server.arg("plain");
  
  JsonDocument doc;
  
  DeserializationError error = deserializeJson(doc, body);
  
  if (error) {
    Serial.print("JSON parsing failed: ");
    Serial.println(error.c_str());
    server.send(400, "application/json", "{\"status\":\"error\",\"message\":\"Invalid JSON\"}");
    return;
  }
  
  if (doc.containsKey("text")) {
    const char* text = doc["text"];
    message = string(text);
    Serial.print("Received text: ");
    Serial.println(message.c_str());
  }
  
  if (doc.containsKey("repeat")) {
    repeatCount = doc["repeat"];
    Serial.print("Repeat count: ");
    Serial.println(repeatCount);
  }
  
  server.send(200, "application/json", "{\"status\":\"ok\"}");
}

void setup() {
  Serial.begin(115200);
  pinMode(LED_PIN, OUTPUT);
  pinMode(RED_PIN, OUTPUT);
  pinMode(GREEN_PIN, OUTPUT);
  pinMode(BLUE_PIN, OUTPUT);

  Serial.println("Подключение к Wi-Fi");
  WiFi.begin(ssid, password);

  while(WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nПодключено к Wi-Fi");

  Serial.println(WiFi.localIP());

  server.on("/", HTTP_GET, handlRoot);
  server.on("/command", HTTP_POST, handlJson);

  server.begin();
  Serial.println("HTTP server started");

  Serial.println("Setup done");
}

char* string_to_char(string word) {
  char* arr = new char[word.size() + 1];
  for (size_t i = 0; i < word.size(); i++) {
    arr[i] = word[i];
  }
  arr[word.size()] = '\0';
  return arr;
}

std::vector<short> encoding_word(char* arr) {
  std::map<char, std::vector<short>>morse_code = {
    {'a', {0, 1}},
    {'b', {1, 0, 0, 0}},
    {'c', {1, 0, 1, 0}},
    {'d', {1, 0, 0}},
    {'e', {0}},
    {'f', {0, 0, 1, 0}},
    {'g', {1, 1, 0}},
    {'h', {0, 0, 0, 0}},
    {'i', {0, 0}},
    {'j', {0, 1, 1, 1}},
    {'k', {1, 0, 1}},
    {'l', {0, 1, 0, 0}},
    {'m', {1, 1}},
    {'n', {1, 0}},
    {'o', {1, 1, 1}},
    {'p', {0, 1, 1, 0}},
    {'q', {1, 1, 0, 1}},
    {'r', {0, 1, 0}},
    {'s', {0, 0, 0}},
    {'t', {1}},
    {'u', {0, 0, 1}},
    {'v', {0, 0, 0, 1}},
    {'w', {0, 1, 1}},
    {'x', {1, 0, 0, 1}},
    {'y', {1, 0, 1, 1}},
    {'z', {1, 1, 0, 0}}
  };
  vector<short> encoded;
  size_t len = strlen(arr);
  for (size_t i = 0; i < len; i++) {
    char simbol = arr[i];
    if (simbol == ' ') {
      encoded.push_back(2);
      continue;
    }
    simbol = (char)tolower((unsigned char)simbol);
    if (morse_code.find(simbol) != morse_code.end()) {
      vector<short> code = morse_code[simbol];
      encoded.insert(encoded.end(), code.begin(), code.end());
      encoded.push_back(2);
    }
  }
  return encoded;
}

void start_string() {
  for (int j = 0; j < 3; j++) {
    digitalWrite(BLUE_PIN, 1);
    delay(300);
    digitalWrite(BLUE_PIN, 0);
    delay(300);
  }
}


void loop() {
  server.handleClient();

  string str = message;
  if (str.empty()) {
    delay(10);
    return;
  }
  int c = repeatCount;
  repeatCount = 0;
  message.clear();

  char* arr = string_to_char(str);
  vector<short> encoded = encoding_word(arr);
  Serial.println(str.c_str());
  for (int i = 0; i < c; i++) {
    start_string();
    for (short signal : encoded) {
    Serial.println(signal);
      if (signal == 0) {
        digitalWrite(GREEN_PIN, 1);
        delay(300);
        digitalWrite(GREEN_PIN, 0);
      } else if (signal == 1) {
        digitalWrite(RED_PIN, 1);
        delay(300);
        digitalWrite(RED_PIN, 0);
      } else if (signal == 2) {
        digitalWrite(BLUE_PIN, 1);
        delay(300);
        digitalWrite(BLUE_PIN, 0);
      }
      delay(300);
    }
  }
  delete[] arr;
}
