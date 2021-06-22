package com.zhxh.imms.omron.backgroud;

import java.util.Hashtable;

import com.zhxh.imms.omron.domain.PLC;
import com.zhxh.imms.utils.BusinessException;

public class OmronPlc {
    protected TcpClient tcpClient;
    protected PLC plc;
    private boolean handShaked = false;

    public PLC getPlc() {
        return plc;
    }

    public void setPlc(PLC plc) {
        this.plc = plc;
    }

    public OmronPlc() {
    }

    public OmronPlc(PLC plc) {
        this.plc = plc;        
    }

    public boolean isNetworkConnected(){
        if(this.tcpClient==null){
            return false;
        }

        return this.tcpClient.isConnected();
    }

    public boolean isHandShaked() {
        return handShaked;
    }

    public boolean connect() {
        if (this.tcpClient == null) {
            this.tcpClient = new TcpClient(plc.getIp(), plc.getPort());
        }

        if (!this.isHandShaked() || !this.tcpClient.isConnected()) {
            if (!this.tcpClient.connect()) {
                return false;
            }
            this.handShaked = this.connectToPlc();
        }
        return this.handShaked;
    }
   
    public void disConnect() {       
        this.tcpClient.disConnect();
        this.tcpClient = null;
        this.handShaked = false;
    }

    public byte readBit(String area,String addr,byte index){
        byte[] frame = new byte[this.fins_frame.length];
        System.arraycopy(this.fins_frame, 0, frame, 0, frame.length);
        byte areaCodeBit = this.getAreaCodeBit(area);
        int channel = Integer.parseInt(addr);
        frame[28] = areaCodeBit;
        frame[29] = (byte) ((channel >> 8) & 0xFF);
        frame[30] = (byte) (channel & 0xFF);
        frame[31] = index;
        frame[32] = 0x00;
        frame[33] = 0x01;

        byte[] result = this.doRead(frame, 1);
        if(result==null){
            return -1;
        }
        return result[0];
    }

    public byte[] readWord(String area,String addr,int length){
        byte[] frame = new byte[this.fins_frame.length];
        System.arraycopy(this.fins_frame, 0, frame, 0, frame.length);

        byte areaCodeWord = this.getAreaCodeWord(area);
        int channel = Integer.parseInt(addr);

        frame[28] = areaCodeWord;
        frame[29] = (byte) ((channel >> 8) & 0xFF);
        frame[30] = (byte) (channel & 0xFF);
        frame[31] = 0x00;
        frame[32] = (byte) ((length >> 8) & 0xFF);
        frame[33] = (byte) (length & 0xFF);

        return this.doRead(frame, length);
    }

    public float readFloat(String area,String addr,int length){
        byte[] floatBuffer = this.getNumberBytes(area, addr,length, length * 2, 4, 2, "Float");
        int intValue = this.bytes2Int(floatBuffer);
        float result = Float.intBitsToFloat(intValue);
        return result;
    }

    public String readString(String area,String addr,int length){
        byte[] dataBuffer = this.readWord(area, addr, length*2);
        String resultString = new String(dataBuffer);
        return resultString;
    }

    private boolean connectToPlc(){
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

    private byte[] getNumberBytes(String area,String addr,int length,int byteLength,int MAX_BYTES,int MAX_WORD,String DATA_TYPE){
        if (byteLength > MAX_BYTES) {
            throw new BusinessException(addr + "的配置错误：" + DATA_TYPE + "的长度最大是" + MAX_WORD + "个字，" + MAX_BYTES
                    + "个字节，配置为" + length + "字，" + byteLength + "字节.");
        }

        byte[] dataBuffer = this.readWord(area, addr, byteLength);
        byte[] numberBufer = new byte[MAX_BYTES];
        System.arraycopy(dataBuffer, 0, numberBufer, MAX_BYTES - byteLength, byteLength);

        return numberBufer;
    }

    public int readInt(String area,String addr,int length){
        byte[] intBuffer = this.getNumberBytes(area, addr,length, length * 2, 4, 2, "Int");
        int result = this.bytes2Int(intBuffer); 
        return result;
    }

    private int bytes2Int(byte[] intBuffer){
        return  ((intBuffer[3] << 24) &0xFF000000) | ((intBuffer[2] << 16) &0xFF00)  | ((intBuffer[1] << 8) &0xFF) | intBuffer[0]; 
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

    private byte getAreaCodeWord(String area) {
        switch (area) {
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

    private byte getAreaCodeBit(String area) {
        switch (area) {
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
