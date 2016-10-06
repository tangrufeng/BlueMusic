package com.xhk.bluemusic;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Set;

public class BluetoothListActivity extends Activity {
    private final static String TAG = BluetoothListActivity.class.getSimpleName();

    private BluetoothAdapter adapter;
    private ListView lv;
    private String targetDevName = "YueJiaJingLing";
    private int flag = -1;
    private BluetoothProfile mBPro;

    private boolean needHandset = true;
    private BluetoothProfile.ServiceListener serviceListener;

    private BluetoothProfile proxy=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled()) {
            adapter.enable();
        }

        int a2dp = adapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int headset = adapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int health = adapter.getProfileConnectionState(BluetoothProfile.HEALTH);

        if (a2dp == BluetoothProfile.STATE_CONNECTED) {
            flag = a2dp;
        } else if (headset == BluetoothProfile.STATE_CONNECTED) {
            flag = headset;
        } else if (health == BluetoothProfile.STATE_CONNECTED) {
            flag = health;
        }

        if (flag != -1) {
            serviceListener = new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceDisconnected(int profile) {
                }

                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    BluetoothListActivity.this.proxy=proxy;
                    List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                    if (mDevices != null && mDevices.size() > 0) {
                        for (BluetoothDevice device : mDevices) {
                            Log.i("W", "device name:" + device.getName() + "device mac" + device.getAddress());
                            if (targetDevName.equals(device.getName())) {
                                needHandset = false;
                                Intent it = new Intent(BluetoothListActivity.this, MainActivity.class);
                                it.putExtra("SELECT_DEV", device);
                                BluetoothListActivity.this.startActivity(it);
                                break;
                            }
                        }
                    } else {
                        Log.i("W", "mDevices is null");
                    }
                }
            };
            adapter.getProfileProxy(BluetoothListActivity.this, serviceListener, flag);
        }
        if (needHandset) {
            setContentView(R.layout.activity_bluetooth_list);
            lv = (ListView) findViewById(R.id.lv_devs);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (needHandset) {
            findDevs();
        }
    }

    private void findDevs() {
        Set<BluetoothDevice> devs = adapter.getBondedDevices();
        if (devs != null && devs.size() > 0) {
            BluetoothDevice[] arrDev = new BluetoothDevice[devs.size()];
            DevAdapter adapter = new DevAdapter(this, 0, devs.toArray(arrDev));
            lv.setAdapter(adapter);

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("未找到已配对的蓝牙设备，请确认后重试").setPositiveButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    BluetoothListActivity.this.finish();
                }
            }).setNegativeButton("重试", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    findDevs();
                }
            }).show();
        }
    }

    private class DevAdapter extends ArrayAdapter<BluetoothDevice> {

        ViewHolder vHolder = null;
        private Context ctx;

        public DevAdapter(Context context, int resource, BluetoothDevice[] objects) {
            super(context, resource, objects);
            this.ctx = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(ctx, R.layout.lv_items, null);
                vHolder = new ViewHolder();
                vHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
                vHolder.tvAddr = (TextView) convertView.findViewById(R.id.tvAddr);
                vHolder.tvState = (TextView) convertView.findViewById(R.id.tvState);
                convertView.setTag(vHolder);
            } else {
                vHolder = (ViewHolder) convertView.getTag();
            }
            final BluetoothDevice dev = getItem(position);
            vHolder.tvName.setText(dev.getName());
            vHolder.tvAddr.setText(dev.getAddress());
            switch (dev.getBondState()) {
                case BluetoothDevice.BOND_BONDED:
                    vHolder.tvState.setText("已配对");
                    break;
                case BluetoothDevice.BOND_BONDING:
                    vHolder.tvState.setText("配对中");
                    break;
                case BluetoothDevice.BOND_NONE:
                    vHolder.tvState.setText("未配对");
                    break;
            }
            convertView.setClickable(true);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dev.getBondState() != BluetoothDevice.BOND_BONDED) {
                        Toast.makeText(ctx, "还未与该蓝牙配对，请先配对", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent it = new Intent(ctx, MainActivity.class);
                        it.putExtra("SELECT_DEV", dev);
                        ctx.startActivity(it);
                    }
                }
            });
            return convertView;
        }

        class ViewHolder {
            TextView tvName, tvAddr, tvState;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.closeProfileProxy(flag, proxy);
    }
}
