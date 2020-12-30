package com.witstec.epaper;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.witstec.ble.tagsdk.bean.DeviceSize;
import com.witstec.ble.tagsdk.bean.ErrorCode;
import com.witstec.ble.tagsdk.bean.RenderingGear;
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

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    //    String mac = "12:9A:04:01:4D:08";
    String mac = "12:9A:04:01:43:08";
    Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Apply for permission
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
                        // permission request failed
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        //Permission application is successful
                        //check if location is enabled
                        if (ScanUtil.isLocationEnabled(MainActivity.this)) {
                            //get the Bluetooth manager
                            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            //check if Bluetooth is on or not
                            if (!mBluetoothAdapter.isEnabled()) {
                                // If not, request to turn on Bluetooth
                                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableBtIntent, 1);
                            } else {
                                // Bluetooth operation
                            }
                        } else {
                            //go to open the location settings interface
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                        }
                    }
                }).start();
        setListener();
    }

    private void setListener() {
        Button scanning = findViewById(R.id.btn_scanning);
        Button connection = findViewById(R.id.btn_connection);
        Button btn_connection_device_info = findViewById(R.id.btn_connection_device_info);
        Button btn_send_image = findViewById(R.id.btn_send_image);
        final ImageView btn_imageView = findViewById(R.id.btn_imageView);
        try {
            mBitmap = BitmapFactory.decodeStream(getResources().getAssets().open("test3.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        btn_imageView.setImageBitmap(mBitmap);

        /**
         * Get the list of Bluetooth devices
         */
        scanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start scanning
                EPaperSdk.BleScanManager.startScanNow();
                //Get Scan Results
                EPaperSdk.BleScanManager.getScanResult(new BleScanCallbackCompat() {
                    @Override
                    public void onScanResult(List<ScanData> deviceList) {
                        //Print the acquired device information
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

        //Simply connected devices
        connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mac.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please scan the Bluetooth first", Toast.LENGTH_SHORT).show();
                }
                //Stop scanning
//                EPaperSdk.BleScanManager.stopCycleScan();

                //Single Scan Device Reply Notification Callback
                EPaperSdk.bleConnectionManager.connection(mac, new BleConnectCallback() {

                    @Override
                    public void onConnectionChange(StatusCode statusCode) {
                        if (statusCode == StatusCode.CONNECTION_START) {
                            LogHelper.i("Start Connection");
                        } else if (statusCode == StatusCode.CONNECTION_SUCCESS) {
                            LogHelper.i("Connection completed");
                        }
                    }

                    @Override
                    public void onConnectionError(ErrorCode errorCode) {
                        if (errorCode == ErrorCode.ERROR_BLE_CONNECTION_TIMEOUT) {
                            LogHelper.i("Connection timeout");
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
            }
        });

        //Connect the device and get the device information
        btn_connection_device_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mac.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please scan the Bluetooth first", Toast.LENGTH_SHORT).show();
                }
                //Stop scanning
//                EPaperSdk.BleScanManager.stopCycleScan();
           //scan plus get device information No device notification callback
                EPaperSdk.connectMsgManager.connection(mac, new BleConnectionDeviceInfoCallback() {
                    @Override
                    public void onConnectionChange(StatusCode statusCode) {
                        if (statusCode == StatusCode.CONNECTION_START) {
                            LogHelper.i("Starting connection");
                        } else if (statusCode == StatusCode.CONNECTION_SUCCESS) {
                            LogHelper.i("Connection completed");
                        } else if (statusCode == StatusCode.CONNECTION_GET_MSG_START) {
                            LogHelper.i("Getting device information");
                        } else if (statusCode == StatusCode.CONNECTION_GET_MSG_SUCCESS) {
                            LogHelper.i("Fetching device information completed");
                        }
                    }

                    @Override
                    public void onConnectionError(ErrorCode errorCode) {
                        if (errorCode == ErrorCode.ERROR_BLE_CONNECTION_TIMEOUT) {
                            LogHelper.i("Connection timeout");
                        } else if (errorCode == ErrorCode.ERROR_CONNECTION_GET_DEVICE_MSG_TIMEOUT) {
                            LogHelper.i("Failed to obtain device information");
                        }
                    }

                    @Override
                    public void onConnectionSuccess(DeviceInfo msg) {
                        LogHelper.i("Device power" + msg.getPower());
                        LogHelper.i("Device address" + msg.getAddress());
                        LogHelper.i("Device Version" + msg.getVersion());
                    }
                });

//                EPaperSdk.connectMsgManager.disConnection();
//                boolean isConnection = EPaperSdk.connectMsgManager.isConnection();
//                EPaperSdk.connectMsgManager.connection(mac);
            }
        });

        // connect to the device and send the image to the device
        btn_send_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
//                EPaperSdk.templateManager.disConnection();
//                boolean isConnection = EPaperSdk.templateManager.isConnection();
//                EPaperSdk.templateManager.connection(mac);
//                EPaperSdk.templateManager.reconnect(mac);
            }
        });


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EPaperSdk.templateManager.disConnection();
    }

}
