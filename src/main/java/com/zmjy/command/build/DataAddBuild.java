package com.zmjy.command.build;

import com.zmjy.command.dto.Data;
import com.zmjy.command.dto.enums.CDatEnums;
import com.zmjy.command.dto.enums.FpIdEnums;
import com.zmjy.command.dto.enums.FpIdLnIdEnums;
import com.zmjy.command.dto.enums.PrDatProdNbFmIdEnums;
import com.zmjy.command.dto.enums.PrIdEnums;
import com.zmjy.command.util.ByteConvertor;

/**
 * 数据库地址构建
 *
 * @author tang
 * @date 2025/11/10
 */
public class DataAddBuild {

    /**
     * 计算器数据库协议构建
     *
     * @param cDatEnums dataId枚举
     * @return {@link Data }
     */
    public static Data CDat(CDatEnums cDatEnums) {
        Data data = new Data();
        data.setDataAdd(new byte[]{0x01});
        data.setDataId(cDatEnums.getDataId());
        return data;
    }

    /**
     * 油品数据库协议构建
     *
     * @param prIdEnums dataId枚举
     * @param prId      油品标识(1-8)
     * @return {@link Data }
     */
    public static Data prId(PrIdEnums prIdEnums, int prId) {
        Data data = new Data();
        data.setDataAdd(ByteConvertor.toBcd(40 + prId, 1));
        data.setDataId(prIdEnums.getDataId());
        return data;
    }

    /**
     * 加油点数据库协议构建
     *
     * @param fpIdEnums dataId枚举
     * @param fpId      加油点表示(1-4)
     * @return {@link Data }
     */
    public static Data fpId(FpIdEnums fpIdEnums, int fpId) {
        Data data = new Data();
        data.setDataAdd(ByteConvertor.toBcd(20 + fpId, 1));
        data.setDataId(fpIdEnums.getDataId());
        return data;
    }

    /**
     * 逻辑油枪数据库协议构建
     *
     * @param fpIdLnIdEnums dataId枚举
     * @param fpId      加油点表示(1-4)
     * @param lnId      逻辑油枪标识(1-8)
     * @return {@link Data }
     */
    public static Data fpIdLnId(FpIdLnIdEnums fpIdLnIdEnums, int fpId, int lnId) {
        byte[] dataAdd = new byte[2];
        System.arraycopy(ByteConvertor.toBcd(20 + fpId, 1), 0, dataAdd, 0, 1);
        System.arraycopy(ByteConvertor.toBcd(10 + fpId, 1), 0, dataAdd, 1, 1);
        Data data = new Data();
        data.setDataAdd(dataAdd);
        data.setDataId(fpIdLnIdEnums.getDataId());
        return data;
    }

    /**
     * 加油模式下的油品数据库协议构建
     *
     * @param prDatProdNbFmIdEnums dataId枚举
     * @param prodNb     油品编码
     * @param fmId       加油模式标识
     * @return {@link Data }
     */
    public static Data PrDat(PrDatProdNbFmIdEnums prDatProdNbFmIdEnums, int prodNb, int fmId) {
        byte[] dataAdd = new byte[1 + 4 + 1];
        dataAdd[0] = 0x61; // 固定标识
        System.arraycopy(ByteConvertor.toBcd(prodNb, 4), 0, dataAdd, 1, 4);
        System.arraycopy(ByteConvertor.toBcd(10 + fmId, 1), 0, dataAdd, 5, 1);
        Data data = new Data();
        data.setDataAdd(dataAdd);
        data.setDataId(prDatProdNbFmIdEnums.getDataId());
        return data;
    }
}
