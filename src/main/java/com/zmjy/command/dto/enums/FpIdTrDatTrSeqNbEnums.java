package com.zmjy.command.dto.enums;

import lombok.Getter;

public enum FpIdTrDatTrSeqNbEnums {
    //清除交易缓冲区中的一个可支付交易。在清除前，交易不一定要被锁定。当交易缓冲区状态处在状态2或3时，允许这个命令。
    Clear_Transaction((byte) 0x1E)
    ;

    @Getter
    public final byte dataId;

    FpIdTrDatTrSeqNbEnums(byte dataId) {
        this.dataId = dataId;
    }
}
