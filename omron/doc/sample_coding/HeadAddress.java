package com.zhxh.imms.mes.egr.service;

import org.apache.commons.lang3.StringUtils;

public class HeadAddress {
    private String areaCode;
    private int channel;
    private byte bit;

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public byte getBit() {
        return bit;
    }

    public void setBit(byte bit) {
        this.bit = bit;
    }

    public HeadAddress() {
    }

    public HeadAddress(String fullAddr) {
        String[] array = StringUtils.split(fullAddr,".");
        this.areaCode = array[0];
        this.channel = Integer.parseInt(array[1]);
        if (array.length > 2) {
            this.bit = Byte.parseByte(array[2]);
        } else {
            this.bit = 0;
        }
    }

    public HeadAddress(String areaCode, short channel, byte bit) {
        this.areaCode = areaCode;
        this.channel = channel;
        this.bit = bit;
    }

    public byte getAreaCodeWord() {
        switch (this.areaCode) {
            case "CIO":
                return (byte) 0xB0;
            case "DM":
                return (byte) 0x82;
            case "WR":
                return (byte) 0xB1;
            case "HR":
                return (byte) 0xB2;
            case "AR":
                return (byte) 0xB3;
        }

        return 0x00;
    }

    public byte getAreaCodeBit() {
        switch (this.areaCode) {
            case "CIO":
                return (byte) 0x30;
            case "DM":
                return (byte) 0x02;
            case "WR":
                return (byte) 0x31;
            case "HR":
                return (byte) 0x32;
            case "AR":
                return (byte) 0x33;
        }

        return 0x00;
    }
}
