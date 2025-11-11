package com.zmjy.command.dto.enums;

import lombok.Getter;

public enum FpIdLnIdEnums {
    //用来标识逻辑油枪加出的油品。
    PR_Id((byte) 0x01),
    //为该逻辑油枪指定对应物理油枪标识
    Physical_Noz_Id((byte) 0x05),
    ;

    @Getter
    public final byte dataId;

    FpIdLnIdEnums(byte dataId) {
        this.dataId = dataId;
    }
}
