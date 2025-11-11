package com.zmjy.command.dto.enums;

import lombok.Data;
import lombok.ToString;

/**
 * FP状态枚举
 *
 * @author tang
 * @date 2025/11/11
 */
@ToString
public enum FpStatusEnums {
    INOPERATIVE(1, "不可操作"),
    CLOSED(2, "关闭"),
    IDLE(3, "空闲"),
    CALLING(4, "请求授权"),
    AUTHORIZED(5, "已授权"),
    STARTED(6, "开始加油"),
    SUSPENDED_STARTED7(7, "暂停开始加油7"),
    FUELLING(8, "加油中"),
    SUSPENDED_STARTED9(9, "暂停开始加油9"),
    ;

    private final int code;
    private final String message;

    FpStatusEnums(int code, String message) {
        this.code = code;
        this.message = message;
    }

    //通过code获取枚举
    public static FpStatusEnums getByCode(int code) {
        for (FpStatusEnums value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
