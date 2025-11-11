package com.zmjy.command.dto.enums;

import lombok.ToString;

@ToString
public enum ErrorCode {

    // 重大错误
    RAM_DEFECT((byte)0x01, "重大错误", "RAM 缺陷"),
    ROM_DEFECT((byte)0x02, "重大错误", "ROM 缺陷"),
    CONFIG_ERROR((byte)0x03, "重大错误", "配置或参数错误"),
    VOLTAGE_UNSTABLE((byte)0x04, "重大错误", "电压不稳定"),
    // …其他重大错误…
    OIL_RECOVERY_ERROR((byte)0x12, "重大错误", "油气回收错误"),

    // 空闲 0x13 ~ 0x1F
    IDLE_13((byte)0x13, "空闲", "未使用"),
    IDLE_14((byte)0x14, "空闲", "未使用"),
    IDLE_15((byte)0x15, "空闲", "未使用"),
    IDLE_16((byte)0x16, "空闲", "未使用"),
    IDLE_17((byte)0x17, "空闲", "未使用"),
    IDLE_18((byte)0x18, "空闲", "未使用"),
    IDLE_19((byte)0x19, "空闲", "未使用"),
    IDLE_1A((byte)0x1A, "空闲", "未使用"),
    IDLE_1B((byte)0x1B, "空闲", "未使用"),
    IDLE_1C((byte)0x1C, "空闲", "未使用"),
    IDLE_1D((byte)0x1D, "空闲", "未使用"),
    IDLE_1E((byte)0x1E, "空闲", "未使用"),
    IDLE_1F((byte)0x1F, "空闲", "未使用"),

    // 微小错误
    BATTERY_ERROR((byte)0x20, "微小错误", "电池错误"),
    COMM_ERROR((byte)0x21, "微小错误", "通讯错误"),
    CUSTOMER_STOP((byte)0x22, "微小错误", "Customer_Stop_Pressed（急停）"),
    IDLE_23((byte)0x23, "空闲", "未使用"),

    // 加油点错误
    AUTH_TIMEOUT((byte)0x24, "加油点错误", "授权超时"),
    MAX_TIME_REACHED((byte)0x25, "加油点错误", "达到加油最大允许时间"),
    NO_FUEL((byte)0x26, "加油点错误", "未加油"),
    LIMIT_REACHED((byte)0x27, "加油点错误", "达到限定量"),
    PUMP_SUSPEND((byte)0x28, "加油点错误", "加油挂起"),
    PUMP_CONTINUE((byte)0x29, "加油点错误", "加油继续"),

    // 状态错误
    OIL_RECOVERY_TIMER_START((byte)0x2A, "状态错误", "启动油气回收计时器"),
    OIL_RECOVERY_TIMER_RESET((byte)0x2B, "状态错误", "重置油气回收计时器"),
    OIL_RECOVERY_MODULE_ERROR((byte)0x2C, "状态错误", "油气回收模块错误"),
    STATUS_ERROR_1((byte)0x2D, "状态错误", "FP 在不可操作状态"),
    STATUS_ERROR_2((byte)0x2E, "状态错误", "FP 在关闭状态"),
    STATUS_ERROR_3((byte)0x2F, "状态错误", "FP 已经被打开"),
    STATUS_ERROR_4((byte)0x30, "状态错误", "交易没有进行"),
    STATUS_ERROR_5((byte)0x31, "状态错误", "交易已开始"),
    STATUS_ERROR_6((byte)0x32, "状态错误", "不可能进行参数/配置改变"),
    CONTROL_DEVICE_INVALID((byte)0x33, "状态错误", "控制设备标识符不正确"),
    UREA_TEMP_LOW((byte)0x34, "状态错误", "尿素温度低，加热器失效"),
    
    // 微小错误可选
    PRICE_MISMATCH((byte)0x35, "微小错误", "变价时油品不匹配"),
    MAINBOARD_NO_RESPONSE((byte)0x36, "微小错误", "加油机主板未响应变价命令"),
    PRICE_NOT_ALLOWED((byte)0x37, "微小错误", "此状态下不允许变价"),
    UREA_TEMP_HIGH((byte)0x38, "微小错误", "尿素温度过高，加热器失效"),
    IDLE_39((byte)0x39, "空闲", "未使用"),
    IDLE_3A((byte)0x3A, "空闲", "未使用"),
    // …39H~40H 空闲
    PRICE_CHANGE_SUCCESS((byte)0x99, "微小错误", "变价成功");

    private final byte code;
    private final String category;
    private final String description;

    ErrorCode(byte code, String category, String description) {
        this.code = code;
        this.category = category;
        this.description = description;
    }

    public byte getCode() { return code; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }

    public static ErrorCode fromByte(byte code) {
        for (ErrorCode e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null; // 未知或未定义
    }
}
