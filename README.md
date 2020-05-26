# 1. Introduction SDK
## copy witstecbletagsdk.aar package to app/libs directory.
app/build.gradle add the following code to the arr file to the project dependency
```
repositories {
    flatDir {
        dirs 'libs'
    }
}
dependencies {
    compile(name: 'witstecbletagsdk_v1.0.0', ext: 'aar')
}
```
# 2. APPLICATION
## Application for APP First Start Permission
Add permission to declare to apply to AndroidManifest.xml file
Attention: In addition to the declaration in the AndroidManifest.xm, 
you also need to dynamically apply for these permissions when entering the main interface, 
otherwise you can not use the function properly
```
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

# Bluetooth/position switch check
every time before calling the SDK interface, you need to check if the bluetooth and position switch are on at the same time. 
if there is no need to guide the user to turn on the bluetooth and position switch, if all are on state, 
you can do a series of operations.

```
     //Check if positioning is on
        if (ScanUtil.isLocationEnabled(MainActivity.this)) {
            //Get Bluetooth Manager
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            //Check if Bluetooth is turned on
            if (!mBluetoothAdapter.isEnabled()) {
                // If it is not turned on, request to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            } else {
                //Bluetooth operation
                setListener();
            }
        } else {
            //Enter the open position setting interface
            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
        }
```
# 3．SDK interface calls
## Initialization SDK
Note: Add initialization SDK, to the Application onCreate method
```
EPaperSdk.init (this);
```
## SDK resource release
Call the following interface to log out of the application
```
EPaperSdk.destroy();
```
## Log Debugging Switch
 The SDK log switch can be turned on in debug mode to see more log information to help locate the problem quickly. it is recommended to turn off the log switch in release mode.
```
EPaperSdk.setDebugMode (true);
```
## Scan device interface calls
Note: Open Bluetooth, search nearby device name "WITSTEC" Bluetooth device, and get MAC address and signal strength

### Startup scan
Directions: Start Bluetooth scanning
```
EPaperSdk.BleScanManager.startScanNow ();
```
### Scanned results returned
Description: The search results are returned once in 2.5 seconds, the return results are filtered, only return the electronic signature called "WITSTEC" device,
```
             //Start scanning
                EPaperSdk.BleScanManager.getScanResult(new BleScanCallbackCompat() {
                    @Override
                    public void onScanResult(List<ScanData> deviceList) {
                        //Print the obtained device information
                        for (ScanData scanData : deviceList) {
                            Log.i("scData", scanData.getAddress());
                        }
                    }
                });
```
#### ScanData Object Parameter Description

Parameters | Type |  Note  
-|-|-
address | String | Bluetooth mac address |
name | String | Bluetooth Device Name |
rssi | String | Equipment signal values |

### Stop scanning
Description: Stop searching for nearby devices, no longer return scan results,
```
EPaperSdk.BleScanManager.stopCycleScan ();
```
## Connection Device Interface Call
Description: Connect the device, get the device details, disconnect, check if connected, reconnect.
### Connection equipment
Description: Enter the mac address to connect the device, and the device details will be returned after the connection device is successful.
```
 // Scan and get device information No device notification callback
   EPaperSdk.connectMsgManager.connection(mac, new BleConnectionDeviceInfoCallback() {

                    // Connection Status Callback, Update Connection Status
                    @Override
                    public void onConnectionChange(StatusCode statusCode) {
                        if (statusCode == StatusCode.CONNECTION_START) {
                            LogHelper.i("Start to connect");
                        } else if (statusCode == StatusCode.CONNECTION_SUCCESS) {
                            LogHelper.i("Connection complete");
                        } else if (statusCode == StatusCode.CONNECTION_GET_MSG_START) {
                            LogHelper.i("Getting device information");
                        } else if (statusCode == StatusCode.CONNECTION_GET_MSG_SUCCESS) {
                            LogHelper.i("Complete device information");
                        }
                    }

                    // Call when connection encounters exception, return exception error code
                    @Override
                    public void onConnectionError(ErrorCode errorCode) {
                        if (errorCode == ErrorCode.ERROR_BLE_CONNECTION_TIMEOUT) {
                            LogHelper.i("Connection timed out");
                        } else if (errorCode == ErrorCode.ERROR_CONNECTION_GET_DEVICE_MSG_TIMEOUT) {
                            LogHelper.i("Failed to obtain device information");
                        }
                    }

                    // The connection gets the device information successfully and returns the detailed device information
                    @Override
                    public void onConnectionSuccess(DeviceInfo msg) {
                        LogHelper.i("Device power" + msg.getPower());
                        LogHelper.i("Device address" + msg.getAddress());
                        LogHelper.i("Device version" + msg.getVersion());
                    }
                });
```
#### The state StatusCode callback in the void onConnectionChange (statusCode) method


State code|Note
-|-
CONNECTION_START|	Connection equipment|
CONNECTION_SUCCESS	|Device connection successfully|
CONNECTION_GET_MSG_START|	Equipment information is being obtained|
CONNECTION_GET_MSG_SUCCESS	|Access to device information|


#### The state ErrorCode callback in the void onConnectionError (errorCode) method


State code	| Note
ERROR_BLE_CONNECTION_TIMEOUT	|Connection Device Timeout|
ERROR_CONNECTION_GET_DEVICE_MSG_TIMEOUT	|Failed to obtain device information|


#### void onConnectionSuccess description of object parameters in DeviceInfo callback method


Parameters|	Type|	Note
-|-|-
address|	String|	Bluetooth mac address|
name|	String|	Bluetooth Device Name|
version|	String|	Device Firmware Version|
power	|Int|	Percentage of surplus equipment|
deviceType|	 DeviceType|	Type of equipment size|


#### DeviceType enumeration object description

Parameters|	Note
-|-
DEVICE_042	|4.2 inch equipment|
DEVICE_075	|7.5 inch equipment|
DEVICE_029	|2.9 inch equipment|


### Disconnect
Directions: Disconnect the device connection and need to reconnect after disconnection.

```
EPaperSdk.connectMsgManager.disConnection ();

```

### Gets the device connection state
Description: Return device connection status true/false.

```
boolean isConnection =EPaperSdk.connectMsgManager.isConnection ();
```

### Reconnect, no callback
description: incoming mac address, reconnect to the device.

```
EPaperSdk.bleConnectDeviceMsgManager.connection (mac);
```

### Release Connection Resources
Description: Disconnect the device and release the resource

```
EPaperSdk.bleConnectDeviceMsgManager.release ();
```


## Send template images to device interface calls
Description: Because it is custom version SDK, so built-in template files, no need to import template files. For ease of understanding, the following is an example diagram of the display effect of a built-in template file, with five input boxes, for one QR code, four text. All you need to do is pass in the values in the input box as Json.



























