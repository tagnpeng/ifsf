package com.zmjy.command.dto.enums;

import lombok.Getter;

public enum ComSvEnums {
    //
    read((byte) 0x02),
    //查询主动推送地址
    query((byte) 0x03),
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
