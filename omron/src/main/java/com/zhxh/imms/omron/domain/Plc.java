package com.zhxh.imms.omron.domain;

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
    private PlcRegister[] registers;
}
