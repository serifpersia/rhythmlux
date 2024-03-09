#include "FadeController.h"
FadeController ::FadeController() {
}

void FadeController::fade(uint8_t fadeRate) {
  for (uint8_t i = 0; i < NUM_LEDS; i++) {
    uint8_t effectiveSplashRate = fadeRate;
    if (keysOn[i]) {
        effectiveSplashRate = 0;
    }
    uint8_t ledNo = i;
    CRGB currentColor = leds[ledNo];

    // Trigger is complete, fade the LED
    if (effectiveSplashRate > 0) {
      leds[ledNo].fadeToBlackBy(fadeRate);
    }
  }
}
