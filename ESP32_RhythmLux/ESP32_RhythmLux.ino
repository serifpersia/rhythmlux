#include <WiFi.h>
#include <WiFiUdp.h>
#include <ArduinoOTA.h>
const int udpPort = 12345;
const char* ssid = "yourwifi_ssid";
const char* password = "yourwifi_pass";

WiFiUDP udp;

// Constants and variables for LED control
#include <FastLED.h>
#include "FadeController.h"

#define NUM_LEDS 176
#define DATA_PIN 18
#define LED_CURRENT 2000

FadeController* fadeCtrl = new FadeController();
CRGB leds[NUM_LEDS];

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
        leds[i].fadeToBlackBy(64);
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
  // Check for incoming UDP packets
  int packetSize = udp.parsePacket();
  if (packetSize) {
    // Read the packet into a buffer
    byte packetBuffer[2];
    int len = udp.read(packetBuffer, 2);
    // Assume that the packet contains the keycode and its state as bytes
    if (len > 0) {
      if (len == 2) {
        byte keyCode = packetBuffer[0];
        bool keyPressed = packetBuffer[1] == 1;

        if (keyPressed) {
          keyOn(keyCode);
        } else {
          keyOff(keyCode);
        }
        Serial.printf("Keycode: %d, State: %s\n", keyCode, keyPressed ? "pressed" : "released");
      } else {
        Serial.println("Invalid packet size");
      }
    }
  }
}

void keyOn(uint8_t keyCode) {
  // Define the number of sections and the size of each section
  int sectionSize = NUM_LEDS / 4; // Divide the LED strip into 4 equal sections

  // Define the start and end indices of the section in the keysOn array
  int startIndex, endIndex;

  // Define the color for each keycode and calculate section indices
  CRGB color;
  switch (keyCode) {
    case 32:
      startIndex = 0;
      endIndex = sectionSize;
      break;
    case 33:
      startIndex = sectionSize;
      endIndex = 2 * sectionSize;
      break;
    case 36:
      startIndex = 2 * sectionSize;
      endIndex = 3 * sectionSize;
      break;
    case 37:
      startIndex = 3 * sectionSize;
      endIndex = NUM_LEDS;
      break;
    default:
      // Handle unknown keycodes or keys
      return;
  }

  // Update the corresponding section of keysOn to true
  for (int i = startIndex; i < endIndex; i++) {
    keysOn[i] = true;
  }
}

void keyOff(uint8_t keyCode) {
  // Define the number of sections and the size of each section
  int sectionSize = NUM_LEDS / 4; // Divide the LED strip into 4 equal sections

  // Define the start and end indices of the section in the keysOn array
  int startIndex, endIndex;

  // Calculate the section indices based on the key code
  switch (keyCode) {
    case 32:
      startIndex = 0;
      endIndex = sectionSize;
      break;
    case 33:
      startIndex = sectionSize;
      endIndex = 2 * sectionSize;
      break;
    case 36:
      startIndex = 2 * sectionSize;
      endIndex = 3 * sectionSize;
      break;
    case 37:
      startIndex = 3 * sectionSize;
      endIndex = NUM_LEDS;
      break;
    default:
      // Handle unknown keycodes or keys
      return;
  }

  // Update the corresponding section of keysOn to false
  for (int i = startIndex; i < endIndex; i++) {
    keysOn[i] = false;
  }
}

void blackout() {
  fill_solid(leds, NUM_LEDS, CRGB::Black);
}
