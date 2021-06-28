package com.zhxh.imms.omron.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PLC {
    private long id;  
    private String ip;
    private int port;
    private String code;
    private int maxDuration;  
    private boolean inUse;   //启用状态： true 启用 false 停用
    private boolean running; //运行状态: true 运行 false 停止
    private LocalDateTime lastConnectionTime; //最后连线时间
    private PlcRegister[] registers;

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PLC)){
            return false;
        }

        PLC otherPlc = (PLC)obj;
        return this.code.equals(otherPlc.code);
    }
}
