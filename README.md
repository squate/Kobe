# Kobe
Kobe is an Android application that allows you to throw your phone at an AR generated target overlaid into the real world. It also contains KobeQuest, a text based quest system involving throwing and spinning your phone. Kobe is an app where you throw your phone because we said so, please.

Method 1: Google Play Store
https://play.google.com/store/apps/details?id=com.moonsplain.kobe

Method 2: Android Studio (for phones that can't run the store version, this will get quest mode but not AR mode on your device)
1. Navigate to https://developer.android.com/studio/ and install the latest version of Android Studio
3. Download the Master branch from GitHub and save into a directory of your choice.
2. When you see the Android Studio menu, chose "Open an Existing Android Studio Project"
3. Chose the folder you just saved and open it in Android Studio
4. Wait for Android Studio to set itself up.
5. Plug in your Android device to the USB port on your laptop and allow any debugging/messages that pop up on your phone
6. Press the green triangle "Run" button on the top toolbar (if it is not there press Shift+F10)
7. The app should install install and you can now use Kobe!

Some notes:
AR mode only works on newer phones with x64 cpu architechture. To see if your device is compatible follow this link:
https://developers.google.com/ar/discover/supported-devices#android_play
Kobe will NOT work on emulator because the AR mode will not be able to detect planes. You can run it but you won't be able to throw it, unless you have some sort of way to spoof sensor input, which we don't.
Kobequest works on all devices with an accelerometer and gyroscope.
