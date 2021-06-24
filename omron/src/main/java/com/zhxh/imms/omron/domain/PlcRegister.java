package com.zhxh.imms.omron.domain;

import lombok.Setter;

import lombok.Getter;

@Getter
@Setter
public class PlcRegister {
    private long pldId; // 所属PLC
    private String fieldName; // 字段
    private String dataType; //数据类型: varchar/int/float/bit
    private int fieldLength; //字段长度：存储在数据库中的长度（位数）
    private String area; // 区域
    private String addr; // 起始地址
    private int indexOrLength; // 长度    
    private String chinese; // 中文
    private String japanese; // 日文

    public final static int READ_TYPE_WORD = 0;
    public final static int READ_TYPE_BIT = 1;
}
