package com.zmjy.command.dto.enums;

import lombok.Getter;

public enum FpIdTrDatTrSeqNbEnums {
    //FP为每个交易生成唯一的序列号。这个号和在这个数据库地址中使用的是同一个号
    TR_Seq_Nb((byte) 0x01),
    //指定交易中的金额
    TR_Amount((byte) 0x05),
    //指定交易中的体积
    TR_Volume((byte) 0x06),
    //指定加入的油品单价
    TR_Unit_Price((byte) 0x07),
    //指定加油使用的逻辑油枪
    TR_Log_Noz((byte) 0x08),
    //清除交易缓冲区中的一个可支付交易。在清除前，交易不一定要被锁定。当交易缓冲区状态处在状态2或3时，允许这个命令。
    Clear_Transaction((byte) 0x1E),
    ;

    @Getter
    public final byte dataId;

    FpIdTrDatTrSeqNbEnums(byte dataId) {
        this.dataId = dataId;
    }
}
