package com.zmjy.command.dto.enums;

import lombok.Getter;

public enum PrDatProdNbFmIdEnums {
    //指定油品/加油模式中的单价
    Prod_Price((byte) 0x02),
    ;

    @Getter
    public final byte dataId;

    PrDatProdNbFmIdEnums(byte dataId) {
        this.dataId = dataId;
    }
}
