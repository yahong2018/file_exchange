package com.zhxh.imms.mes.egr.service;

import com.zhxh.imms.data.BusinessException;
import com.zhxh.imms.mes.egr.setting.domain.PLC;
import com.zhxh.imms.utils.ByteUtil;

import java.util.Hashtable;

public class OmronPlcPipe extends PlcPipe {
    private static final byte[] handShake =
            new byte[]{0x46, 0x49, 0x4e, 0x53, 0x00, 0x00, 0x00, 0x0c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    private final byte[] fins_frame = new byte[]{
            0x46, 0x49, 0x4e, 0x53,  //FINS
            0x00, 0x00, 0x00, 0x1A,  //长度：26
            0x00, 0x00, 0x00, 0x02,  //命令：02
            0x00, 0x00, 0x00, 0x00,  //错误码：0
            (byte) 0x80, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    private final byte[] plcIP = new byte[4];
    private final byte[] serverIp = new byte[4];

    private Hashtable<String, HeadAddress> headCache = new Hashtable<>();

    public OmronPlcPipe() {
    }

    public OmronPlcPipe(PLC plc) {
        super(plc);
    }

    @Override
    protected boolean connectToPlc() {
        int writeCount = this.tcpClient.write(handShake);
        if (writeCount != handShake.length) {
            return false;
        }
        byte[] recvBuffer = new byte[24];
        int readCount = this.tcpClient.read(recvBuffer);
        if (readCount != recvBuffer.length) {
            return false;
        }
        System.arraycopy(recvBuffer, 16, this.serverIp, 0, 4);
        System.arraycopy(recvBuffer, 20, this.plcIP, 0, 4);

        fins_frame[20] = this.plcIP[3];
        fins_frame[23] = this.serverIp[3];

        return true;
    }

    @Override
    public boolean readBoolean(String addr) {
        byte result = this.readBit(addr);
        return result == 1;
    }

    @Override
    public String readString(String addr, int length) {
        byte[] readBuffer = this.readWord(addr, length);
        return new String(readBuffer);
    }

    @Override
    public int readInt(String addr, int length) {
        int DATA_TYPE_LENGTH = 4;
        String DATA_TYPE = "整数";
        byte[] intBuffer = this.getDataBytes(addr, length, DATA_TYPE_LENGTH, DATA_TYPE);

        return ByteUtil.getInt(intBuffer, 0);
    }

    public long readLong(String addr, int length) {
        int DATA_TYPE_LENGTH = 8;
        String DATA_TYPE = "长整数";
        byte[] intBuffer = this.getDataBytes(addr, length, DATA_TYPE_LENGTH, DATA_TYPE);
        return ByteUtil.getLong(intBuffer, 0);
    }

    @Override
    public float readFloat(String addr, int length) {
        int DATA_TYPE_LENGTH = 4;
        String DATA_TYPE = "单精度浮点数";
        byte[] intBuffer = this.getDataBytes(addr, length, DATA_TYPE_LENGTH, DATA_TYPE);
        return ByteUtil.getFloat(intBuffer, 0);
    }

    @Override
    public double readDouble(String addr, int length) {
        int DATA_TYPE_LENGTH = 8;
        String DATA_TYPE = "双精度浮点数";
        byte[] intBuffer = this.getDataBytes(addr, length, DATA_TYPE_LENGTH, DATA_TYPE);
        return ByteUtil.getDouble(intBuffer, 0);
    }

    private byte[] getDataBytes(String addr, int length, int DATA_TYPE_LENGTH, String DATA_TYPE) {
        if (length > DATA_TYPE_LENGTH || length < 1) {
            throw new BusinessException(DATA_TYPE + "的长度必须大于1且小于等于" + DATA_TYPE_LENGTH);
        }
        int index = DATA_TYPE_LENGTH - length;
        byte[] intBuffer = new byte[DATA_TYPE_LENGTH];
        byte[] readBuffer = this.readWord(addr, length);
        if (readBuffer != null) {
            System.arraycopy(readBuffer, 0, intBuffer, index, length);
            return intBuffer;
        }
        throw new BusinessException("数据读取出现异常：没有读取到指定长度的数据");
    }

    @Override
    public int rawRead(String addr, int length, byte[] resultBuffer,int readType) {
        HeadAddress headAddress = headCache.get(addr);
        if (headAddress == null) {
            headAddress = new HeadAddress(addr);
        }

        byte[] frame = new byte[this.fins_frame.length];
        System.arraycopy(this.fins_frame, 0, frame, 0, frame.length);

        frame[29] = (byte) ((headAddress.getChannel() >> 8) & 0xFF);
        frame[30] = (byte) (headAddress.getChannel() & 0xFF);
        if (readType == PlcPipe.READ_TYPE_WORD) {
            frame[28] = headAddress.getAreaCodeWord();
            frame[31] = 0x00;
            frame[32] = (byte) ((length >> 8) & 0xFF);
            frame[33] = (byte) (length & 0xFF);
        } else {
            frame[28] = headAddress.getAreaCodeBit();
            frame[31] = headAddress.getBit();
            frame[32] = 0x00;
            frame[33] = 0x01;
        }
        if (this.tcpClient.write(frame) != frame.length) {
            throw new BusinessException("向PLC发送读取命令失败！");
        }

        return this.tcpClient.read(resultBuffer);
    }

    @Override
    public byte[] readWord(String addr, int length) {
        HeadAddress headAddress = headCache.get(addr);
        if (headAddress == null) {
            headAddress = new HeadAddress(addr);
        }

        byte[] frame = new byte[this.fins_frame.length];
        System.arraycopy(this.fins_frame, 0, frame, 0, frame.length);

        frame[28] = headAddress.getAreaCodeWord();
        frame[29] = (byte) ((headAddress.getChannel() >> 8) & 0xFF);
        frame[30] = (byte) (headAddress.getChannel() & 0xFF);
        frame[31] = 0x00;
        frame[32] = (byte) ((length >> 8) & 0xFF);
        frame[33] = (byte) (length & 0xFF);

        return this.doRead(frame, length);
    }

    @Override
    public byte readBit(String addr) {
        HeadAddress headAddress = headCache.get(addr);
        if (headAddress == null) {
            headAddress = new HeadAddress(addr);
        }

        byte[] frame = new byte[this.fins_frame.length];
        System.arraycopy(this.fins_frame, 0, frame, 0, frame.length);

        frame[28] = headAddress.getAreaCodeBit();
        frame[29] = (byte) ((headAddress.getChannel() >> 8) & 0xFF);
        frame[30] = (byte) (headAddress.getChannel() & 0xFF);
        frame[31] = headAddress.getBit();
        frame[32] = 0x00;
        frame[33] = 0x01;

        return this.doRead(frame, 1)[0];
    }

    private byte[] doRead(byte[] command, int length) {
        if (this.tcpClient.write(command) != command.length) {
            throw new BusinessException("向PLC发送读取命令失败！");
        }

        byte[] resultBuffer = new byte[30 + length];
        int readLength = this.tcpClient.read(resultBuffer);
        if (readLength != resultBuffer.length) {
            return null;
        }

        boolean isSuccess = true;
        if (resultBuffer[11] == 3) {
            isSuccess = checkResultHead(resultBuffer[15]);
        }
        if (isSuccess && resultBuffer[27] == 1 && checkResultEnd(resultBuffer[28], resultBuffer[29])) {
            byte[] dataBuffer = new byte[length];
            System.arraycopy(resultBuffer, 30, dataBuffer, 0, dataBuffer.length);
            return dataBuffer;
        }
        return null;
    }


    private boolean checkResultEnd(byte main, byte sub) {
        if (main == 0 && sub == 0) {
            return true;
        }

        String[] subErrors = endErrors.get((int) main);
        if (sub < 1 || subErrors == null || sub >= subErrors.length) {
            throw new BusinessException("unknown exception");
        }
        throw new BusinessException(subErrors[sub]);
    }

    private boolean checkResultHead(byte code) {
        if (code == 0) {
            return true;
        }
        if (code > 0 && code <= 3) {
            throw new BusinessException(headError[code]);
        }
        throw new BusinessException("unknown exception");
    }

    private static final String[] subError_0 = new String[]{
            "",
            "service canceled"
    };
    private static final String[] subError_1 = new String[]{
            "",
            "local node not in network",
            "token timeout",
            "retries failed",
            "too many send frames",
            "node address range error",
            "node address duplication"
    };
    private static final String[] subError_2 = new String[]{
            "",
            "destination node not in network",
            "unit missing",
            "third node missing",
            "destination node busy",
            "response timeout"
    };
    private static final String[] subError_3 = new String[]{
            "",
            "communications controller error",
            "CPU unit error",
            "controller error",
            "unit number error"
    };
    private static final String[] subError_4 = new String[]{
            "",
            "undefined command",
            "not supported by model/version"
    };
    private static final String[] subError_5 = new String[]{
            "",
            "destination address setting error",
            "no routing tables",
            "routing table error",
            "too many relays"
    };
    private static final String[] subError_16 = new String[]{
            "",
            "command too long",
            "command too short",
            "elements/data don't match",
            "command format error",
            "header error"
    };
    private static final String[] subError_17 = new String[]{
            "",
            "area classification missing",
            "access size error",
            "address range error",
            "address range exceeded",
            "program missing",
            "relational error",
            "duplicate data access",
            "response too long",
            "parameter error"
    };
    private static final String[] subError_32 = new String[]{
            "",
            "protected",
            "table missing",
            "data missing",
            "program missing",
            "file missing",
            "data mismatch"
    };
    private static final String[] subError_33 = new String[]{
            "",
            "read-only",
            "protected , cannot write data link table",
            "cannot register",
            "program missing",
            "file missing",
            "file name already exists",
            "cannot change"
    };
    private static final String[] subError_34 = new String[]{
            "",
            "not possible during execution",
            "not possible while running",
            "wrong PLC mode",
            "wrong PLC mode",
            "wrong PLC mode",
            "wrong PLC mode",
            "specified node not polling node",
            "step cannot be executed"
    };
    private static final String[] subError_35 = new String[]{
            "",
            "file device missing",
            "memory missing",
            "clock missing"
    };
    private static final String[] subError_36 = new String[]{
            "",
            "table missing"
    };
    private static final String[] subError_37 = new String[]{
            "",
            "memory error",
            "I/O setting error",
            "too many I/O points",
            "CPU bus error",
            "I/O duplication",
            "CPU bus error",
            "SYSMAC BUS/2 error",
            "CPU bus unit error",
            "SYSMAC BUS No. duplication",
            "memory error",
            "SYSMAC BUS terminator missing"
    };
    private static final String[] subError_38 = new String[]{
            "",
            "no protection",
            "incorrect password",
            "protected",
            "service already executing",
            "service stopped",
            "no execution right",
            "settings required before execution",
            "necessary items not set",
            "number already defined",
            "error will not clear"
    };
    private static final String[] subError_48 = new String[]{
            "",
            "no access right"
    };
    private static final String[] subError_64 = new String[]{
            "",
            "service aborted"
    };

    private static final Hashtable<Integer, String[]> endErrors = new Hashtable<>();

    private static final String[] headError = new String[]{
            "",
            "the head is not 'FINS'",
            "the data length is too long",
            "the command is not supported"
    };

    static {
        endErrors.put(0, subError_0);
        endErrors.put(1, subError_1);
        endErrors.put(2, subError_2);
        endErrors.put(3, subError_3);
        endErrors.put(4, subError_4);
        endErrors.put(5, subError_5);

        endErrors.put(16, subError_16);
        endErrors.put(17, subError_17);

        endErrors.put(32, subError_32);
        endErrors.put(33, subError_33);
        endErrors.put(34, subError_34);
        endErrors.put(35, subError_35);
        endErrors.put(36, subError_36);
        endErrors.put(37, subError_37);
        endErrors.put(38, subError_38);

        endErrors.put(48, subError_48);
        endErrors.put(64, subError_64);
    }
}
