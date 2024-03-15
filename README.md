

<div align="center">

![svgviewer-output](https://github.com/serifpersia/rhythmlux/assets/62844718/7702dad8-64a9-4fbc-99ef-c1121bdd2cab)

<h1><span class="piano-text" style="color: white;">Rhythm</span><span class="lux-text" style="color: yellow;">Lux</span></h1>   

[![Release](https://img.shields.io/github/release/serifpersia/rhythmlux.svg?style=flat-square)](https://github.com/serifpersia/rhythmlux/releases)
[![License](https://img.shields.io/github/license/serifpersia/rhythmlux?color=blue&style=flat-square)](https://raw.githubusercontent.com/serifpersia/rhythmlux/master/LICENSE)
[![Discord](https://img.shields.io/discord/1077195120950120458.svg?colorB=blue&label=discord&style=flat-square)](https://discord.gg/MAypyD7k86)
</div>

<div align="center">
 
![image](https://github.com/serifpersia/rhythmlux/assets/62844718/41110702-1293-4566-8bed-04a51290b5d1)


</div>

RhythmLux is Java based LED strip controller for rhythm games. Works on 4K-10K key modes

## Hardware
- Supported boards: ESP32, ESP32 S2 & ESP32 S3 4MB, 8MB and 16MB
- Supported LED strip WS2812B(NEOPIXEL) any density can work, 144/m 60/m 30/m
- Auto install firmwares have hard coded 176 number of leds, modify & upload esp32 arduino ide ino code
 with your number of leds if you need more
- Female to male jumper cables x3

## Hardware Setup
Connect correct side of led strip to esp32 pins(data arrow on led strip should point to right, usually its female connector
use jumper cables:
- 5V Red Wire - ESP32 5V pin
- GND White Wire - ESP32 GND pin
- DIn Green Wire - ESP32 pin 18

## Installation
- Visit [![Auto Install](https://img.shields.io/badge/Auto-%20Install-blue?style=flat-square)](https://serifpersia.github.io/rhythmlux/) page to install RhythmLux automatically.
- Connect your wifi capable device to ESP32's AP called RhythmLux.
- Visit http://rhythmlux.local or just rhythmlux.local or default http://192.168.4.1/ to setup your network, type your WiFi network name & password and press Configure button, ESP32 will restart and use those WiFi credentials.
- led current default 450 mA, if powering led strip externally modify this value to match your 80-90% of your 5V Power supply example PSU 3A(3000mA) use 2800 value and make sure you connect
  ground from external PSU to gnd pin on ESP32 to avoid groung loop issues.
- If your WiFi capable device no longer sees this AP anymore your ESP32 is connected to your local network, to get back to this AP again connect pin 15 to gnd while powering ESP32,
remove the connection once you see the AP, now you can configure network & led strip current again.
- Lastly install Java x64 JRE for your system to run the java application

## Limitations
Current version doesn't support non letter key bindings. Autohotkey could be used to translate desired key into key that the current version of the app supports.

### Usage
- Launch java app, press the scan button to automatically find ESP32's IP
- Configure your key bindings and press Start now your key presses will be translated to strip LEDs
- Use Update button to update state of the keys and fade slider value

## License
This project is licensed under the [MIT License](LICENSE).

