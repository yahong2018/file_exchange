package com.zhxh.imms.omron.backgroud;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskConfig {
    private boolean autStart; 
    private int connectionCheckDuration; 
    private int disConnectLogDuration; 
}
