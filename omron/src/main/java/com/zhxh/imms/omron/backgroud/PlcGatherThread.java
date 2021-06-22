package com.zhxh.imms.omron.backgroud;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.zhxh.imms.omron.domain.PLC;
import com.zhxh.imms.omron.domain.PlcRegister;
import com.zhxh.imms.utils.Logger;


public class PlcGatherThread {
    private boolean terminated = true;
    private Thread workThread;
    private PLC plc;
    private OmronPlc omronPlc;

    public PLC getPlc() {
        return plc;
    }

    public void setPlc(PLC plc) {
        this.plc = plc;
        this.omronPlc = new OmronPlc(plc);
    }

    public boolean isTerminated() {
        return terminated;
    }

    public void terminate() {
        this.terminated = true;
    }

    public void start() {
        if (!this.terminated) {
            return;
        }
        this.terminated = false;

        this.workThread = new Thread(() -> doGather());    
        this.workThread.setName("PLC数据采集线程" + this.plc.getCode());
        this.workThread.start();        
    }

    public void stop() {
        if(this.terminated){
            return;
        }

        this.omronPlc.disConnect();
        this.terminated = true;        
    }

    public boolean isConnected(){
        return this.omronPlc.isNetworkConnected() && this.omronPlc.isHandShaked();
    }

    public void restart(){
        if (this.terminated) {
            this.start();
        } else {
            this.omronPlc.disConnect();
            this.omronPlc.connect();
        }
    }

    private void doGather(){
        while(!this.terminated){
            Map<String, Object> lastItem = null;
            LocalDateTime lastDataReadTime = LocalDateTime.MIN;
            LocalDateTime lastConnectTime = LocalDateTime.now();
            LocalDateTime lastConnectCheckTime = LocalDateTime.now();
            while (!terminated) {
                try {
                    Thread.sleep(1000);
                    if (!omronPlc.connect()) {
                        // 如果连续的网络不通的时间长达1分钟，则需要记录下来，每个1分钟记录1次
                        long disConnectSeconds = Duration.between(lastConnectCheckTime, LocalDateTime.now()).getSeconds();
                        long totalDisConnect = Duration.between(lastConnectTime, LocalDateTime.now()).getSeconds();
    
                        if (disConnectSeconds > 60) {
                            lastConnectCheckTime = LocalDateTime.now();
    
                            int totalMin = (int) (totalDisConnect / 60);
                            Logger.error("PLC" + this.omronPlc.getPlc().getCode() + "断线,累计断线" + totalMin + "分钟");
                        }
    
                        continue;
                    }
    
                    lastConnectTime = LocalDateTime.now();
                    lastConnectCheckTime = LocalDateTime.now();
    
                    Map<String, Object> item = new HashMap<>();
                    for (int i = 0; i < this.plc.getRegisters().length; i++) {
                        if(this.terminated){
                            return;
                        }
    
                        PlcRegister register = this.plc.getRegisters()[i];
                        String area = register.getArea();
                        String addr = register.getAddr();
                        int indexOrLength = register.getIndexOrLength();
                        Object value = null;
                        try {
                            switch (register.getDataType()) {
                                case "int":
                                    value = this.omronPlc.readInt(area, addr, indexOrLength);
                                    break;                            
                                case "float":
                                    value = this.omronPlc.readFloat(area, addr, indexOrLength);
                                    break;                    
                                case "bit":
                                    value = this.omronPlc.readBit(area,addr,(byte)indexOrLength);
                                    break;
                                default:
                                    value = this.omronPlc.readString(area, addr, indexOrLength);
                                    break;
                            }
                        }catch (Exception e){
                            Logger.error("获取字段"+addr+"的值失败："+e.getMessage());
                            Logger.debug(e);
                        }
                        item.put(register.getFieldName(), value);
                    }
    
                    long duration = Duration.between(lastDataReadTime, LocalDateTime.now()).getSeconds();
                    if (duration > this.getPlc().getMaxDuration()   //如果到了下一个取数周期
                            || !itemEquals(lastItem, item)   // 或者取得的数据跟上次数据不一样
                    ) {
                        if(this.terminated){
                            return;
                        }
    
                        this.insertData(item);
                        lastItem = item;
                        lastDataReadTime = LocalDateTime.now();
                    }
                } catch (Exception e) {
                    Logger.error(e);
                }
            }

            this.sleepThread(100);            
        }
    }

    private boolean itemEquals(Map<String, Object> lastItem, Map<String, Object> item) {
        if (lastItem == null) {
            return false;
        }
        for (String key : lastItem.keySet()) {
            Object lastValue = lastItem.get(key);
            Object value = item.get(key);

            if (lastValue == null) {
                if (value != null) {
                    return false;
                }
            } else if (!lastValue.equals(value)) {
                return false;
            }
        }

        return true;
    }

    private void insertData(Map<String, Object> item){
        
    }


    private void sleepThread(int mills){
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {                
            e.printStackTrace();
        }
    }

}
