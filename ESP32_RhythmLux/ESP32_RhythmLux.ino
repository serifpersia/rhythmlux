#include <WiFi.h>
#include <WiFiUdp.h>
#include <ArduinoOTA.h>
const int udpPort = 12345;
const char* ssid = "yourwifi_ssid";
const char* password = "yourwifi_pass";

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

  // Connect to WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("WiFi connected!");

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


  ArduinoOTA.setHostname("ESP32-OTA");

  ArduinoOTA
  .onStart([]() {
    String type;
    if (ArduinoOTA.getCommand() == U_FLASH)
      type = "sketch";
    else // U_LittleFS
      type = "filesystem";

    // NOTE: if updating LittleFS this would be the place to unmount LittleFS using LittleFS.end()
    Serial.println("Start updating " + type);
  })
  .onEnd([]() {
    Serial.println("\nEnd");
  })
  .onProgress([](unsigned int progress, unsigned int total) {
    Serial.printf("Progress: %u%%\r", (progress / (total / 100)));
  })
  .onError([](ota_error_t error) {
    Serial.printf("Error[%u]: ", error);
    if (error == OTA_AUTH_ERROR) Serial.println("Auth Failed");
    else if (error == OTA_BEGIN_ERROR) Serial.println("Begin Failed");
    else if (error == OTA_CONNECT_ERROR) Serial.println("Connect Failed");
    else if (error == OTA_RECEIVE_ERROR) Serial.println("Receive Failed");
    else if (error == OTA_END_ERROR) Serial.println("End Failed");
  });
  ArduinoOTA.begin();

  // Allocate memory for keyArray with default size
  keyArray = new uint8_t[4] {68, 70, 74, 75}; // Example keycodes for 4 modes
  numKeyModes = 4;

  calculateLedDivisions();
}

void calculateLedDivisions() {
  int sectionSize = NUM_LEDS / numKeyModes;
  int remainder = NUM_LEDS % numKeyModes;

  ledDivisions[0] = 0;
  for (int i = 1; i <= numKeyModes; i++) {
    ledDivisions[i] = ledDivisions[i - 1] + sectionSize + (i <= remainder ? 1 : 0);
  }
}
void loop() {
  delay(1000);
  ArduinoOTA.handle();
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
      default:
        break;
    }
  }
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
void blackout() {
  fill_solid(leds, NUM_LEDS, CRGB::Black);
}
