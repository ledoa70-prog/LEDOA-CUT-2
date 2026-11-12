# LEDOA 인도금 계산기 Android APK (1.0.0)

LEDOA's calculator is hosted at https://ledoa-indopayment.vercel.app/.
This Android application opens the live mobile calculator in a WebView and
bridges the website's Web Share API to Android's native image sharing.

* Invoices are rendered from the existing calculator with all nine card logos.
* A single tap saves a JPEG to Pictures/LEDOA and hands it directly to KakaoTalk.
* The recipient still must be selected in KakaoTalk. The app cannot silently send.
* If KakaoTalk is not installed, Android shows the system app chooser.
* Customer payment details remain in the WebView's localStorage on device.
* The app requires internet to load the calculator; images are stored locally.
* Native sharing tested by CI compilation only until a real device test is done.

Main Activity package: kr.ledoa.indopayment (separate from other LEDOA apps).
Android 10+ (API 29+) required.
