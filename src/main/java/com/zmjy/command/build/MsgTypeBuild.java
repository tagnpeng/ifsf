package com.zmjy.command.build;

import com.zmjy.command.util.ByteConvertor;
import java.math.BigDecimal;

/**
 * 指定消息类型构建
 * 常见消息类型
 * a) bin8：符号位和小数点位置（左数），0 表示正值，1 表示负值；
 * b) bit7～1：小数点位置（左数），取值范围 0～127；
 * c) bcdx：使用 bcd 码的值（每字节两个 bcd 码）。
 * 浮点数举例：
 * 06，12，34，56，78=123456.78；
 * 0B，12，34，56，78=12345678000；
 * 82，12，34=-12.34。
 *
 * @author tang
 * @date 2025/11/10
 */
public class MsgTypeBuild {

    //bin8+bcd6 单价值（用于加油交易数据）
    public static byte[] unitPrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("price 不能为空");
        }

        // 符号位
        byte signBit = price.signum() < 0 ? (byte) 1 : 0;

        // 绝对值
        BigDecimal absPrice = price.abs();

        // 整数位长度（左数）
        int integerDigits = absPrice.precision() - absPrice.scale();
        if (integerDigits < 0) {
            integerDigits = 0; // 全小数情况
        }

        if (integerDigits > 127) {
            throw new IllegalArgumentException("整数位过长，超过 7 位");
        }

        // 价格转换成整数（去掉小数点）
        BigDecimal multiplier = BigDecimal.TEN.pow(absPrice.scale());
        int priceInt = absPrice.multiply(multiplier).intValue();

        byte[] bcd = ByteConvertor.toBcd(priceInt, 3);

        String str = Long.toString(priceInt);
        //构建 bin8, 需要考虑补零的数量(6 - str.length())
        byte bin8 = (byte) ((signBit << 7) | ((integerDigits + (6 - str.length())) & 0x7F));

        // 组合 bin8 + BCD6
        byte[] result = new byte[1 + 3];
        result[0] = bin8;
        System.arraycopy(bcd, 0, result, 1, 3);
        return result;
    }

}