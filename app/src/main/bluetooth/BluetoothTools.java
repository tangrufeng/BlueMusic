package com.xhk.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 封装蓝牙设备的相关操作，包括建立蓝牙连接，设置收音机频率、操作提示音开关、自动播放开关等操作，以及读取对应状态<br>
 * 使用该类时，需要先通过已经配对成功的蓝牙设备对象来实例化该类，设置蓝牙连接状态监听对象，然后调用该类的openConnection（异步方法）方法打开蓝牙连接。<br>
 * 建立连接后，可调用对应的方法进行蓝牙设备功能的设置和读取，这些方法不能放在主线程中调用。<br>
 * 为了确保成功读取和设置数据，每次设置和读取蓝牙数据，都会重复发送三次指令。
 * <p/>
 * Created by tang on 2016/4/27.
 */
public class BluetoothTools {
    private final static String TAG = BluetoothTools.class.getSimpleName();
    private static final int CNT_RETRY = 3;
    private long waitTime = 500l;

    private String uuid = "00001101-0000-1000-8000-00805F9B34FB";

    /**
     * 蓝牙设备最小的收音机发射频率值
     */
    public final static int MIN_FREQUENCY = 8750;


    /**
     * 蓝牙设备最大的收音机发射频率值
     */
    public final static int MAX_FREQUENCY = 10800;

    private BluetoothDevice dev = null;

    BluetoothSocket socket = null;

    private com.xhk.bluetooth.OnBluetoothConnectListener listenter;
    //
    private OutputStream out = null;

    private InputStream in = null;

    private int reTryCnt = 3;

    private final Object lock = new Object();

    /**
     * 初始化BluetoothTools，指定的蓝牙设备需要已经配对
     *
     * @param dev 需要连接的蓝牙设备
     */
    public BluetoothTools(BluetoothDevice dev) {
        if (dev == null) {
            throw new XHKException("The BluetoothDevice must not be null!");
        }
        this.dev = dev;
    }

    /**
     * 设置每次指令发送时间间隔，默认为500毫秒
     *
     * @param waitTime
     */
    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    /**
     * 设置指令重试发送次数，默认为3次
     *
     * @param repeatCnt
     */
    public void setRepeatCnt(int repeatCnt) {
        this.reTryCnt = repeatCnt;
    }

    /**
     * 设置蓝牙设备连接接听器
     *
     * @param listener
     */
    public void setOnBluetoothConnectListener(com.xhk.bluetooth.OnBluetoothConnectListener listener) {
        this.listenter = listener;
    }

    /**
     * 打开蓝牙连接
     */
    public void openConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = getBluetoothSocket();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d(TAG,"socket.isConnect()====>"+socket.isConnected());
                                socket.connect();
                                out = socket.getOutputStream();
                                in = socket.getInputStream();
                                Log.d(TAG, dev.getName() + "[" + dev.getAddress() + "] connection success");
                                if (listenter != null) {
                                    listenter.onConnectSuccess();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, dev.getName() + "[" + dev.getAddress() + "] connection failed", e);
                                if (listenter != null) {
                                    listenter.onConnectFailed();
                                }
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    Log.e(TAG, dev.getName() + "[" + dev.getAddress() + "] connection failed", e);
                    if (listenter != null) {
                        listenter.onConnectFailed();
                    }
                }

            }
        }).start();

    }

    /**
     * 获取蓝牙连接的UUID
     *
     * @return
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * 设置蓝牙连接的UUID，如果不设置，默认为<code>00001101-0000-1000-8000-00805F9B34FB</code>
     *
     * @param UUID 蓝牙连接的UUID，
     */
    public void setUUID(String UUID) {
        this.uuid = UUID;
    }

    /**
     * 关闭蓝牙连接
     */
    public void closeConnection() {
        if (out != null) {
            try {
                out.close();
                out = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (in != null) {
            try {
                in.close();
                in = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 获取蓝牙设备的发送频率
     *
     * @return
     */
    public int getFMFrequency() {
        int tmp = -1;
        synchronized (out) {
            Log.d(TAG, "getFMFrequency() ===========");
            for (int i = 0; i < reTryCnt; i++) {
                tmp = getFMFrequencyOnce();
                Log.d(TAG, "getFMFrequencyOnce===>" + tmp);
                if (tmp != -1) {
                    break;
                }
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                }
            }
        }
        return tmp;
    }

    private int getFMFrequencyOnce() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new XHKException("Please don't run it in Main Thread!");
        }
        try {
            byte bytes[] = new byte[16];
            bytes[0] = 0x01;
            bytes[1] = (byte) 0xFE;
            bytes[2] = 0x00;
            bytes[3] = 0x00;
            bytes[4] = 0x51;
            bytes[5] = (byte) 0x89;
            bytes[6] = 0x10;
            bytes[7] = 0x00;
            bytes[8] = 0x00;
            bytes[9] = 0x00;
            bytes[10] = 0x00;
            bytes[11] = 0x00;
            bytes[12] = 0x00;
            bytes[13] = 0x00;
            bytes[14] = 0x00;
            bytes[15] = 0x00;

            Log.d(TAG, "req ==>" + Utils.bytes2HexString(bytes));
            out.write(bytes);
            int count = 0;
            int retry = 0;
            while (count == 0 && ++retry <= CNT_RETRY) {
                count = in.available();
                Thread.sleep(500);
            }
            byte resp[] = new byte[count];
            in.read(resp);
            Log.d(TAG, "resp==>" + Utils.bytes2HexString(resp));
            byte b[] = new byte[4];
            System.arraycopy(resp, 12, b, 0, 4);
            return Utils.byte2int(b);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
            return -1;
        } finally {
            closeAll(out, in, socket);
        }
    }

    private void closeAll(OutputStream out, InputStream in, BluetoothSocket socket) {
//        if (out != null) {
//            try {
//                out.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        if (in != null) {
//            try {
//                in.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        if (socket != null) {
//            try {
//                socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * 设置蓝牙设备发射频率，需要设置为整形，如要调整至87.5，则设置为875
     *
     * @param frequency frequency 频率值，最大为1080，最小为875
     */
    public void setFMFrequency(int frequency) {
        Log.d(TAG, "setFMFrequency===>" + frequency);
        for (int i = 0; i < reTryCnt; i++) {
            synchronized (out) {
                setFMFrequencyOnce(frequency);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * 设置硬件设备的收音机接收频率
     *
     * @param frequency 频率值，最大为10800，最小为8750
     */
    private void setFMFrequencyOnce(int frequency) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new XHKException("Please don't run it in Main Thread!");
        }

        if (frequency < MIN_FREQUENCY) {
            frequency = MIN_FREQUENCY;
        }
        if (frequency > MAX_FREQUENCY) {
            frequency = MAX_FREQUENCY;
        }

        frequency = Math.round(frequency / 5) * 5;

        byte[] freByte = Utils.int2byte(frequency);
        try {
            byte bytes[] = new byte[12];
            bytes[0] = 0x01;
            bytes[1] = (byte) 0xFE;
            bytes[2] = 0x00;
            bytes[3] = 0x00;
            bytes[4] = 0x53;
            bytes[5] = (byte) 0x89;
            bytes[6] = 0x10;
            bytes[7] = 0x00;
            bytes[8] = freByte[0];
            bytes[9] = freByte[1];
            bytes[10] = freByte[2];
            bytes[11] = freByte[3];
            Log.d(TAG, "requ==>" + Utils.bytes2HexString(bytes));
            out.write(bytes);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
        } finally {
            closeAll(out, null, socket);
        }
    }

    private void _switch(byte commond, boolean isOpen) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new XHKException("Please don't run it in Main Thread!");
        }

        BluetoothSocket socket = null;
        byte bytes[] = new byte[16];
        bytes[0] = 0x01;
        bytes[1] = (byte) 0xFE;
        bytes[2] = 0x00;
        bytes[3] = 0x00;
        bytes[4] = 0x53;
        bytes[5] = commond;
        bytes[6] = 0x10;
        bytes[7] = 0x00;
        bytes[8] = (byte) (isOpen ? 0x01 : 0x00);
        bytes[9] = 0x00;
        bytes[10] = 0x00;
        bytes[11] = 0x00;
        bytes[12] = 0x00;
        bytes[13] = 0x00;
        bytes[14] = 0x00;
        bytes[15] = 0x00;
        Log.d(TAG, "requ==>" + Utils.bytes2HexString(bytes));
        try {
            out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
        } finally {
            closeAll(out, null, socket);
        }
    }

    private int getSwitchState(byte commond) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new XHKException("Please don't run it in Main Thread!");
        }
        byte bytes[] = new byte[16];
        bytes[0] = 0x01;
        bytes[1] = (byte) 0xFE;
        bytes[2] = 0x00;
        bytes[3] = 0x00;
        bytes[4] = 0x51;
        bytes[5] = commond;
        bytes[6] = 0x10;
        bytes[7] = 0x00;
        bytes[8] = 0x00;
        bytes[9] = 0x00;
        bytes[10] = 0x00;
        bytes[11] = 0x00;
        bytes[12] = 0x00;
        bytes[13] = 0x00;
        bytes[14] = 0x00;
        bytes[15] = 0x00;

        Log.d(TAG, "req ==>" + Utils.bytes2HexString(bytes));
        try {
            out.write(bytes);
            int count = 0;
            int retry = 0;
            while (count == 0 && ++retry <= CNT_RETRY) {
                count = in.available();
                Thread.sleep(500l);
            }
            byte resp[] = new byte[count];
            in.read(resp);
            Log.d(TAG, "resp==>" + Utils.bytes2HexString(resp));
            if (resp.length < 16) {
                return -1;
            } else {
                byte b[] = new byte[4];
                System.arraycopy(resp, 12, b, 0, 4);
                return Utils.byte2int(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
            return -1;
        } finally {
            closeAll(out, in, socket);
        }
    }

    /**
     * 设置是否自动播放
     *
     * @param isAutoPlay true--开启自动播放 false--关闭自动播放
     */
    public void setAutoPlay(boolean isAutoPlay) {
        for (int i = 0; i < reTryCnt; i++) {
            synchronized (out) {
                _switch((byte) 0x87, isAutoPlay);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * 获取蓝牙设备当前的自动播放状态
     *
     * @return true--开启自动播放 false--关闭自动播放
     */
    public boolean isAutoPlay() {
        int tmp = -1;
        synchronized (out) {
            for (int i = 0; i < reTryCnt; i++) {
                tmp = getSwitchState((byte) 0x87);
                Log.d(TAG, "isAutoPlay===>" + tmp);
                if (tmp != -1) {
                    break;
                }
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                }
            }
        }
        return tmp == 1;
    }

    /**
     * 设置蓝牙设备的语言提示开关
     *
     * @param isOpen true--开启语音提示 false--关闭语音提示
     */
    public void setMusicTip(boolean isOpen) {
        for (int i = 0; i < reTryCnt; i++) {
            synchronized (out) {
                _switch((byte) 0x9d, isOpen);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * 获取蓝牙设备的语言提示状态
     *
     * @return true--开启语音提示 false--关闭语音提示
     */
    public boolean hasMusicTip() {
        int tmp = -1;
        synchronized (out) {
            for (int i = 0; i < reTryCnt; i++) {
                tmp = getSwitchState((byte) 0x9d);
                Log.d(TAG, "hasMusicTip===>" + tmp);
                if (tmp != -1) {
                    break;
                }
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                }
            }
        }
        return tmp == 1;
    }

    private BluetoothSocket getBluetoothSocket() throws IOException {
        BluetoothSocket socket = null;
        try {
            Method m = dev.getClass().getMethod("createRfcommSocketToServiceRecord", new Class[]{UUID.class});
            socket = (BluetoothSocket) m.invoke(dev, UUID.fromString(uuid));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
//        if (Build.VERSION.SDK_INT >= 10) {
//            socket = dev.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid));
//        } else {
//            socket = dev.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
//        }
//        try {
//            Method m = dev.getClass().getMethod(
//                    "createRfcommSocket", new Class[] { int.class });
//            socket = (BluetoothSocket) m.invoke(dev, 1);//这里端口为1
//        } catch (SecurityException e) {
//            e.printStackTrace();
//            Log.e(TAG, "", e);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//            Log.e(TAG, "", e);
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//            Log.e(TAG, "", e);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//            Log.e(TAG, "", e);
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//            Log.e(TAG, "", e);
//        }
        return socket;
    }


}
