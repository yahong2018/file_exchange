package com.zhxh.imms.omron.backgroud;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskConfig {
    private boolean autoStart; 
    private int connectionCheckDuration; 
    private int disConnectLogDuration; 
}
