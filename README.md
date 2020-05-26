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

----


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

<img src="http://api.witstec.com/Public/img/2020052601.jpg" width = 200 height = 400 />

### Json Format Description
Json field description: and the values inside are sequential, data the first data corresponds to the QR code data, the second input corresponds to the first text input box,... from top to bottom, and so on.

Json data examples

```
{
"id"："001",
"mac"："mac address ",
"data"：[{
"type"："qrcode",
"content"：" Changeable  the first input box for the value of the QR code"
},{
"type"："text",
"content"："Changeable  the value of the first text in the second input box"
},{
"type"："text",
"content"："Changeable the value of the second text in the third input box"
},{
"type"："text",
"content"："Changeable the value of the third text in the fourth input box"
},{
"type"："text",
"content"："Changeable the value of the fourth text in the fifth input box"
}]
}
```

Json parameters|	Type|	Note|
-|-|-
Id|	String	|Fixed to 001|
Mac	|String	|Equipment mac address|
type	|String|	Type of input box (" qrcode "," textqrcode ")|
context	|String|	Value of input box|


### Connection Device Send Template Picture
Description: connect the device to send the template to the electronic price tag, after the template is sent, the electronic price tag shows the template content, please make sure the battery power is more than 30% before sending.

Interface calls:

```
// Incoming parameter description: mac= device MAC address, inputStrJson mac= input box content in the format of the Json string
EPaperSdk.templateManager.connection(mac, inputStrJson, new BleTemplateCallback() {

                   // Connection Status Callback, Update Connection Status
                    @Override
                    public void onConnectionChange(StatusCode statusCode) {
                        if (statusCode == StatusCode.CONNECTION_START) {
                            LogHelper.i("Start connection");
                        } else if (statusCode == StatusCode.CONNECTION_SUCCESS) {
                            LogHelper.i("Connection completed");
                        } else if (statusCode == StatusCode.TEMPLATE_START_SEND) {
                            LogHelper.i("Start sending pictures");
                        } else if (statusCode == StatusCode.TEMPLATE_SEND_LOADING) {
                            LogHelper.i("Sending picture");
                        } else if (statusCode == StatusCode.TEMPLATE_SEND_SUCCESS) {
                            LogHelper.i("Successfully sent pictures");
                        } else if (statusCode == StatusCode.TEMPLATE_REFRESH_DEVICE) {
                            LogHelper.i("Refreshing picture, wait 15 seconds");
                        } else if (statusCode == StatusCode.TEMPLATE_REFRESH_DEVICE_SUCCESS) {
                            LogHelper.i("Image refresh completed");
                        }
                    }
                    
                    // Call when connection encounters exception, return exception error code
                    @Override
                    public void onConnectionError(ErrorCode errorCode) {
                        if (errorCode == ErrorCode.ERROR_BLE_CONNECTION_TIMEOUT) {
                            LogHelper.i("Connection Timed Out");
                        } else if (errorCode == ErrorCode.ERROR_TEMPLATE_SEND_TIMEOUT) {
                            LogHelper.i("Timeout for sending pictures");
                        }
                    }
                    
                   // After sending and refreshing the electronic price tag is called, you can save the inputStrJson data to the local,                      //facilitate the next automatic update input box content.
                    @Override
                    public void onSuccess(String inputStrJson) {
                        LogHelper.i("The picture is sent json=" + inputStrJson);
                    }
                });
```


#### The state StatusCode callback in the void onConnectionChange (statusCode) method


State code|	Note
-|-
CONNECTION_START	|Connection equipment|
CONNECTION_SUCCESS|	Device connection successfully|
TEMPLATE_START_SEND	|Start sending template images to the device|
TEMPLATE_SEND_LOADING|	Is sending a template image to the device|
TEMPLATE_SEND_SUCCESS|	Send template pictures to device successfully|
TEMPLATE_REFRESH_DEVICE|	Refresh electronic price tag screen content|
TEMPLATE_REFRESH_DEVICE_SUCCESS	|Electronic price tag|


#### The state ErrorCode callback in the void onConnectionError (errorCode) method

State code|	Note
-|-
ERROR_BLE_CONNECTION_TIMEOUT|	Connection Device Timeout|
ERROR_TEMPLATE_SEND_TIMEOUT	|Send template picture timeout|


### Refresh template images
Note: This interface is called when the value of the input box entering the sending template interface changes, and the function is to preview the template display effect

```
Bitmap bitmap=EPaperSdk.templateManager.refreshTemplate (inputStrJson);
```

 Return Bitmap, use ImageView control display, the specific use of example code as follows.
 
```
btn_imageView.setImageBitmap (EPaperSdk.bleConnectDeviceMsgManager.release5(inputStrJson));
```

### Gets the input box list
Description: This interface is called when entering the sending template interface. The function is to parse the data in json format into a collection of objects, to generate input boxes, to determine the number of input boxes, to return the collection of value objects of input boxes, and to generate an input box for one object. .

```
List<BleTemplateItemData>imageBleMsgList =EPaperSdk.bleConnectDeviceMsgManager.release4();
```

#### BleTemplateItemData Object Value Description

Request parameters	|Type	|Note
-|-|-
id|	String|	Template Id|
type|	String|	Type of input|
context|	String	|Value of text type|
qrCodeContext|	String	|Value of two-dimensional code type|


### Status Code and Error Code
#### StatusCode status tables

State value	|Note
-|-
CONNECTION_START	|Connection equipment|
CONNECTION_SUCCESS|	Connection device successfully|
CONNECTION_GET_MSG_START	|Equipment information is being obtained|
CONNECTION_GET_MSG_SUCCESS	|Access to device information|
TEMPLATE_START_SEND|	Start sending templates to electronic price tag devices|
TEMPLATE_SEND_LOADING	|A template is being sent to an electronic price tag device|
TEMPLATE_SEND_SUCCESS|	Send template to electronic price tag device successfully|
TEMPLATE_REFRESH_DEVICE|	Refresh the electronic tag template picture|
TEMPLATE_REFRESH_DEVICE_SUCCESS|	Refresh the electronic tag template picture|


### ErrorCode Error Error Code Matching Table


State value	|Note|
-|-
ERROR_PURVIEW_NO_OPEN_BLE|	No Bluetooth switch on|
ERROR_PURVIEW_NO_OPEN_LOCATION|	No positioning switch on|
 ERROR_BLE_CONNECTION_TIMEOUT|	Connection Device Timeout|
ERROR_CONNECTION_GET_DEVICE_MSG_TIMEOUT|	Gets device information timeout|
ERROR_TEMPLATE_SEND_TIMEOUT	|Send template images to electronic price tag device timeout|



















