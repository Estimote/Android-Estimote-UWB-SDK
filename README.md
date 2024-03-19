## Introduction to Estimote UWB Beacons Android SDK

Welcome to the Era of Contextual Computing!

This README is created to assist you in developing next-generation, real-world applications that offer magical experiences through location-based and spatial awareness technologies, powered by Estimote UWB Beacons.

Our **Estimote UWB SDK** is a software library designed to showcase precise ranging capabilities between [Estimote UWB Beacons](https://estimote.com/uwb-beacons) and UWB-enabled Android devices (Google Pixel 6 Pro and newer Pro versions, Google Pixel Fold and Google Pixel Tablet, Samsung Galaxy Note 20 Ultra, Galaxy S21 Plus and newer Plus versions, Galaxy Z Fold 2 and newer, Xiaomi MIX4). It leverages the Bluetooth API and the Core Ultra Wideband (UWB) Jetpack library to discover, connect to, and range between UWB-enabled Android devices and beacons.

> [!IMPORTANT]
> Our Android UWB SDK requires at least **Android 14**, so make sure your project's minimum SDK is set up to be at least API 34 ("UpsideDownCake", Android 14.0)

To integrate the Estimote UWB SDK into your project, you need to add Estimote UWB dependencies to your Android Studio project. 

Add below UWB SDK reference to the *dependencies* section of your **build.gradle.kts** file.

```kotlin
dependencies {

  implementation("com.estimote:uwb-sdk:1.0.0-rc5")

}
```

Also, add the Maven link below to the *repositories* section of your **settings.gradle.kts** file.

```kotlin
dependencyResolutionManagement {
  repositories {

    maven { url = uri("https://estimote.jfrog.io/artifactory/android-proximity-sdk/") }

 }
}
```

Remember to *Sync Project with Gradle Files* from the *File* menu after adding them.

Then you are ready to start importing our SDK classes to your project.

```kotlin
import com.estimote.uwb.api.EstimoteUWBFactory
import com.estimote.uwb.api.scanning.EstimoteUWBScanResult
import com.estimote.uwb.api.ranging.EstimoteUWBRangingResult
```

Additionally, ensure that you have added all the necessary permission requests in your app for the Bluetooth and UWB to function correctly. Detailed instructions on the required permissions are provided towards [the end of this document.](https://github.com/Estimote/Android-Estimote-UWB-SDK/#required-user-permissions)


### Getting Started with Estimote UWB Beacons

Imagine beacons as compact, battery-operated computers equipped with sensors and various radio technologies (BLE, UWB).

By design, beacons function in a low-power mode, primarily broadcasting their presence via Bluetooth Low Energy (BLE). This efficient use of power allows them to operate for several years without needing a battery replacement.

![Estimote UWB Beacons](https://s3.amazonaws.com/assets.github.estimote.com/uwb-sdk/estimote-uwb-beacons.png)

**Scanning for Nearby Beacons Over BLE**

To get started you need to create an instance of UWB Manager using `EstimoteUWBFactory`.

```kotlin
class MainActivity : ComponentActivity() {
    private val uwbManager = EstimoteUWBFactory.create()
    // rest of your code
```

Then to detect beacons in your proximity, you'll need to run the `startDeviceScanning()` method of the UWB Manager. This method initiates the scanning process for nearby beacons.

```kotlin
uwbManager.startDeviceScanning(this)
```


Under the hood, the scanning process uses Bluetooth API to search and scan for available Bluetooth Low Energy (BLE) packets. It specifically parses only those packets advertised by our UWB Beacons. 

Upon successful discovery, the **uwbManager.uwbDevices.collect** lambda receives a **scanResult** object. It's a list of **EstimoteDevice** objects
with the following fields:

- EstimoteDevice.deviceId - id of the beacon
- EstimoteDevice.timestamp - timestamp at which the scan result was recorded
- EstimoteDevice.device - device object as `BluetoothDevice` instance
- EstimoteDevice.rssi - received signal strength

In the provided code example, we use this lambda to print the discovery results.

```kotlin
uwbManager.uwbDevices.collect { scanResult: EstimoteUWBScanResult ->
    when (scanResult) {
        is EstimoteUWBScanResult.Devices -> {
            scanResult.devices.forEach { device ->
                println("Discovered device: ${device.deviceId} rssi: ${device.rssi}")
            }
        }
        else -> println("No devices found or error occurred")
    }
}
```

When you run it your Android Studio Logcat might display something like this:

```
Discovered device: 317804 rssi: -71
Discovered device: b288ef rssi: -40
```

>[!WARNING]
>If you don't see the above results in the log, or if your app crashes, it might be because you haven't added the required user permissions for Bluetooth scanning. Read more at the [end of this document](https://github.com/Estimote/Android-Estimote-UWB-SDK/#required-user-permissions).


The visible values in the log are the first 6 digits of unique and persistent identifier for each beacon. You can view the full identifier using our [iOS Estimote UWB app](https://apps.apple.com/us/app/estimote-uwb/id1593848641) from the App Store (only iOS at the moment) or when you log in into your [Estimote Cloud](https://cloud.estimote.com/) account.

![Estimote UWB Beacons](https://s3.amazonaws.com/assets.github.estimote.com/uwb-sdk/estimote-uwb-app.png)

> [!NOTE]
> Your Cloud account user login is typically email address you have used to purchase your Dev Kit.

![Estimote UWB Beacons](https://s3.amazonaws.com/assets.github.estimote.com/uwb-sdk/estimote-cloud-uwb-beacons.png)

```
Discovered device: 317804 rssi: -71
Discovered device: b288ef rssi: -40
```

Next to the identifier, the rssi value represents the received signal strength index (RSSI) in dB units. A higher value indicates closer proximity to the beacon. For instance, -40 dB suggests a relatively close distance (20-50cm), whereas -90 dB indicates a much greater distance (several meters away).

![Estimote UWB Beacons - BLE rssi](https://s3.amazonaws.com/assets.github.estimote.com/uwb-sdk/rssi.gif?v=2)

>[!IMPORTANT]
>It's important to note that the RSSI value is determined by the Android Bluetooth API and does not reflect precise UWB ranging yet. RSSI can be quite variable, fluctuating based on the orientation of the phone or obstacles between the phone and the beacon. Therefore, RSSI should only be used as an indication of which beacons are relatively nearby, especially since your phone can scan and discover hundreds of beacons in the vicinity.


**UWB two-way ranging**

Once you discover UWB beacons using Bluetooth you can use `uwbManager.uwbDevices.collect` lambda. You can perform there a `uwbManager.connect()` method with a `BluetoothDevice` as an argument to establish Bluetooth connection with each beacon. It will obtain the necessary UWB session parameters and turn on UWB radio on the beacon. You can also use `uwbManager.connectSuspend()` with coroutine.

```kotlin
lifecycleScope.launch {
    uwbManager.uwbDevices.collect { scanResult: EstimoteUWBScanResult ->
        when (scanResult) {
            is EstimoteUWBScanResult.Devices -> {
                scanResult.devices.forEach { device ->
                    device.device?.let { bluetoothDevice ->
                        uwbManager.connect(bluetoothDevice, this@MainActivity)
                    }
                }
            }
            else -> {  }
        }
    }
}

```

Once the phone and beacon are connected via Bluetooth, they both turn on their UWB radios and begin exchanging security tokens necessary to initiate UWB session.

After the session is successfully established, both devices run two-way UWB ranging, which yields precise measurements of distance and orientation between them. The technique is called **time-of-flight**. Both the UWB-enabled Android phone and the beacon have very precise clocks and they measure time of radio propagation back-and-forth. Multiplying this time by speed of light (speed of electromagnetic radio waves) they can compute distance down to few inches (10cm) precision.

![Estimote UWB Beacons](https://s3.amazonaws.com/assets.github.estimote.com/uwb-sdk/estimote-uwb-beacons-ranging.gif)

If the UWB ranging is succesful `uwbManager.rangingResult.collect` lambda receives ranging results with precise distance measurements you can print to the console.

```kotlin
 lifecycleScope.launch {
            uwbManager.rangingResult.collect { result ->
                when (result) {
                    is EstimoteUWBRangingResult.Position -> {
                        println("Device address prefix: ${result.device.address.toString()}:..., Distance: ${result.position.distance?.value.toString()} m, Azimuth: ${result.position.azimuth?.value.toString()},  Elevation angle: ${result.position.elevation?.value.toString()} ")
                    }
                    else -> {
                        println("Ranging unavailable or error" )
                    }
                }
            }
        }
```

In the Android Studio Logcat you should see something like below:

```
Device address prefix: 02:39:.... Distance 0.99 m, Azimuth: -40.240578,  Elevation angle: 18.97932  
Device address prefix: 02:39:.... Distance 0.86 m, Azimuth: -33.938553,  Elevation angle: 57.48887 
Device address prefix: 02:39:.... Distance 0.49 m, Azimuth: -25.906908,  Elevation angle: 29.65366 
```

> [!TIP]
> If ultra wideband ranging is successful you should see UWB Beacon LED light flashing when your phone is very close to the beacon.
Ideally keep only one beacon near the phone and the other beacons move to the other room as the current version can only connect and range with one beacon at the same time.

> [!WARNING]
> If you don't see above results in the log or your app crashes it might be because you haven't added
required user permissions for UWB ranging. Read more at the [end of this document](https://github.com/Estimote/Android-Estimote-UWB-SDK/#required-user-permissions).
It is also possible your phone doesn't have UWB or your UWB Beacon doesn't have the latest Android firmware.

```
Device address prefix: 02:39:.... Distance 0.99 m, Azimuth: -40.240578,  Elevation angle: 18.97932  
Device address prefix: 02:39:.... Distance 0.86 m, Azimuth: -33.938553,  Elevation angle: 57.48887 
Device address prefix: 02:39:.... Distance 0.49 m, Azimuth: -25.906908,  Elevation angle: 29.65366 
```

- first is 4 digits of unique beacon **identifier**
- **Distance** is a measured value in meters between phone and beacon
- **Azimuth** is a vector of orientation from where the beacon signal is coming from
- **Elevation angle** is a vector of orientation from where the beacon signal is coming from

Note the azimuth and elevation are computed using a technique known as **angle-of-arrival (AoA)**.

Some Google Pixel phones as well Samsung phones have multiple UWB antennas strategically positioned within the device. As a UWB signal from a beacon reaches these antennas in sequence—first hitting antenna 1, then antenna 2, etc. the time differences between these receptions enable Android to calculate the orientation from which the signal arrived. Our SDK then provices azimuth angle in degrees. See [Android documentation for RangingPosition](https://developer.android.com/reference/androidx/core/uwb/RangingPosition).


>[!IMPORTANT]
>It's important to remember that due to the inherent limitations of radio signal propagation, computing the angle is feasible only when the beacon is "in front of the phone." A simple way to conceptualize this is to consider whether the beacon would be visible to the phone's camera. If the camera could "see" the beacon, then it's likely that the phone's multiple UWB antennas could also "hear" it. This mental model helps in visualizing the positional relationship required for accurate angle-of-arrival (AoA) calculations. 

If the beacon is located behind the phone, such that the camera wouldn't be able to "see" it, you can still obtain UWB ranging and have the distance value computed. However, the **azimuth** or **elevation** will be **null**. 

```
Device address prefix: 02:39:.... Distance 0.49 m, Azimuth: null,  Elevation angle: null 
```

**Manually connecting and disconnecting from UWB Beacons**

In order to be able to interact with many beacons around you would need to make sure you only connect to one beacon at the time. 

If you have obtained the distance measurement you can disconnect from the current UWB session calling **disconnectDevice()** method and then connect to the next one using previously discussed **connect()** method.

```kotlin
uwbManager.disconnectDevice()
```

You can also stop scanning for nearby beacons to completely stop **uwbManager.uwbDevices.collect** -> **connect()** -> **uwbManager.rangingResult.collect** lambdas flow.

```kotlin
uwbManager.stopDeviceScanning()
```

![Estimote UWB Beacons](https://s3.amazonaws.com/assets.github.estimote.com/uwb-sdk/phone-ranging-multiple-beacons.gif)

Obtaining precise distance measurements from many beacons can enhance precision and reliability of your experience or can be used to create simple triangulation and positioning algorithm.

**Multiple phones ranging with the same beacons**

Another reason to manually connect/disconnect or to start/stop scanning is to allow the same beacons to be accessible by multiple phones simultaneously.

>[!WARNING]
>With the existing UWB firmware on our beacons, when one Android phone establishes a connection and begins ranging with a UWB Beacon, other phones will not be able to discover, connect to, or range with the same beacon at the same time.

To workaround this limitation, you should disconnect from the beacons once you have obtained the necessary distance measurements, thereby making them available for other phones. Implementing a clever synchronization/timing algorithm is essential to enable ranging from multiple phones.

> [!TIP]
> Another reason to disconnect from UWB Beacons is to preserve battery life for both the phone and the UWB beacon. Every time the beacon actively ranges, it depletes its AA battery's energy. Therefore, the best approach is to obtain the distance, then shut down the UWB radio until the user makes a significant move.




### Required user permissions

Please remember that for your app to successfully discover, connect to, and range with UWB Beacons, it requires several crucial permissions to be defined and granted by the user.

Your app need to request these permissions from your users once, so it's important to provide a clear explanation of why these permissions are necessary for your application.

- **BLUETOOTH_SCAN** is required to discover nearby UWB Beacons over the Bluetooth.
- **BLUETOOTH_CONNECT** is required to connect to nearby UWB Beacons and to turn on their UWB radio
- **UWB_RANGING** is required to start UWB ranging session with nearby UWB Beacon and to obtain the precise distance and/or orientation

> [!IMPORTANT]
> Make sure to add these permission request to your app initialization otherwise you might not be able see ranging results or your app might crash.
 
 ```kotlin
 requestPermissions(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.UWB_RANGING
            ),
            1
        )
```

Please also make sure you import Manifest class to obtain the permissions.

```kotlin
import android.Manifest
```

When you run your app for the first time make sure you tap **Allow** when prompted to give necessary permissions.

You can also use `uwbManager.init()` instead which will perform a necessary checks and obtain all the needed permissions:

```kotlin
 uwbManager.init(
            activity = this, // 'this' refers to the Fragment instance
            onDenied = { deniedRequirements ->
                // Optional: Custom handling of denied permissions.
            }
        )
```

**Background ranging**

As of February 2024 precise UWB ranging is working only when the app is actively running in the foreground. With our beacons, it is also feasible to range and calculate the distance to beacons in the background, but Android 14 and Core UWB Jetpack library does not support it yet.

**Authentication and security**

This UWB SDK is part of the Estimote UWB Beacons Development Kit, designed to showcase the technology and offer the essential tools for evaluating our hardware and software offerings.

>[!CAUTION]
>UWB Beacons that are sold as part of Development Kits do not have authentication enabled. 
This implies that anyone with access to this SDK can discover, connect to, and range with your beacons draining their battery and obtaining their location/orientation.

If you require a secure solution for deployment in a production environment, please contact our team to discuss licensing, as well as production firmware and hardware options.

**Settings and customization**

Our UWB Beacons sold as part of the Development Kit have default settings for Bluetooth advertising interval as well as disconnect timeout or UWB ranging frequency. If your use-case requires different settings please contact our team.


### Contact Estimote Team

Our contact details are provided on our website [www.estimote.com](https://estimote.com) and our customer success team is always available at contact (at) estimote.com to discuss business opportunities or opening a support project. We have shipped millions of beacons and have seen most sophisticated use-cases and are always happy to recommend the best approach or discuss firmware/hardware customization for your project.

Unfortunately our engineers are not able to provide tech assistance to every Dev Kit customer without support project initiated. Free tech support can be only provided via our [Developer Forum](https://forums.estimote.com/).
