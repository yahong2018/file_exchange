package com.zhxh.imms.mes.egr.service;

import com.zhxh.imms.mes.egr.setting.domain.PlcRegister;
import com.zhxh.imms.mes.egr.setting.logic.PlcLogic;
import com.zhxh.imms.utils.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlcDataThread extends Thread {
    private PlcPipe plcPipe;
    private PlcLogic plcLogic;
    private boolean terminated = false;
    private List<PlcRegister> registerList;

    public PlcDataThread(PlcPipe plcPipe, PlcLogic plcLogic) {
        this.plcPipe = plcPipe;
        this.setName("PLC数据线程[" + plcPipe.plc.getPlcCode() + "]");

        this.plcLogic = plcLogic;
        this.registerList = this.plcLogic.getRegistersByPlcId(plcPipe.getPlc().getRecordId());
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    @Override
    public void run() {
        Map<String, Object> lastItem = null;
        LocalDateTime lastDataReadTime = LocalDateTime.MIN;
        LocalDateTime lastConnectTime = LocalDateTime.now();
        LocalDateTime lastConnectCheckTime = LocalDateTime.now();
        while (!terminated) {
            try {
                Thread.sleep(1000);
                if (!plcPipe.connect()) {
                    // 如果连续的网络不通的时间长达1分钟，则需要记录下来，每个1分钟记录1次
                    long disConnectSeconds = Duration.between(lastConnectCheckTime, LocalDateTime.now()).getSeconds();
                    long totalDisConnect = Duration.between(lastConnectTime, LocalDateTime.now()).getSeconds();

                    if (disConnectSeconds > 60) {
                        lastConnectCheckTime = LocalDateTime.now();

                        int totalMin = (int) (totalDisConnect / 60);
                        Logger.error("PLC" + this.plcPipe.getPlc().getPlcCode() + "断线,累计断线" + totalMin + "分钟");
                    }

                    continue;
                }

                lastConnectTime = LocalDateTime.now();
                lastConnectCheckTime = LocalDateTime.now();

                Map<String, Object> item = new HashMap<>();
                for (int i = 0; i < this.registerList.size(); i++) {
                    if(this.terminated){
                        return;
                    }

                    PlcRegister register = this.registerList.get(i);
                    String addr = register.getRegAddr();
                    int length = register.getFieldLength();
                    Object value = null;
                    try {
                        switch (register.getFieldType()) {
                            case "int":
                                value = plcPipe.readInt(addr, length);
                                break;
                            case "bigint":
                                value = plcPipe.readLong(addr, length);
                                break;
                            case "float":
                                value = plcPipe.readFloat(addr, length);
                                break;
                            case "double":
                                value = plcPipe.readDouble(addr, length);
                                break;
                            case "bit":
                                value = plcPipe.readBoolean(addr);
                                break;
                            default:
                                value = plcPipe.readString(addr, length);
                                break;
                        }
                    }catch (Exception e){
                        Logger.error("获取字段"+addr+"的值失败："+e.getMessage());
                        Logger.debug(e);
                    }
                    item.put(register.getFieldName(), value);
                }

                long duration = Duration.between(lastDataReadTime, LocalDateTime.now()).getSeconds();
                if (duration > this.plcPipe.getPlc().getMaxDuration()   //如果到了下一个取数周期
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
    }

    private void insertData(Map<String, Object> item) {
        this.plcLogic.insertData(plcPipe.getPlc().getRecordId(), item, LocalDateTime.now());
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

    public void terminate() {
        this.terminated = true;
        this.plcPipe.disConnect();
    }
}
