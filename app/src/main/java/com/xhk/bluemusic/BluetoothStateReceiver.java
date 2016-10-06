package com.xhk.bluemusic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

public class BluetoothStateReceiver extends BroadcastReceiver {

    private final static String TAG=BluetoothStateReceiver.class.getName();

    private BluetoothAdapter adapter;
    private String targetDevName = "YueJiaJingLing";
    private int flag = -1;
    private BluetoothProfile mBPro;

    private BluetoothProfile.ServiceListener serviceListener;

    public BluetoothStateReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG,"intent.getAction===>"+intent.getAction());
        if(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())){

            String stateExtra = BluetoothAdapter.EXTRA_CONNECTION_STATE;
            int state = intent.getIntExtra(stateExtra, -1);
            Log.d(TAG,"stateExtra===>"+state);
            Log.d(TAG,"intent====>"+intent);

            BluetoothDevice dev=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            Log.d(TAG,"BluetoothDevice====>"+dev+" name====>"+dev.getName());
            switch(state) {
                case BluetoothAdapter.STATE_CONNECTING:
                    break;
                case BluetoothAdapter.STATE_CONNECTED:
                    if(dev.getName().equals(targetDevName)){
                        try {
                            Thread.sleep(1000l);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Intent it = new Intent(context, BluetoothListActivity.class);
//                        it.putExtra("SELECT_DEV", dev);
                        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(it);

                    }
//                    adapter = BluetoothAdapter.getDefaultAdapter();
//                    if (!adapter.isEnabled()) {
//                        adapter.enable();
//                    }
//
//                    int a2dp = adapter.getProfileConnectionState(BluetoothProfile.A2DP);
//                    int headset = adapter.getProfileConnectionState(BluetoothProfile.HEADSET);
//                    int health = adapter.getProfileConnectionState(BluetoothProfile.HEALTH);
//
//                    if (a2dp == BluetoothProfile.STATE_CONNECTED) {
//                        flag = a2dp;
//                    } else if (headset == BluetoothProfile.STATE_CONNECTED) {
//                        flag = headset;
//                    } else if (health == BluetoothProfile.STATE_CONNECTED) {
//                        flag = health;
//                    }
//
//                    if (flag != -1) {
//                        serviceListener = new BluetoothProfile.ServiceListener() {
//                            @Override
//                            public void onServiceDisconnected(int profile) {
//                            }
//
//                            @Override
//                            public void onServiceConnected(int profile, BluetoothProfile proxy) {
//                                List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
//                                if (mDevices != null && mDevices.size() > 0) {
//                                    for (BluetoothDevice device : mDevices) {
//                                        Log.i(TAG, "device name:" + device.getName() + "device mac" + device.getAddress());
//                                        if (targetDevName.equals(device.getName())) {
//                                            Intent it = new Intent(context, MainActivity.class);
//                                            it.putExtra("SELECT_DEV", device);
//                                            context.startActivity(it);
//                                            break;
//                                        }
//                                    }
//                                } else {
//                                    Log.i(TAG, "mDevices is null");
//                                }
//                            }
//                        };
//                        adapter.getProfileProxy(context, serviceListener, flag);
//                    }
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    break;
                case BluetoothAdapter.STATE_DISCONNECTING:
                    break;
            }
        }
    }


}
