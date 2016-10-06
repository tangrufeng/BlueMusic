package com.xhk.bluetooth;

import java.text.DecimalFormat;

/**
 * 相关的工具方法
 * Created by tang on 2016/4/27.
 */
public class Utils {

    /**
     * 字节数组转16进制字符串
     * @param b
     * @return
     */
    public static String bytes2HexString(byte[] b) {
        StringBuffer result = new StringBuffer();
        String hex;
        for (int i = 0; i < b.length; i++) {
            hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result.append(hex.toUpperCase());
        }
        return result.toString();
    }

    /**
     * int转字节
     *
     * @return
     */
    public static byte[] int2byte(int num) {
         byte[] targets = new byte[4];

         targets[0] = (byte) (num & 0xff);// 最低位
         targets[1] = (byte) ((num >> 8) & 0xff);// 次低位
         targets[2] = (byte) ((num >> 16) & 0xff);// 次高位
         targets[3] = (byte) (num >>> 24);// 最高位,无符号右移。
         return targets;

    }

    /**
     * 将整型频率值转为格式化频率值，如875，返回87.5
     * @param frequency
     * @return
     */
    public static String formatFrequency(int frequency){
        DecimalFormat df=new DecimalFormat("#.00");
        return df.format(((float)frequency/100.00));
    }

    /**
     * 将格式化频率值转为整型频率值，如87.5，返回875
     * @param frequency
     * @return
     */
    public static int intOfFrequency(String frequency){
        frequency=frequency.replace(".","");
        return Integer.parseInt(frequency);
    }

     /**
     * 字节数组转int
     *
     * @param res
     * @return
     */
    public static int byte2int(byte[] res) {

        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示按位或
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
        return targets;
    }

    public static void main(String args[]){
        Utils.bytes2HexString(int2byte(899));
    }
}
