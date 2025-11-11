package com.zmjy.command.dto.enums;

import lombok.Getter;

public enum PrIdEnums {
    //在系统配置时油品号由控制设备设定，控制设备及其
    //程序用它来发送油品参数（油品名称，价格）。一台加油机的油
    //品编号必须唯一。以油品号为 0000000 对地址 PR_ID 的写操作
    //意味着其关联的数据将被删除
    Prod_Nb((byte) 0x02),
    //指定的油品描述
    Prod_description((byte) 0x03),
    ;

    @Getter
    public final byte dataId;

    PrIdEnums(byte dataId) {
        this.dataId = dataId;
    }
}
