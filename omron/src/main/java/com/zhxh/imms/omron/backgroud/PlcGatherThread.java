package com.zhxh.imms.omron.backgroud;

import com.zhxh.imms.omron.domain.PLC;

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
            

            this.sleepThread(100);            
        }
    }

    private void sleepThread(int mills){
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {                
            e.printStackTrace();
        }
    }

}
