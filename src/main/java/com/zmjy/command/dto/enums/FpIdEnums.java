package com.zmjy.command.dto.enums;

import lombok.Getter;

public enum FpIdEnums {
    //FP 上逻辑油枪的数量。数量被接受的范围是 1 到 8。
    Nb_Logical_Nozzle((byte) 0x04),
    //用来指定 FP 是否被分配给控制器，分配给哪个控制器。
    Assign_Contr_Id((byte) 0x16),
    //允许 CD 授权一个或多个逻辑油枪
    Log_Noz_Mask((byte) 0x19),
    //打开一个关闭的 FP
    Open_FP((byte) 0x3C),
    //用来关闭一个 FP
    Close_FP((byte) 0x3D),
    //加油点的加油模式（FM_ID）
    Fuelling_Mode((byte) 0x21),
    //用来表明 FP 的状态。请参看加油点状态图中每个状态的详细情况（FP_State）
    FP_State((byte) 0x14),
    //允许读取所有逻辑油枪的状态（Log_Noz_State）
    Log_Noz_State((byte) 0x15),
    ;

    @Getter
    public final byte dataId;

    FpIdEnums(byte dataId) {
        this.dataId = dataId;
    }
}
