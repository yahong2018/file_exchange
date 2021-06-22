package com.zhxh.imms.utils;

public class ByteUtil {
    private static final char[] HEXES = { 
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
        'a', 'b', 'c', 'd', 'e','f' 
    };

    public static String bytesToHex(byte[] bytes,int length) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        StringBuilder hex = new StringBuilder();
        for(int i=0;i<bytes.length && i<length;i++){
            byte b = bytes[i];
            hex.append(HEXES[(b >> 4) & 0x0F]).append("-");
            hex.append(HEXES[b & 0x0F]);
        }
       
        return hex.toString();
    }

    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, bytes.length);
    }

    public static byte[] hex2Bytes(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }

        char[] hexChars = hex.toCharArray();
        byte[] bytes = new byte[hexChars.length / 2];   // 如果 hex 中的字符不是偶数个, 则忽略最后一个

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt("" + hexChars[i * 2] + hexChars[i * 2 + 1], 16);
        }

        return bytes;
    }
}
