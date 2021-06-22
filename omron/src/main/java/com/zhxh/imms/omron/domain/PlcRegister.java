package com.zhxh.imms.omron.domain;

import lombok.Setter;

import lombok.Getter;

@Getter
@Setter
public class PlcRegister {
    private long pldId; // 所属PLC
    private String fieldName; // 字段
    private String dataType; //数据类型: varchar/int/float
    private int fieldLength; //字段长度：存储在数据库中的长度（位数）
    private String area; // 区域
    private String addr; // 起始地址
    private int registerLength; // 长度    
    private String chinese; // 中文
    private String japanese; // 日文
    private int readType; // 读取方式： 0 按字读取，每次读取N个字<N*2个字节> 1 按位读取，每次读取N个位
    private int updateRate; //数据更新频率

    public final static int READ_TYPE_WORD = 0;
    public final static int READ_TYPE_BIT = 1;
}
