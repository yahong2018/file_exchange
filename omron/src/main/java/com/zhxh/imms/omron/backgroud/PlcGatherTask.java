package com.zhxh.imms.omron.backgroud;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.zhxh.imms.omron.domain.PLC;
import com.zhxh.imms.omron.domain.PLcInitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlcGatherTask {
    /*
        功能：
            1. 启动/停止所有的PLC采集线程
            2. 检查某个PLC采集线程的网络状态与采集状态，对出现问题的线程，及时重启。
            3. 如果增加/移除了PLC，则需要重启服务（手工重启服务或者重启程序）
    */
    @Autowired
    private PLcInitor initor;

    private boolean autoStart;
    private boolean running;
    private Thread checkThread;
    private TaskConfig taskConfig;
    private List<PlcGatherThread> threadList = new ArrayList<>();
    
    public boolean isAutoStart() {
        return autoStart;
    }
        
    private void init(){
        threadList.clear();
        this.taskConfig  = initor.getTaskConfig();
        PLC[] plcList = initor.getPlcList();
        for(PLC plc:plcList){
            PlcGatherThread thread = new PlcGatherThread();
            thread.setPlc(plc);

            threadList.add(thread);
        }
    }

    @PostConstruct
    private void autoStart(){
        this.autoStart = false; //正式系统从配置文件中获取

        this.init();
        if(this.isAutoStart()){
            this.start();
        }
    }

    public PlcGatherThread[] getThreadList() {
        PlcGatherThread[] result = new PlcGatherThread[this.threadList.size()];
        return threadList.toArray(result);
    }

    public boolean isRunning() {
        return running;
    }

    public void start(){
        if(this.isRunning()){
            return;
        }
        
        for(PlcGatherThread thread:this.threadList){
            if (thread.getPlc().isInUse()) {
                thread.start();
            }
        }

        this.running = true;
        this.checkThread = new Thread(()->check());
        this.checkThread.setName("PLC采集总调度任务线程");
        this.checkThread.start();
    }

    public void stop(){
        if(!this.isRunning()){
            return;
        }

        for(PlcGatherThread thread:this.threadList){
            thread.stop();
        }
        this.running = false;
    }

    private void check(){
        while(this.isRunning()){
            try{            
                Thread.sleep(this.taskConfig.getConnectionCheckDuration() * 1000); // 5分钟检查一次

                for(PlcGatherThread thread:this.threadList){
                    if(!thread.getPlc().isInUse()){
                        continue;
                    }

                    if(!thread.isConnected()){
                        thread.restart();
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void stop(PLC plc){
        for(PlcGatherThread thread:this.threadList){
            if(thread.getPlc().equals(plc)){
                thread.stop();
                return;
            }
        }
    }

    public void start(PLC plc){
        for(PlcGatherThread thread:this.threadList){
            if(thread.getPlc().equals(plc)){
                thread.start();
                return;
            }
        }
    }

    public void restart(PLC plc){
        for(PlcGatherThread thread:this.threadList){
            if(thread.getPlc().equals(plc)){
                thread.restart();
                return;
            }
        }
    }

    public void restart(){
        this.stop();
        this.init();
        this.start();
    }
}
