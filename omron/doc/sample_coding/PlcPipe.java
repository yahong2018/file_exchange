package com.zhxh.imms.mes.egr.service;

import com.zhxh.imms.mes.egr.setting.domain.PLC;

public abstract class PlcPipe {
    protected TcpClient tcpClient;
    protected PLC plc;
    private boolean plcConnected;

    public PLC getPlc() {
        return plc;
    }

    public void setPlc(PLC plc) {
        this.plc = plc;
    }

    public PlcPipe() {
    }

    public PlcPipe(PLC plc) {
        this.plc = plc;
        this.tcpClient = new TcpClient(plc.getIp(), plc.getPort());
    }

    public boolean isConnected() {
        return plcConnected;
    }

    public boolean connect() {
        if (this.tcpClient == null) {
            this.tcpClient = new TcpClient(plc.getIp(), plc.getPort());
        }

        if (!this.isConnected() || !this.tcpClient.isConnected()) {
            if (!this.tcpClient.connect()) {
                return false;
            }
            this.plcConnected = this.connectToPlc();
        }
        return this.plcConnected;
    }

    protected abstract boolean connectToPlc();

    public void disConnect() {
        if (plcConnected) {
            this.disConnectFromPlc();
        }

        this.tcpClient.disConnect();
        this.plcConnected = false;
    }

    private void disConnectFromPlc() {
    }

    public abstract boolean readBoolean(String addr);

    public abstract String readString(String addr, int length);

    public abstract int readInt(String addr, int length);

    public abstract long readLong(String addr, int length);

    public abstract float readFloat(String addr, int length);

    public abstract double readDouble(String addr, int length);

    public abstract int rawRead(String addr, int length, byte[] resultBuffer, int readType);

    public abstract byte[] readWord(String addr, int length);

    public abstract byte readBit(String addr);

    public final static int READ_TYPE_BIT = 1;
    public final static int READ_TYPE_WORD = 0;
}
