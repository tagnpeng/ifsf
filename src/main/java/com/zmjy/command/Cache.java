package com.zmjy.command;

import com.zmjy.command.dto.Heartbeat;
import com.zmjy.command.dto.enums.MsgType;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

@Data
public class Cache {

    //---------- upd信息缓存-------------
    private Boolean receiveHeartbeat = Boolean.FALSE;
    private Heartbeat heartbeat;

    //---------- tcp-client信息缓存 ----------
    //是否初始化
    private Boolean isInitialized = Boolean.FALSE;
    //用于构建token,线程安全的int
    private static final AtomicInteger token = new AtomicInteger(1);
    //油机节点
    private byte remoteNode = 1 & 0xFF;
    private byte remoteSubNode = 11 & 0xFF;
    //本机节点
    private byte localNode = 3 & 0xFF;
    private byte localSubNode = 4 & 0xFF;



    private Cache() {

    }

    private static class CacheHolder {

        private static final Cache INSTANCE = new Cache();
    }

    public static synchronized Cache getInstance() {
        return CacheHolder.INSTANCE;
    }


    private synchronized int getToken() {
        if (token.get() == 31) {
            token.compareAndSet(31, 1);
            return token.get();
        } else {
            return token.addAndGet(1);
        }
    }

    /**
     * 构建 1 字节消息类型字段
     * bit8~6 表示类型，bit1~5 为 token（固定为 1）
     */
    public byte buildTypeByte(MsgType type) {
        int token = getToken();  // 固定 Token = 1
        int bits = 0;

        switch (type) {
            case READ:
                bits = 0b000; // 第8~6位 = 000
                break;
            case RESPONSE:
                bits = 0b001; // 001
                break;
            case WRITE:
                bits = 0b010; // 010
                break;
            case ACTIVE_WITH_ACK:
                bits = 0b011; // 011
                break;
            case ACTIVE_NO_ACK:
                bits = 0b100; // 100
                break;
            case CONFIRM:
                bits = 0b111; // 111
                break;
            default:
                throw new IllegalArgumentException("未知消息类型: " + type);
        }

        // 左移5位，把bits放到高3位，再加上低5位token
        int value = (bits << 5) | (token & 0b11111);

        return (byte) (value & 0xFF);
    }

}
