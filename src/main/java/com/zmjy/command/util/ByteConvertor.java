package com.zmjy.command.util;

import java.nio.charset.StandardCharsets;

/**
 * 进制转换
 *
 * @author tang
 * @date 2025/11/10
 */
public class ByteConvertor {
    /**
     * 将字符串转为固定长度的 ASCII 字节数组（右侧补 0x00，不足补零，超出截断）
     * @param value 要转换的字符串
     * @param binLength 字节数组长度
     * @return 固定长度的 byte[]
     */
    public static byte[] toAsc(String value, int binLength) {
        if (value == null) {
            throw new IllegalArgumentException("value 不能为空");
        }
        if (binLength <= 0) {
            throw new IllegalArgumentException("binLength 必须大于0");
        }

        // 1️⃣ 转成 ASCII 编码字节
        byte[] src = value.getBytes(StandardCharsets.US_ASCII);

        // 2️⃣ 构造固定长度数组
        byte[] result = new byte[binLength];

        // 3️⃣ 拷贝或截断
        int len = Math.min(src.length, binLength);
        System.arraycopy(src, 0, result, 0, len);

        // 若不足则右侧自动补0x00（数组初始化默认全是0）
        return result;
    }

    /**
     * 将整数转为指定长度的二进制字节数组（大端，高位在前），不足补零
     * @param value 十进制整数
     * @param binLength 字节数组长度
     * @return 二进制字节数组
     */
    public static byte[] toBin(int value, int binLength) {
        if (binLength <= 0) {
            throw new IllegalArgumentException("binLength 必须大于 0");
        }

        byte[] result = new byte[binLength];
        for (int i = 0; i < binLength; i++) {
            // 大端模式，高位在前
            result[binLength - 1 - i] = (byte) ((value >> (8 * i)) & 0xFF);
        }
        return result;
    }

    /**
     * 十进制转 BCD
     *
     * @param value     十进制整数
     * @param bcdLength 指定 BCD 字节长度
     * @return BCD 字节数组，高位在前
     */
    public static byte[] toBcd(int value, int bcdLength) {
        if (value < 0) {
            throw new IllegalArgumentException("value 不能为负数");
        }

        // 1️⃣ 转成字符串
        String str = Long.toString(value);

        // 2️⃣ 每个字节两个数字 → 最大可表示 bcdLength * 2 位
        if (str.length() > bcdLength * 2) {
            throw new IllegalArgumentException("数字太大，无法用指定长度表示");
        }

        // 3️⃣ 左补零到 bcdLength*2 位
        str = String.format("%" + (bcdLength * 2) + "s", str).replace(' ', '0');

        // 4️⃣ 转 BCD
        byte[] bcd = new byte[bcdLength];
        for (int i = 0; i < bcdLength; i++) {
            int high = str.charAt(i * 2) - '0';
            int low = str.charAt(i * 2 + 1) - '0';
            bcd[i] = (byte) ((high << 4) | low);
        }

        return bcd;
    }

    /**
     * BCD 转十进制整数
     *
     * @param bcd BCD 字节数组，高位在前
     * @return 十进制整数
     */
    public static int bcdToDecimal(byte[] bcd) {
        if (bcd == null || bcd.length == 0) {
            throw new IllegalArgumentException("bcd 不能为空");
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bcd) {
            // 高 4 位
            int high = (b >> 4) & 0x0F;
            // 低 4 位
            int low = b & 0x0F;
            sb.append(high).append(low);
        }

        // 转整数
        return Integer.parseInt(sb.toString());
    }

    /**
     * 将指定长度的大端二进制字节数组转换成十进制整数
     * @param bytes 大端二进制字节数组，高位在前
     * @return 十进制整数
     */
    public static int binToDecimal(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("bytes 不能为空");
        }
        int value = 0;
        for (int i = 0; i < bytes.length; i++) {
            value <<= 8;               // 左移 8 位给下一字节腾位置
            value |= (bytes[i] & 0xFF); // 转成无符号再合并
        }
        return value;
    }

    /**
     * 将固定长度 ASCII 字节数组转换成字符串（去掉右侧 0x00）
     *
     * @param bytes ASCII 字节数组
     * @return 字符串
     */
    public static String ascToString(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes 不能为空");
        }

        // 找到右侧第一个非 0x00 的位置
        int len = bytes.length;
        while (len > 0 && bytes[len - 1] == 0x00) {
            len--;
        }

        // 根据有效长度创建字符串
        return new String(bytes, 0, len, StandardCharsets.US_ASCII);
    }

    public static byte[] hexStringToByteArray(String hex) {
        if (hex == null || hex.trim().isEmpty()) {
            throw new IllegalArgumentException("输入的 hex 字符串不能为空");
        }

        // 去除所有空格和非16进制字符
        hex = hex.replaceAll("[^0-9A-Fa-f]", "");

        // 校验长度
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex 字符串长度必须为偶数: " + hex);
        }

        int len = hex.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("无效的16进制字符: " + hex.substring(i, i + 2));
            }
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }
}
