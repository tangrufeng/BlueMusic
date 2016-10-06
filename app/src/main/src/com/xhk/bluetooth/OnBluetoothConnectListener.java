package com.xhk.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * 蓝牙连接状态监听，因为蓝牙连接操作是异步执行，所以强烈建议使用该监听器，确保获取最终蓝牙连接状态
 * Created by tang on 2016/4/27.
 */
public interface OnBluetoothConnectListener {
    /**
     * 蓝牙连接成功时的回调方法
     */
    public void onConnectSuccess(BluetoothDevice dev);

    /**
     * 蓝牙连接失败时的回调方法
     */
    public void onConnectFailed(BluetoothDevice dev);
}
