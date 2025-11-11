package com.zmjy.command;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;

public class MsgUtil {

    public static String bytesToHex(byte b) {
        return String.format("%02X ", b).trim();
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    public static String bytesToHex(ByteBuf buf) {
        int length = buf.writerIndex(); // 只取实际写入的数据长度
        byte[] bytes = new byte[length];
        buf.getBytes(0, bytes, 0, length);
        return bytesToHex(bytes);
    }

    public static String bytesToHex(DatagramPacket packet) {
        ByteBuf buf = packet.content();
        return bytesToHex(packet.content());
    }
}
