package com.zhxh.imms.mes.egr.service;

import com.zhxh.imms.admin.logic.SystemParamLogic;
import com.zhxh.imms.mes.egr.setting.domain.PLC;
import com.zhxh.imms.mes.egr.setting.domain.PlcRegister;
import com.zhxh.imms.mes.egr.setting.logic.PlcLogic;
import com.zhxh.imms.utils.Logger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AutoPlcTask {
    @Autowired
    private PlcLogic plcLogic;

    @Autowired
    private SystemParamLogic paramLogic;

    private boolean running;
    private List<PlcDataThread> plcDataReadThreadList = new ArrayList<>();

    @PostConstruct
    public void init() {
        String autoStartGather = paramLogic.getValue("Z03", "auto_start_gather");
        if (StringUtils.equalsIgnoreCase("true", autoStartGather)) {
            this.start();
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void start() {
        if (this.running) {
            return;
        }
        this.plcDataReadThreadList.clear();
        List<PLC> plcList = plcLogic.getAll();
        for (PLC plc : plcList) {
            PlcPipe plcPipe = new OmronPlcPipe(plc);
            PlcDataThread thread = new PlcDataThread(plcPipe, plcLogic);
            plcDataReadThreadList.add(thread);
        }

        for (PlcDataThread thread : this.plcDataReadThreadList) {
            thread.start();
        }

        Logger.info("自动采集任务已启动...");

        this.running = true;
    }

    public synchronized void stop() {
        if (!this.running) {
            return;
        }
        for (PlcDataThread thread : this.plcDataReadThreadList) {
            thread.terminate();
        }
        this.plcDataReadThreadList.clear();

        Logger.info("自动采集任务已停止...");
        this.running = false;
    }
}

