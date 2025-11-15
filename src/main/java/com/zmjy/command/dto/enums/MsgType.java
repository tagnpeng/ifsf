package com.zmjy.command.dto.enums;

public enum MsgType {
    READ,               // 读消息
    RESPONSE,           // 应答消息
    WRITE,              // 写消息
    ACTIVE_WITH_ACK,    // 带确认的主动数据消息
    ACTIVE_NO_ACK,      // 不带确认的主动数据消息
    CONFIRM             // 确认消息
    ;

    /**
     * 根据消息类型字节解析出 MsgType
     */
    public MsgType parseTypeByte(byte typeByte) {
        // 将 byte 转为无符号 int
        int bits = (typeByte >> 5) & 0b111;

        switch (bits) {
            case 0b000:
                return MsgType.READ;
            case 0b001:
                return MsgType.RESPONSE;
            case 0b010:
                return MsgType.WRITE;
            case 0b011:
                return MsgType.ACTIVE_WITH_ACK;
            case 0b100:
                return MsgType.ACTIVE_NO_ACK;
            case 0b111:
                return MsgType.CONFIRM;
            default:
                throw new IllegalArgumentException("未知消息类型 bits: " + bits);
        }
    }
    }