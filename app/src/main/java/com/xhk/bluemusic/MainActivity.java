package com.xhk.bluemusic;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xhk.bluetooth.BluetoothTools;
import com.xhk.bluetooth.OnBluetoothConnectListener;
import com.xhk.bluetooth.Utils;

import java.io.IOException;

public class MainActivity extends Activity implements OnBluetoothConnectListener {
    private final static String TAG = MainActivity.class.getSimpleName();
    BluetoothTools bUtils = null;
    TextView tv = null;
    Button btnTip, btnAuto;
    SeekBar seekbar = null;
    //    ImageButton ibUp, ibDown;
    boolean isAuto, hasTip;
    int currentFrequency;
    boolean hasConnect = false;

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    currentFrequency = msg.arg1;
                    seekbar.setProgress(getProgress(currentFrequency));
                    String tmp = Utils.formatFrequency(currentFrequency);
                    tv.setText(tmp);
                    break;
                case 2:
                    isAuto = msg.arg1 == 1;
                    Log.d(TAG, "isAuto ===msg.arg1===>" + msg.arg1);
                    btnAuto.setEnabled(true);
                    if (isAuto) {
                        btnAuto.setText("点击关闭");
                        Toast.makeText(MainActivity.this, "自动播放已开启....", Toast.LENGTH_SHORT).show();
                    } else {
                        btnAuto.setText("点击打开");
                        Toast.makeText(MainActivity.this, "自动播放已关闭....", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 3:
                    hasTip = msg.arg1 == 1;
                    Log.d(TAG, "hasTip ===msg.arg1===>" + msg.arg1);
                    btnTip.setEnabled(true);
                    if (hasTip) {
                        btnTip.setText("点击关闭");
                        Toast.makeText(MainActivity.this, "提示音已开启....", Toast.LENGTH_SHORT).show();
                    } else {
                        btnTip.setText("点击打开");
                        Toast.makeText(MainActivity.this, "提示音已关闭....", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 4:
                    Toast.makeText(MainActivity.this, "蓝牙连接失败....", Toast.LENGTH_SHORT).show();
//                    Intent it=new Intent(MainActivity.this,BluetoothListActivity.class);
//                    MainActivity.this.startActivity(it);
//                    MainActivity.this.finish();
                    break;
                case 5:
                    Toast.makeText(MainActivity.this, "蓝牙连接成功....", Toast.LENGTH_SHORT).show();
                    initDate();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv_fm);
        btnTip = (Button) findViewById(R.id.btnTip);
        btnAuto = (Button) findViewById(R.id.btnAuto);
        seekbar = (SeekBar) findViewById(R.id.seekbar);

//        ibUp = (ImageButton) findViewById(R.id.btnUp);
//        ibDown = (ImageButton) findViewById(R.id.btnDown);

        initView();
        final BluetoothDevice dev = getIntent().getParcelableExtra("SELECT_DEV");//adapter.getRemoteDevice("00:E0:4C:D9:87:52");//00:E0:4C:97:BA:DD

        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        int state=adapter.getProfileConnectionState(BluetoothProfile.A2DP);
//        if(state==BluetoothProfile.STATE_CONNECTED) {
            bUtils = new BluetoothTools(dev);
            bUtils.openConnection();
            bUtils.setOnBluetoothConnectListener(this);
            bUtils.setRepeatCnt(2);
            bUtils.setWaitTime(500l);
//        }else{
//            Toast.makeText(this,"蓝牙没有处于连接状态,请确认后重试",Toast.LENGTH_SHORT).show();
//        }
    }

    private void initDate() {
        getFM();
        getAuto();
        getTip();
    }

    private void initView() {
//        ibDown.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                setFM(false);
//            }
//        });
//        ibUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                setFM(true);
//            }
//        });
//        tv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if (!TextUtils.isEmpty(tv.getText())) {
//                    int temp = Utils.intOfFrequency(tv.getText().toString());
//                    if (temp >= BluetoothTools.MIN_FREQUENCY && temp <= BluetoothTools.MAX_FREQUENCY) {
//                        Log.d(TAG, "Text value======>" + temp);
//                        setFM(temp);
//                    }
//                }
//            }
//        });


        seekbar.setMax(100);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                Log.d(TAG, "onTouch===>" + seekbar.getProgress());
//                int fre = BluetoothTools.MIN_FREQUENCY + (BluetoothTools.MAX_FREQUENCY - BluetoothTools.MIN_FREQUENCY) * (seekbar.getProgress() / 100);
//                setFM(fre);
            }
        });
        seekbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    int temp = BluetoothTools.MIN_FREQUENCY + ((BluetoothTools.MAX_FREQUENCY - BluetoothTools.MIN_FREQUENCY) * seekbar.getProgress()) / 100;
                    Log.d(TAG, seekbar.getProgress() + "onTouch===>" + ((BluetoothTools.MAX_FREQUENCY - BluetoothTools.MIN_FREQUENCY) * seekbar.getProgress()) / 1000 + " fre::" + temp);
                    temp=Math.round(temp/5)*5;
                    setFM(temp);
                }
                return false;
            }
        });
        btnTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnTip.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (hasConnect) {
                            bUtils.setMusicTip(!hasTip);
                            getTip();
                        }
                    }
                }).start();
            }
        });
        btnAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnAuto.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (hasConnect) {
                            bUtils.setAutoPlay(!isAuto);
                            getAuto();
                        }
                    }
                }).start();
            }
        });
    }

    private void getFM() {
        if (hasConnect) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int fm = bUtils.getFMFrequency();
                    Message msg = mHandler.obtainMessage();
                    msg.arg1 = fm;
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                }
            }).start();
        }
    }

    private void setFM(final int frequency) {

        if (!hasConnect) {
            return;
        }
        if (frequency < BluetoothTools.MIN_FREQUENCY || frequency > BluetoothTools.MAX_FREQUENCY) {
            Toast.makeText(this, "收音机频率必须在87.50到108.00之间", Toast.LENGTH_SHORT).show();
            return;
        }

        Message msg = mHandler.obtainMessage();
        msg.arg1 = frequency;
        msg.what = 1;
        mHandler.sendMessage(msg);
        new Thread(new Runnable() {
            @Override
            public void run() {

                bUtils.setFMFrequency(frequency);

//                Message msg = mHandler.obtainMessage();
//                msg.arg1 =getFM();;
//                msg.what = 1;
//                mHandler.sendMessage(msg);
            }
        }).start();
    }

    private void setFM(final boolean isUp) {
        Log.d(TAG, "currentFrequency===>" + currentFrequency);
        if (currentFrequency == 0) {
            return;
        }
        int frequency = isUp ? ++currentFrequency : --currentFrequency;
        setFM(frequency);
    }

    private void getAuto() {
        if (hasConnect) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean temp = bUtils.isAutoPlay();
                    Message msg = mHandler.obtainMessage();
                    msg.arg1 = temp ? 1 : 0;
                    msg.what = 2;
                    mHandler.sendMessage(msg);
                }
            }).start();
        }
    }

    private void getTip() {
        if (hasConnect) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean temp = bUtils.hasMusicTip();
                    Message msg = mHandler.obtainMessage();
                    msg.arg1 = temp ? 1 : 0;
                    msg.what = 3;
                    mHandler.sendMessage(msg);
                }
            }).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bUtils!=null) {
            bUtils.closeConnection();
        }

    }

    @Override
    public void onConnectSuccess(BluetoothDevice dev) {
        hasConnect = true;
        mHandler.sendEmptyMessage(5);
    }

    @Override
    public void onConnectFailed(BluetoothDevice dev) {
        hasConnect = false;
        mHandler.sendEmptyMessage(4);
    }

    private int getProgress(int fre) {
        float pro = ((float) fre - BluetoothTools.MIN_FREQUENCY) / (BluetoothTools.MAX_FREQUENCY - BluetoothTools.MIN_FREQUENCY);
        Log.d(TAG, "getProgress(int fre) ==>" + fre + "==>" + pro);
        return (int) (pro * 100);
    }
}
