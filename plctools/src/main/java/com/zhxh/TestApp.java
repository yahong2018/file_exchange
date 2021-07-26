package com.zhxh;
import com.zhxh.imms.utils.ByteUtil;

public class TestApp 
{
    public static void main( String[] args )
    {
        byte[] buffer = new byte[]{0x00,0x00,(byte)(0xc2),0x00};
        int intValue =  ByteUtil.bytes2Int(buffer);
        System.out.println(String.format("the result is :%d",intValue)); 
    }
}
