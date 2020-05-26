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

        //申请权限
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
                        //权限申请失败
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        //权限申请成功

                        //检查定位是否开启
                        if (ScanUtil.isLocationEnabled(MainActivity.this)) {
                            //获取蓝牙管理器
                            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            //检查蓝牙是否打开
                            if (!mBluetoothAdapter.isEnabled()) {
                                // 若未打开，则请求打开蓝牙
                                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableBtIntent, 1);
                            } else {
                                //蓝牙操作
                                setListener();
                            }
                        } else {
                            //进入打开位置设置界面
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
                //开始扫描，
                EPaperSdk.BleScanManager.startScanNow();
                //获取扫描结果
                EPaperSdk.BleScanManager.getScanResult(new BleScanCallbackCompat() {
                    @Override
                    public void onScanResult(List<ScanData> deviceList) {
                        //打印获取的设备信息
                        for (ScanData scanData : deviceList) {
                            Log.i("scData", scanData.getAddress());
                        }
//                        mac = deviceList.get(0).getAddress();
                    }
                });

                //停止扫描
//                EPaperSdk.BleScanManager.stopCycleScan();

            }
        });

        connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mac.isEmpty()) {
                    Toast.makeText(MainActivity.this, "请先扫描蓝牙", Toast.LENGTH_SHORT).show();
                }
                //停止扫描
                EPaperSdk.BleScanManager.stopCycleScan();

                //单扫描  设备回复通知回调
                EPaperSdk.bleConnectionManager.connection(mac, new BleConnectCallback() {

                    @Override
                    public void onConnectionChange(StatusCode statusCode) {
                        if (statusCode == StatusCode.CONNECTION_START) {
                            LogHelper.i("开始连接");
                        } else if (statusCode == StatusCode.CONNECTION_SUCCESS) {
                            LogHelper.i("连接完成");
                        }
                    }

                    @Override
                    public void onConnectionError(ErrorCode errorCode) {
                        if (errorCode == ErrorCode.ERROR_BLE_CONNECTION_TIMEOUT) {
                            LogHelper.i("连接超时");
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
                    Toast.makeText(MainActivity.this, "请先扫描蓝牙", Toast.LENGTH_SHORT).show();
                }

                //停止扫描
                EPaperSdk.BleScanManager.stopCycleScan();
                //扫描加获取设备信息  无设备通知回调
                EPaperSdk.connectMsgManager.connection(mac, new BleConnectionDeviceInfoCallback() {
                    @Override
                    public void onConnectionChange(StatusCode statusCode) {
                        if (statusCode == StatusCode.CONNECTION_START) {
                            LogHelper.i("开始连接");
                        } else if (statusCode == StatusCode.CONNECTION_SUCCESS) {
                            LogHelper.i("连接完成");
                        } else if (statusCode == StatusCode.CONNECTION_GET_MSG_START) {
                            LogHelper.i("正在获取设备信息");
                        } else if (statusCode == StatusCode.CONNECTION_GET_MSG_SUCCESS) {
                            LogHelper.i("获取设备信息完成");
                        }
                    }

                    @Override
                    public void onConnectionError(ErrorCode errorCode) {
                        if (errorCode == ErrorCode.ERROR_BLE_CONNECTION_TIMEOUT) {
                            LogHelper.i("连接超时");
                        } else if (errorCode == ErrorCode.ERROR_CONNECTION_GET_DEVICE_MSG_TIMEOUT) {
                            LogHelper.i("获取设备信息失败");
                        }
                    }

                    @Override
                    public void onConnectionSuccess(DeviceInfo msg) {
                        LogHelper.i("设备电量" + msg.getPower());
                        LogHelper.i("设备地址" + msg.getAddress());
                        LogHelper.i("设备版本" + msg.getVersion());
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
                            LogHelper.i("开始连接");
                        } else if (statusCode == StatusCode.CONNECTION_SUCCESS) {
                            LogHelper.i("连接完成");
                        } else if (statusCode == StatusCode.TEMPLATE_START_SEND) {
                            LogHelper.i("开始发送图片");
                        } else if (statusCode == StatusCode.TEMPLATE_SEND_LOADING) {
                            LogHelper.i("正在发送图片");
                        } else if (statusCode == StatusCode.TEMPLATE_SEND_SUCCESS) {
                            LogHelper.i("发送图片成功");
                        } else if (statusCode == StatusCode.TEMPLATE_REFRESH_DEVICE) {
                            LogHelper.i("正在刷新图片，等待15秒");
                        } else if (statusCode == StatusCode.TEMPLATE_REFRESH_DEVICE_SUCCESS) {
                            LogHelper.i("图片刷新完成");
                        }
                    }

                    @Override
                    public void onConnectionError(ErrorCode errorCode) {
                        if (errorCode == ErrorCode.ERROR_BLE_CONNECTION_TIMEOUT) {
                            LogHelper.i("连接超时");
                        } else if (errorCode == ErrorCode.ERROR_TEMPLATE_SEND_TIMEOUT) {
                            LogHelper.i("发送图片超时");
                        }
                    }

                    @Override
                    public void onSuccess(String inputStrJson) {
                        LogHelper.i("图片发送完成 json=" + inputStrJson);
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
                    LogHelper.i("数据=" + imageBleMsg.getId());
                    LogHelper.i("数据=" + imageBleMsg.getContext());
                    LogHelper.i("数据=" + imageBleMsg.getType());
                    LogHelper.i("数据=" + imageBleMsg.getQrCodeContext());
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
