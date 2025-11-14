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
    //授权或预授权来启动一个交易（Release_FP）
    Release_FP((byte) 0x3E),
    //为运行的加油交易指定序列号（Current_TR_Seq_Nb）
    Current_TR_Seq_Nb((byte) 0x1D),
    //描述未支付交易数（Nb_Tran_Buffer_Not_Paid）
    Nb_Tran_Buffer_Not_Paid((byte) 0x02),
    //为运行的交易指定释放 FP 的控制器（Release_Contr_Id）
    Release_Contr_Id((byte) 0x1E),
    //这顶运行交易发送消息的频率，以10秒为单位 (0 = 未激活 1-999 = 以十秒为单位的延迟。例如：2代表有20秒的间隔)
    Running_Transaction_Message_Frequency((byte) 0x3B),
    ;

    @Getter
    public final byte dataId;

    FpIdEnums(byte dataId) {
        this.dataId = dataId;
    }
}
