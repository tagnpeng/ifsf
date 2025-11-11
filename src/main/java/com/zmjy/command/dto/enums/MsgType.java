package com.zmjy.command.dto.enums;

public enum MsgType {
    READ,               // 读消息
    RESPONSE,           // 应答消息
    WRITE,              // 写消息
    ACTIVE_WITH_ACK,    // 带确认的主动数据消息
    ACTIVE_NO_ACK,      // 不带确认的主动数据消息
    CONFIRM             // 确认消息
}