package com.witstec.epaper;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.witstec.ble.tagsdk.EPaperSdk;
import com.witstec.ble.tagsdk.bean.BleTemplateItemData;
import com.witstec.ble.tagsdk.bean.DeviceInfo;
import com.witstec.ble.tagsdk.bean.ErrorCode;
import com.witstec.ble.tagsdk.bean.ScanData;
import com.witstec.ble.tagsdk.bean.StatusCode;
import com.witstec.ble.tagsdk.connection.BleConnectCallback;
import com.witstec.ble.tagsdk.deviceinfo.BleConnectionDeviceInfoCallback;
import com.witstec.ble.tagsdk.open.LogHelper;
import com.witstec.ble.tagsdk.scanning.BleScanCallbackCompat;
import com.witstec.ble.tagsdk.template.BleTemplateCallback;
import com.witstec.ble.tagsdk.utils.ScanUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    String mac = "57:54:0A:77:00:08";
    String inputStrJson = "{\"id\":\"123\",\"mac\":\"57:54:25:36:02\",\"data\":[{\"type\":\"qrcode\",\"content\":\"abc\"},{\"type\":\"text\",\"content\":\"aaaaaaaaaaaaaaaaaaaaa\"},{\"type\":\"text\",\"content\":\"bbbbbbbbbbbbbbbbbbb\"},{\"type\":\"text\",\"content\":\"ccccccccccccccccccccccccccccccccc\"},{\"type\":\"text\",\"content\":\"ddddddddddddddddddddddddddddddddddddddd\"}]}\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //request for access
        AndPermission.with(this)
                .runtime()
                .permission(
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        //Permission application failed
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        //Successful permission application

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
                    }
                }).start();

    }

    private void setListener() {
        Button scanning = findViewById(R.id.btn_scanning);
        Button connection = findViewById(R.id.btn_connection);
        Button btn_connection_device_info = findViewById(R.id.btn_connection_device_info);
        Button btn_send_image = findViewById(R.id.btn_send_image);
        Button btn_send_image_refresh = findViewById(R.id.btn_send_image_refresh);
        Button btn_destroy = findViewById(R.id.btn_destroy);
        final ImageView btn_imageView = findViewById(R.id.btn_imageView);

        btn_imageView.setImageBitmap(EPaperSdk.templateManager.refreshTemplate(inputStrJson));


        scanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start scanning，
                EPaperSdk.BleScanManager.startScanNow();
                //Start scanning
                EPaperSdk.BleScanManager.getScanResult(new BleScanCallbackCompat() {
                    @Override
                    public void onScanResult(List<ScanData> deviceList) {
                        //Print the obtained device information
                        for (ScanData scanData : deviceList) {
                            Log.i("scData", scanData.getAddress());
                        }
//                        mac = deviceList.get(0).getAddress();
                    }
                });

                //Stop scanning
//                EPaperSdk.BleScanManager.stopCycleScan();

            }
        });

        connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mac.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please scan Bluetooth first", Toast.LENGTH_SHORT).show();
                }
                //Stop scanning
                EPaperSdk.BleScanManager.stopCycleScan();

                //Single scan device response notification callback
                EPaperSdk.bleConnectionManager.connection(mac, new BleConnectCallback() {

                    // Connection Status Callback, Update Connection Status
                    @Override
                    public void onConnectionChange(StatusCode statusCode) {
                        if (statusCode == StatusCode.CONNECTION_START) {
                            LogHelper.i("Start to connect");
                        } else if (statusCode == StatusCode.CONNECTION_SUCCESS) {
                            LogHelper.i("Connection complete");
                        }
                    }
                // Call when connection encounters exception, return exception error code
                    @Override
                    public void onConnectionError(ErrorCode errorCode) {
                        if (errorCode == ErrorCode.ERROR_BLE_CONNECTION_TIMEOUT) {
                            LogHelper.i("Connection timed out");
                        }
                    }

                    @Override
                    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

                    }

                    @Override
                    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

                    }
                });

//                EPaperSdk.bleConnectionManager.connection(mac);
//                EPaperSdk.bleConnectionManager.disConnection();
//                EPaperSdk.bleConnectionManager.isConnection();
//                EPaperSdk.bleConnectionManager.clear();


            }
        });

        btn_connection_device_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mac.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please scan Bluetooth first", Toast.LENGTH_SHORT).show();
                }

                //Stop scanning
                EPaperSdk.BleScanManager.stopCycleScan();
                //// Scan and get device information No device notification callback
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

//                EPaperSdk.connectMsgManager.release();
//                EPaperSdk.connectMsgManager.disConnection();
//                boolean isConnection = EPaperSdk.connectMsgManager.isConnection();
//                EPaperSdk.connectMsgManager.connection(mac);
            }
        });

        btn_send_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EPaperSdk.templateManager.connection(mac, inputStrJson, new BleTemplateCallback() {
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

                    @Override
                    public void onConnectionError(ErrorCode errorCode) {
                        if (errorCode == ErrorCode.ERROR_BLE_CONNECTION_TIMEOUT) {
                            LogHelper.i("Connection Timed Out");
                        } else if (errorCode == ErrorCode.ERROR_TEMPLATE_SEND_TIMEOUT) {
                            LogHelper.i("Timeout for sending pictures");
                        }
                    }

                    @Override
                    public void onSuccess(String inputStrJson) {
                        LogHelper.i("The picture is sent json=" + inputStrJson);
                    }
                });

//                EPaperSdk.templateManager.release();
//                EPaperSdk.templateManager.disConnection();
//                EPaperSdk.templateManager.isConnection();
//                EPaperSdk.templateManager.documentParsingList();
//                EPaperSdk.templateManager.connection(mac);
//                EPaperSdk.templateManager.refreshTemplate(inputStrJson);

            }
        });

        btn_send_image_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                btn_imageView.setImageBitmap(EPaperSdk.templateManager.refreshTemplate(inputStrJson));
                btn_imageView.setImageBitmap(EPaperSdk.templateManager.refreshTemplate());
            }
        });

        btn_destroy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                EPaperSdk.destroy();
                List<BleTemplateItemData> imageBleMsgList = EPaperSdk.templateManager.documentParsingList();
                for (BleTemplateItemData imageBleMsg : imageBleMsgList) {
                    LogHelper.i("data=" + imageBleMsg.getId());
                    LogHelper.i("data=" + imageBleMsg.getContext());
                    LogHelper.i("data=" + imageBleMsg.getType());
                    LogHelper.i("data=" + imageBleMsg.getQrCodeContext());
                }
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
