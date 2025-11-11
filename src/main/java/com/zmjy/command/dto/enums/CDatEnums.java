package com.zmjy.command.dto.enums;

import lombok.Getter;

public enum CDatEnums {
    //油品的种类
    Nb_Products((byte) 0x02),
    //加油模式的种类
    Nb_Fuelling_Modes((byte) 0x03),
    //加油机计算器控制的加油点的数量
    Nb_FP((byte) 0x05),
    //加油机是否可在自主加油模式中工作
    Stand_Alone_Auth((byte) 0x0C),
    ;

    @Getter
    public final byte dataId;

    CDatEnums(byte dataId) {
        this.dataId = dataId;
    }
}
