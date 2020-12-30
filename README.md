# 1. Import SDK

## copy witstecbletagsdk.aar package to app/libs directory.
app/build.gradle add the following code to the arr file to the project dependency

```
repositories {
    flatDir {
        dirs 'libs'
    }
}
dependencies {
    implementation(name: 'witstecbletagsdk_v1.0.0', ext: 'aar')
}
```


# 2. Permission Request
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

----


# 3．SDK interface calls
## init SDK
Note: Add initialization SDK, to the Application onCreate method

```
EPaperSdk.init (this);
```
## close SDK
Call the following interface to log out of the application

```
EPaperSdk.destroy();
```

## Log Switch
 The SDK log switch can be turned on in debug mode to see more log information to help locate the problem quickly. it is recommended to turn off the log switch in release mode.
 
```
EPaperSdk.setDebugMode (true);
```
## Scan device interface calls
Note: Open Bluetooth, search nearby device name "WITSTEC" Bluetooth device, and get MAC address and signal strength

### Start scan
Directions: Start Bluetooth scanning

```
EPaperSdk.BleScanManager.startScanNow ();
```

### Scan Result Callback
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


### Stop scan
Description: Stop searching for nearby devices, no longer return scan results,

```
EPaperSdk.BleScanManager.stopCycleScan ();
```

## Connection Device Interface Call
Description: Connect the device, get the device details, disconnect, check if connected, reconnect.

### Connection Device
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
-|-
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


### Disconnect Device connection
Directions: Disconnect the device connection and need to reconnect after disconnection.

```
EPaperSdk.connectMsgManager.disConnection ();
```

### Get device connection state
Description: Return device connection status true/false.

```
boolean isConnection =EPaperSdk.connectMsgManager.isConnection ();
```

//### Reconnect, no callback
//description: incoming mac address, reconnect to the device.
//
//```
//EPaperSdk.bleConnectDeviceMsgManager.connection (mac);
//```

### Reconnect device
Description: Disconnect the device and release the resource

```
EPaperSdk.bleConnectDeviceMsgManager.release();
```


## Send Image to device interface

Description: connect the device to send the Image to the electronic price tag, after the Image is sent, the electronic price tag shows the Image content, please make sure the battery power is more than 30% before sending.
Please transfer images that match the resolution size, for resolution requirements please see.
```
   EPaperSdk.templateManager.sendImageView(bitmap, DeviceSize.DEVICE_042);
```

#### DeviceType enumeration object description

Parameters|	Note
-|-
DEVICE_015	|1.5(200*200) inch equipment|
DEVICE_021	|2.13(250*128) inch equipment|
DEVICE_029	|2.9(296*128) inch equipment|
DEVICE_042	|4.2(400*300) inch equipment|
DEVICE_075	|7.5(800*528) inch equipment|

Image sending status callback:

```
              // Make the image black, white and red
                Bitmap bitmap = EPaperSdk.templateManager.renderingImage(mBitmap, RenderingGear.RENDERING_48);
                // Tell the SDK the image and size to send
                EPaperSdk.templateManager.sendImageView(bitmap, DeviceSize.DEVICE_042);
                //start sending and return the sending status
                EPaperSdk.templateManager.connection(mac, new BleTemplateCallback() {
                    @Override
                    public void onConnectionChange(StatusCode statusCode) {
                        if (statusCode == StatusCode.CONNECTION_START) {
                            LogHelper.i("Starting connection");
                        } else if (statusCode == StatusCode.CONNECTION_SUCCESS) {
                            LogHelper.i("Connection completed");
                        } else if (statusCode == StatusCode.TEMPLATE_START_SEND) {
                            LogHelper.i("Starting to send image");
                        } else if (statusCode == StatusCode.TEMPLATE_SEND_LOADING) {
                            LogHelper.i("Sending image");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Sending image", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else if (statusCode == StatusCode.TEMPLATE_SEND_SUCCESS) {
                            LogHelper.i("Sending image successfully");
                        } else if (statusCode == StatusCode.TEMPLATE_REFRESH_DEVICE) {
                            LogHelper.i("Refreshing image, waiting 15 seconds");
                        } else if (statusCode == StatusCode.TEMPLATE_REFRESH_DEVICE_SUCCESS) {
                            LogHelper.i("Image refresh complete");
                        }
                    }

                    @Override
                    public void onConnectionError(ErrorCode errorCode) {
                        if (errorCode == ErrorCode.ERROR_BLE_CONNECTION_TIMEOUT) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Connection timeout", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else if (errorCode == ErrorCode.ERROR_TEMPLATE_SEND_TIMEOUT) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Send image timeout", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Send successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
```


#### The state StatusCode callback in the void onConnectionChange (statusCode) method


State code|	Note
-|-
CONNECTION_START	|Connection equipment|
CONNECTION_SUCCESS|	Device connection successfully|
IMAGE_START_SEND	|Start sending Image  to the device|
IMAGE_SEND_LOADING|	Is sending a Image  to the device|
IMAGE_SEND_SUCCESS|	Send Image pictures to device successfully|
IMAGE_REFRESH_DEVICE|	Refresh electronic price tag screen content|
IMAGE_REFRESH_DEVICE_SUCCESS	|Electronic price tag|


#### The state ErrorCode callback in the void onConnectionError (errorCode) method

State code|	Note
-|-
ERROR_BLE_CONNECTION_TIMEOUT|	Connection Device Timeout|
ERROR_IMAGE_SEND_TIMEOUT	|Send Image picture timeout|

### Status Code and Error Code
#### StatusCode status tables

State value	|Note
-|-
CONNECTION_START	|Connection equipment|
CONNECTION_SUCCESS|	Connection device successfully|
CONNECTION_GET_MSG_START	|Equipment information is being obtained|
CONNECTION_GET_MSG_SUCCESS	|Access to device information|
IMAGE_START_SEND|	Start sending Image to electronic price tag devices|
IMAGE_SEND_LOADING	|A Image is being sent to an electronic price tag device|
IMAGE_SEND_SUCCESS|	Send Image to electronic price tag device successfully|
IMAGE_REFRESH_DEVICE|	Refresh the electronic tag Image picture|
IMAGE_REFRESH_DEVICE_SUCCESS|	Refresh the electronic tag Image picture|


### ErrorCode Error Error Code Matching Table


State value	|Note|
-|-
ERROR_PURVIEW_NO_OPEN_BLE|	No Bluetooth switch on|
ERROR_PURVIEW_NO_OPEN_LOCATION|	No positioning switch on|
 ERROR_BLE_CONNECTION_TIMEOUT|	Connection Device Timeout|
ERROR_CONNECTION_GET_DEVICE_MSG_TIMEOUT|	Gets device information timeout|
ERROR_IMAGE_SEND_TIMEOUT	|Send  Image to electronic price tag device timeout|



















