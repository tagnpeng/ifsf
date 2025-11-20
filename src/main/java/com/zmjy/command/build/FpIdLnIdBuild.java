package com.zmjy.command.build;

import com.zmjy.command.Cache;
import com.zmjy.command.dto.Data;
import com.zmjy.command.dto.enums.FpIdLnIdEnums;
import com.zmjy.command.dto.enums.MsgType;
import com.zmjy.command.util.ByteConvertor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class FpIdLnIdBuild {
    /**
     * 用来标识逻辑油枪加出的油品
     * 数据库: FP_ID+LN_ID
     * Data_id: 01H
     *
     * @param fpId  加油点标识(1-4)
     * @param lnId  逻辑油枪标识(1-8)
     * @param prId  油品标识(1-8)
     * @return {@link ByteBuf }
     */
    public static ByteBuf prId(int subNode, int node, int fpId, int lnId, int prId, MsgType msgType) {
        ByteBuf buf = Unpooled.buffer();

        //接收方逻辑节点地址：该字段为消息接收方的 LNA 2字节
        //接收方逻辑节点地址：该字段为消息接收方的 LNA 2字节
        buf.writeBytes(ByteConvertor.toBin(subNode, 1));
        buf.writeBytes(ByteConvertor.toBin(node, 1));

        //发送方逻辑节点地址：该字段为消息发送方的 LNA 2字节
        buf.writeByte(Cache.getInstance().getLocalSubNode());
        buf.writeByte(Cache.getInstance().getLocalNode());

        //消息代码：用于对通信层接收的数据进行过滤，固定0x00 1字节
        buf.writeByte(0x00);

        //消息状态字：定义消息的类型，并且包含令牌（token）1字节
        buf.writeByte(Cache.getInstance().buildTypeByte(msgType));

        //----- 主体消息构建 start ------
        ByteBuf msg = Unpooled.buffer();
        //数据库地址长度：用于指明一个设备的数据库的地址的字节数 1字节
        Data data = DataAddBuild.fpIdLnId(FpIdLnIdEnums.PR_Id, fpId, lnId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeBytes(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = ByteConvertor.toBin(prId, 1);
        msg.writeBytes(ByteConvertor.toBin(bytes.length, 1));

        //数据元素 n字节
        msg.writeBytes(bytes);
        //----- 主体消息构建 end ------

        //消息长度：用于指明消息的字节数（数据库、数据）2字节
        buf.writeBytes(ByteConvertor.toBin(msg.readableBytes(), 2));

        //将消息主体写入
        buf.writeBytes(msg);

//        int length = buf.readableBytes();
//        byte[] validBytes = new byte[length];
//        buf.getBytes(buf.readerIndex(), validBytes);
//        buf.release();
//        return validBytes;
        return buf;
    }
    /**
     * 为该逻辑油枪指定对应物理油枪标识
     * 数据库: FP_ID+LN_ID
     * Data_id: 05H
     *
     * @param fpId  加油点标识(1-4)
     * @param lnId  逻辑油枪标识(1-8)
     * @param gunNo 物理油枪标识(1-8)
     * @return {@link ByteBuf }
     */
    public static ByteBuf physicalNozId(int subNode, int node, int fpId, int lnId, int gunNo) {
        ByteBuf buf = Unpooled.buffer();

        //接收方逻辑节点地址：该字段为消息接收方的 LNA 2字节
        //接收方逻辑节点地址：该字段为消息接收方的 LNA 2字节
        buf.writeBytes(ByteConvertor.toBin(subNode, 1));
        buf.writeBytes(ByteConvertor.toBin(node, 1));

        //发送方逻辑节点地址：该字段为消息发送方的 LNA 2字节
        buf.writeByte(Cache.getInstance().getLocalSubNode());
        buf.writeByte(Cache.getInstance().getLocalNode());

        //消息代码：用于对通信层接收的数据进行过滤，固定0x00 1字节
        buf.writeByte(0x00);

        //消息状态字：定义消息的类型，并且包含令牌（token）1字节
        buf.writeByte(Cache.getInstance().buildTypeByte(MsgType.WRITE));

        //----- 主体消息构建 start ------
        ByteBuf msg = Unpooled.buffer();
        //数据库地址长度：用于指明一个设备的数据库的地址的字节数 1字节
        Data data = DataAddBuild.fpIdLnId(FpIdLnIdEnums.Physical_Noz_Id, fpId, lnId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeBytes(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = ByteConvertor.toBin(gunNo, 1);
        msg.writeBytes(ByteConvertor.toBin(bytes.length, 1));

        //数据元素 n字节
        msg.writeBytes(bytes);
        //----- 主体消息构建 end ------

        //消息长度：用于指明消息的字节数（数据库、数据）2字节
        buf.writeBytes(ByteConvertor.toBin(msg.readableBytes(), 2));

        //将消息主体写入
        buf.writeBytes(msg);

//        int length = buf.readableBytes();
//        byte[] validBytes = new byte[length];
//        buf.getBytes(buf.readerIndex(), validBytes);
//        buf.release();
//        return validBytes;
        return buf;
    }
}
