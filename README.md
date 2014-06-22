# Android Controllable Lights
#### Developed by Thomas Jespersen, TKJ Electronics 2013

The code is released under the GNU General Public License.
_________

This is the code is for controlling a WS2801 LED string with your Arduino using your Android phone with Bluetooth as remote control.

You can check out a video demonstration of the project here: <http://www.youtube.com/watch?v=pZSADQihV_Y>

For more information see my blog post at <http://blog.tkjelectronics.dk/> or send me an email at <thomasj@tkjelectronics.dk>.


# Commands


The first byte of a command package is the command byte, descriping the command of the package. The next bytes are all parameters as described below. Each parameter is terminated with a ‘;’ character, also when only one parameter is needed.


0x0F = Color command [Parameters: 3 bytes of 3 char each]

0x01 = Add fade color [Parameters: 3 bytes of 3 char each]

0x02 = Reset fade colors [Parameters: None]

0x03 = Set speed delay [Parameters: 1 integer of 5 chars]

0x04 = Enable Fade effect [Parameters: None]

0x05 = Add Snap color [Parameters: 3 bytes of 3 char each]

0x06 = Reset snap colors [Parameters: None]

0x07 = Enable Snap effect [Parameters: None]

0x08 = Add Running color (requires WS2801, hence disabled here) [Parameters: 3 bytes of 3 char each]

0x09 = Reset Running colors (requires WS2801, hence disabled here) [Parameters: None]

0x0A = Enable Running effect (requires WS2801, hence disabled here) [Parameters: None]

0x0B = Add Running Fade color (requires WS2801, hence disabled here) [Parameters: 3 bytes of 3 char each]

0x0C = Reset Running Fade colors (requires WS2801, hence disabled here) [Parameters: None]

0x0D = Enable Running Fade effect (requires WS2801, hence disabled here) [Parameters: None]

0x0E = Disable any effect [Parameters: None]

0x1A = Invalidate EEProm