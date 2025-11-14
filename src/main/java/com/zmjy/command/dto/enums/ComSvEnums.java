package com.zmjy.command.dto.enums;

import lombok.Getter;

public enum ComSvEnums {
    //添加主动消息地址
    ADD((byte) 0x0B),
    //删除主动消息地址
    REMOVE((byte) 0x0C),
    ;

    @Getter
    public final byte dataId;

    ComSvEnums(byte dataId) {
        this.dataId = dataId;
    }
}
