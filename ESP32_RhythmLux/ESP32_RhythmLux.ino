#include <WiFi.h>
#include <EEPROM.h>

#include <ESPAsyncWebServer.h>
#include <WebSocketsServer.h>
#include <ArduinoJson.h>
#include <SPIFFS.h>

#define SSID_MAX_LENGTH 32
#define PASS_MAX_LENGTH 64

char ssid[SSID_MAX_LENGTH + 1]; // +1 for null terminator
char password[PASS_MAX_LENGTH + 1]; // +1 for null terminator

AsyncWebServer server(80);
WebSocketsServer webSocket(81);

#define udpPort 12345
WiFiUDP udp;

// Constants and variables for LED control
#include <FastLED.h>

#define NUM_LEDS 176
#define DATA_PIN 18
#define LED_CURRENT 2000

CRGB leds[NUM_LEDS];

// Define the maximum number of key modes
const int maxKeyModes = 10;

// Define key mode and LED division arrays
uint8_t* keyArray; // Pointer to dynamically allocated keyArray
uint8_t numKeyModes; // Number of key modes
uint8_t ledDivisions[maxKeyModes + 1]; // One more division than key modes
uint8_t fadeValue = 64;

boolean keysOn[NUM_LEDS];


// Task handles
TaskHandle_t LEDControlTask;
TaskHandle_t WiFiCommunicationTask;

void setup() {
  Serial.begin(115200);

  // Load credentials from EEPROM
  loadCredentialsFromEEPROM();

  // Check if pin 15 is grounded to determine whether to start in AP mode
  pinMode(15, INPUT_PULLUP);
  bool startInAPMode = digitalRead(15) == LOW;

  // Attempt to connect to Wi-Fi
  if (!connectToWiFi() || startInAPMode) {
    // If connection fails or startInAPMode is true, start in AP mode
    startAPMode();
  } else {
    // Initialize LED control task
    xTaskCreatePinnedToCore(
      taskLEDControl,
      "LEDControlTask",
      4096,
      NULL,
      1,
      &LEDControlTask,
      1
    );

    // Initialize WiFi communication task
    xTaskCreatePinnedToCore(
      taskWiFiCommunication,
      "WiFiTask",
      4096,
      NULL,
      1,
      &WiFiCommunicationTask,
      1
    );

    // Allocate memory for keyArray with default size
    keyArray = new uint8_t[4] {68, 70, 74, 75}; // Example keycodes for 4 modes
    numKeyModes = 4;

    calculateLedDivisions();
  }
}

void loop() {
  webSocket.loop();
}

void saveCredentialsToEEPROM() {
  EEPROM.begin(SSID_MAX_LENGTH + PASS_MAX_LENGTH + 2); // Add 2 for null terminators
  EEPROM.put(0, ssid);
  EEPROM.put(SSID_MAX_LENGTH + 1, password);
  EEPROM.commit();
  EEPROM.end();
}

void loadCredentialsFromEEPROM() {
  EEPROM.begin(SSID_MAX_LENGTH + PASS_MAX_LENGTH + 2); // Add 2 for null terminators
  EEPROM.get(0, ssid);
  EEPROM.get(SSID_MAX_LENGTH + 1, password);
  EEPROM.end();
}

void startAPMode() {
  Serial.println("Starting in AP mode...");

  WiFi.softAP("RhythmLux Setup");

  Serial.println(WiFi.softAPIP());


  if (SPIFFS.begin()) {
    server.serveStatic("/", SPIFFS, "/");
    server.onNotFound([](AsyncWebServerRequest * request) {
      if (request->url() == "/") {
        request->send(SPIFFS, "/index.html", "text/html");
      } else {
        request->send(404, "text/plain", "Not Found");
      }
    });
  } else {
    Serial.println("Failed to mount SPIFFS file system");
  }

  server.begin();

  webSocket.begin();
  webSocket.onEvent(webSocketEvent);
}

bool connectToWiFi() {
  Serial.println("Connecting to Wi-Fi...");
  WiFi.begin(ssid, password);

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print("Status: ");
    Serial.println(WiFi.status()); // Print Wi-Fi status for debugging
    Serial.println("Connecting to Wi-Fi...");
    attempts++;
    if (attempts > 5) {
      Serial.println("Failed to connect to Wi-Fi");
      return false;
    }
  }

  Serial.println("Connected to Wi-Fi");
  return true;
}

void taskLEDControl(void* parameter) {
  // Initialize LED control here
  FastLED.addLeds<NEOPIXEL, DATA_PIN>(leds, NUM_LEDS);
  FastLED.setMaxPowerInVoltsAndMilliamps(5, LED_CURRENT);
  FastLED.setBrightness(50);

  uint8_t hue = 0;

  while (true) {
    // Update hue for dynamic color change
    hue++;

    // Iterate through each LED
    for (int i = 0; i < NUM_LEDS; i++) {
      if (keysOn[i]) {
        // Change the hue for LEDs corresponding to active keycodes
        leds[i] = CHSV(hue, 255, 255);
      } else {
        // Fade out LEDs not corresponding to active keycodes
        leds[i].fadeToBlackBy(fadeValue);
      }
    }

    // Show the updated LEDs
    FastLED.show();

    // Delay for smoother animation
    vTaskDelay(0 / portTICK_PERIOD_MS);
  }
}

void taskWiFiCommunication(void* parameter) {
  // Initialize UDP server here
  udp.begin(udpPort);
  Serial.println("UDP server started, listening on port " + String(udpPort));

  while (true) {
    // WiFi communication logic goes here
    receiveUDPData();
    vTaskDelay(0 / portTICK_PERIOD_MS); // Adjust delay as needed
  }
}

void calculateLedDivisions() {
  int sectionSize = NUM_LEDS / numKeyModes;
  int remainder = NUM_LEDS % numKeyModes;

  ledDivisions[0] = 0;
  for (int i = 1; i <= numKeyModes; i++) {
    ledDivisions[i] = ledDivisions[i - 1] + sectionSize + (i <= remainder ? 1 : 0);
  }
}

void receiveUDPData() {
  // Check if data is available to read
  int packetSize = udp.parsePacket();
  if (packetSize) {
    byte packetBuffer[packetSize]; // Create a buffer to hold the packet data
    udp.read(packetBuffer, packetSize); // Read the packet into packetBuffer

    // Process the packet based on its type
    switch (packetSize) {
      case 4 ... 10:
        handleKeysArray(packetBuffer, packetSize);
        break;
      case 2:
        handleKeyCodes(packetBuffer[0], packetBuffer[1] == 1);
        break;
      case 1:
        handleFadeValue(packetBuffer[0]);
        break;
      case 11: // Special packet size for requesting ESP32 IP info
        sendESP32IPInfo(); // Send ESP32 IP info in response
        break;
      default:
        break;
    }
  }
}

void sendESP32IPInfo() {
  IPAddress localIP = WiFi.localIP();

  byte responseBuffer[4]; // Assuming IPv4 address
  responseBuffer[0] = localIP[0];
  responseBuffer[1] = localIP[1];
  responseBuffer[2] = localIP[2];
  responseBuffer[3] = localIP[3];

  udp.beginPacket(udp.remoteIP(), udp.remotePort());
  udp.write(responseBuffer, 4); // Assuming IPv4 address
  udp.endPacket();
}

void handleKeysArray(byte* packetBuffer, int size) {
  // Update the number of key modes based on the packet size
  numKeyModes = size;

  // Free the previously allocated memory for keyArray
  delete[] keyArray;

  // Allocate new memory and copy data from packetBuffer
  keyArray = new uint8_t[numKeyModes];
  for (int i = 0; i < numKeyModes; i++) {
    keyArray[i] = packetBuffer[i]; // Copy data from packetBuffer
  }
  calculateLedDivisions();
}


void handleKeyCodes(int keyCode, boolean keyPressed) {
  // Print the received key state
  if (keyPressed) {
    keyOn(keyCode);
  } else {
    keyOff(keyCode);
  }
}

void handleFadeValue(byte fade) {
  fadeValue = fade;
}

void keyOn(uint8_t keyCode) {
  int sectionSize = NUM_LEDS / numKeyModes;

  for (int i = 0; i < numKeyModes; i++) {
    if (keyCode == keyArray[i]) {
      int startIndex = ledDivisions[i];
      int endIndex = ledDivisions[i + 1];

      for (int j = startIndex; j < endIndex; j++) {
        keysOn[j] = true;
      }
      break;
    }
  }
}

void keyOff(uint8_t keyCode) {
  int sectionSize = NUM_LEDS / numKeyModes;

  for (int i = 0; i < numKeyModes; i++) {
    if (keyCode == keyArray[i]) {
      int startIndex = ledDivisions[i];
      int endIndex = ledDivisions[i + 1];

      for (int j = startIndex; j < endIndex; j++) {
        keysOn[j] = false;
      }
      break;
    }
  }
}

void webSocketEvent(uint8_t num, WStype_t type, uint8_t *payload, size_t length) {
  switch (type) {
    case WStype_TEXT:
      // Parse the JSON message
      StaticJsonDocument<200> doc;
      DeserializationError error = deserializeJson(doc, payload);

      if (error) {
        Serial.print("JSON parsing error: ");
        Serial.println(error.c_str());
        return;
      }

      String action = doc["action"];

      if (action == "configureNetworkAction") {
        // Extract SSID and password from the JSON message
        String wifi_ssid = doc["wifi"];
        String wifi_password = doc["password"];

        // Copy SSID and password to char arrays
        wifi_ssid.toCharArray(ssid, SSID_MAX_LENGTH + 1);
        wifi_password.toCharArray(password, PASS_MAX_LENGTH + 1);

        // Save credentials to EEPROM
        saveCredentialsToEEPROM();
        delay(1000);
        ESP.restart();
      }
      break;
  }
}
