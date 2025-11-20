package com.zmjy.command.build;

import com.zmjy.command.MsgUtil;
import com.zmjy.command.dto.Data;
import com.zmjy.command.dto.enums.CDatEnums;
import com.zmjy.command.dto.enums.ComSvEnums;
import com.zmjy.command.dto.enums.FpIdEnums;
import com.zmjy.command.dto.enums.FpIdLnIdEnums;
import com.zmjy.command.dto.enums.FpIdTrDatTrSeqNbEnums;
import com.zmjy.command.dto.enums.PrDatProdNbFmIdEnums;
import com.zmjy.command.dto.enums.PrIdEnums;
import com.zmjy.command.util.ByteConvertor;
import java.util.List;

/**
 * 数据库地址构建
 *
 * @author tang
 * @date 2025/11/10
 */
public class DataAddBuild {

    public static Data ComSv(ComSvEnums comSvEnums) {
        Data data = new Data();
        data.setDataAdd(new byte[]{0x00});
        data.setDataId(new byte[]{comSvEnums.getDataId()});
        return data;
    }

    /**
     * 计算器数据库协议构建
     *
     * @param cDatEnums dataId枚举
     * @return {@link Data }
     */
    public static Data CDat(CDatEnums cDatEnums) {
        Data data = new Data();
        data.setDataAdd(new byte[]{0x01});
        data.setDataId(new byte[]{cDatEnums.getDataId()});
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
        data.setDataId(new byte[]{prIdEnums.getDataId()});
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
        data.setDataId(new byte[]{fpIdEnums.getDataId()});
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
        data.setDataId(new byte[]{fpIdLnIdEnums.getDataId()});
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
        data.setDataId(new byte[]{prDatProdNbFmIdEnums.getDataId()});
        return data;
    }

    /**
     * 加油模式下的油品数据库协议构建
     *
     * @param fpIdTrDatTrSeqNbEnums 枚举
     * @param fpId 加油点表示
     * @param trSeqNb 交易号
     * @return {@link Data }
     */
    public static Data FpIdTrDatTrSeqNb(FpIdTrDatTrSeqNbEnums fpIdTrDatTrSeqNbEnums, int fpId, int trSeqNb) {
        byte[] dataAdd = new byte[1 + 1 + 2];
        System.arraycopy(ByteConvertor.toBcd(20 + fpId, 1), 0, dataAdd, 0, 1);
        dataAdd[1] = (byte) 0x21; // 固定标识
        System.arraycopy(ByteConvertor.toBcd(trSeqNb, 2), 0, dataAdd, 2, 2);
        Data data = new Data();
        data.setDataAdd(dataAdd);
        data.setDataId(new byte[]{fpIdTrDatTrSeqNbEnums.getDataId()});
        return data;
    }

    /**
     * 加油模式下的油品数据库协议构建
     *
     * @param fpIdTrDatTrSeqNbEnums 枚举
     * @param fpId 加油点表示
     * @return {@link Data }
     */
    public static Data FpIdTrDatTrSeqNb20(List<FpIdTrDatTrSeqNbEnums> fpIdTrDatTrSeqNbEnums, int fpId) {
        byte[] dataAdd = new byte[1 + 1 + 2];
        System.arraycopy(ByteConvertor.toBcd(20 + fpId, 1), 0, dataAdd, 0, 1);
        dataAdd[1] = (byte) 0x20; // 固定标识 = 20H被用来访问处在Payable（状态2）和Locked（状态3）的加油点的所有交易
        System.arraycopy(ByteConvertor.toBcd(0, 2), 0, dataAdd, 2, 2);
        Data data = new Data();
        data.setDataAdd(dataAdd);
        byte[] bytes = new byte[fpIdTrDatTrSeqNbEnums.size()];
        for (int i = 0; i < fpIdTrDatTrSeqNbEnums.size(); i++) {
            bytes[i] = fpIdTrDatTrSeqNbEnums.get(i).getDataId();
        }
        data.setDataId(bytes);
        return data;
    }
}
