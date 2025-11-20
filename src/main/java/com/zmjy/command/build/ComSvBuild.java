package com.zmjy.command.build;

import com.zmjy.command.Cache;
import com.zmjy.command.MsgUtil;
import com.zmjy.command.dto.Data;
import com.zmjy.command.dto.enums.ComSvEnums;
import com.zmjy.command.dto.enums.FpIdTrDatTrSeqNbEnums;
import com.zmjy.command.dto.enums.MsgType;
import com.zmjy.command.util.ByteConvertor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ComSvBuild {

    public static void main(String[] args) {
        System.out.println(""+MsgUtil.bytesToHex(readNode(1,1)));
        System.out.println("查询主动推送地址"+MsgUtil.bytesToHex(query(1,1)));
        System.out.println("删除主动推送地址"+MsgUtil.bytesToHex(remove(1,1)));
        System.out.println("主动推送地址"+MsgUtil.bytesToHex(add(2, 1)));
        System.out.println("获取交易频率"+MsgUtil.bytesToHex(FpIdBuild.readRunningTransactionMessageFrequency(1, 1, 1)));
        System.out.println("设置交易频率"+MsgUtil.bytesToHex(FpIdBuild.runningTransactionMessageFrequency(1, 1, 1, 1)));
        System.out.println("关闭FP"+MsgUtil.bytesToHex(FpIdBuild.closeFp(1,1, 1)));
        System.out.println("授权"+MsgUtil.bytesToHex(FpIdBuild.releaseFp(1,1, 1)));
        System.out.println("读取当前交易"+MsgUtil.bytesToHex(FpIdBuild.currentTrSeqNb(1, 1, 1)));
        System.out.println("获取所有交易"+MsgUtil.bytesToHex(FpIdTrDatTrSeqNbBuild.readAllTransaction(1, 1, 1)));
        System.out.println("删除交易"+MsgUtil.bytesToHex(FpIdTrDatTrSeqNbBuild.clearTransaction(1, 1, 1, 41)));
        //01 01 02 01 00 42 00 06 04 21 21 00 41 1E
        //01 01 02 01 00 5C 00 06 04 21 21 02 00 1E
    }

    public static ByteBuf readNode(int subNode, int node) {
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
        buf.writeByte(Cache.getInstance().buildTypeByte(MsgType.READ));

        //----- 主体消息构建 start ------
        ByteBuf msg = Unpooled.buffer();
        //数据库地址长度：用于指明一个设备的数据库的地址的字节数 1字节
        Data data = DataAddBuild.ComSv(ComSvEnums.read);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeBytes(data.getDataId());
        //----- 主体消息构建 end ------

        //消息长度：用于指明消息的字节数（数据库、数据）2字节
        buf.writeBytes(ByteConvertor.toBin(msg.readableBytes(), 2));

        //将消息主体写入
        buf.writeBytes(msg);
        return buf;
    }

    /**
     * 添加主动消息地址
     * 数据库: COM_SV
     * Data_id: 0B
     *
     * @return {@link byte[] }
     */
    public static ByteBuf add(int subNode, int node) {
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
        Data data = DataAddBuild.ComSv(ComSvEnums.ADD);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeBytes(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = new byte[]{Cache.getInstance().getLocalSubNode(), Cache.getInstance().getLocalNode()};
        msg.writeBytes(ByteConvertor.toBin(bytes.length, 1));

        //数据元素 n字节
        msg.writeBytes(bytes);
        //----- 主体消息构建 end ------

        //消息长度：用于指明消息的字节数（数据库、数据）2字节
        buf.writeBytes(ByteConvertor.toBin(msg.readableBytes(), 2));

        //将消息主体写入
        buf.writeBytes(msg);
        return buf;
    }

    /**
     * 添加主动消息地址
     * 数据库: COM_SV
     * Data_id: 0B
     *
     * @return {@link byte[] }
     */
    public static ByteBuf remove(int subNode, int node) {
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
        Data data = DataAddBuild.ComSv(ComSvEnums.REMOVE);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeBytes(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = new byte[]{Cache.getInstance().getLocalSubNode(), Cache.getInstance().getLocalNode()};
        msg.writeBytes(ByteConvertor.toBin(bytes.length, 1));

        //数据元素 n字节
        msg.writeBytes(bytes);
        //----- 主体消息构建 end ------

        //消息长度：用于指明消息的字节数（数据库、数据）2字节
        buf.writeBytes(ByteConvertor.toBin(msg.readableBytes(), 2));

        //将消息主体写入
        buf.writeBytes(msg);
        return buf;
    }

    /**
     * 添加主动消息地址
     * 数据库: COM_SV
     * Data_id: 0B
     *
     * @return {@link byte[] }
     */
    public static ByteBuf query(int subNode, int node) {
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
        buf.writeByte(Cache.getInstance().buildTypeByte(MsgType.READ));

        //----- 主体消息构建 start ------
        ByteBuf msg = Unpooled.buffer();
        //数据库地址长度：用于指明一个设备的数据库的地址的字节数 1字节
        Data data = DataAddBuild.ComSv(ComSvEnums.query);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeBytes(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
//        byte[] bytes = new byte[]{Cache.getInstance().getLocalSubNode(), Cache.getInstance().getLocalNode()};
//        msg.writeBytes(ByteConvertor.toBin(bytes.length, 1));

        //数据元素 n字节
//        msg.writeBytes(bytes);
        //----- 主体消息构建 end ------

        //消息长度：用于指明消息的字节数（数据库、数据）2字节
        buf.writeBytes(ByteConvertor.toBin(msg.readableBytes(), 2));

        //将消息主体写入
        buf.writeBytes(msg);
        return buf;
    }
}
