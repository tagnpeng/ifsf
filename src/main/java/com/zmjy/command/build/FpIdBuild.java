package com.zmjy.command.build;

import com.zmjy.command.Cache;
import com.zmjy.command.MsgUtil;
import com.zmjy.command.dto.Data;
import com.zmjy.command.dto.enums.FpIdEnums;
import com.zmjy.command.dto.enums.MsgType;
import com.zmjy.command.util.ByteConvertor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Map;

public class FpIdBuild {

    /**
     * FP 上逻辑油枪的数量。数量被接受的范围是 1 到 8。
     * 数据库: FP_ID
     * Data_id: 04H
     *
     * @param fpId 加油点标识
     * @param num  种数 （1～8）
     * @return {@link ByteBuf }
     */
    public static ByteBuf nbLogicalNozzle(int subNode, int node, int fpId, int num) {
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
        Data data = DataAddBuild.fpId(FpIdEnums.Nb_Logical_Nozzle, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = ByteConvertor.toBin(num, 1);
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
        return buf;
    }

    /**
     * 用来指定 FP 是否被分配给控制器，分配给哪个控制器。
     * 数据库: FP_ID
     * Data_id: 16H
     *
     * @param fpId  加油点标识
     * @param node  节点
     * @param sNode 子节点
     * @return {@link ByteBuf }
     */
    public static ByteBuf assignContrId(int fpId, int node, int sNode) {
        ByteBuf buf = Unpooled.buffer();

        //接收方逻辑节点地址：该字段为消息接收方的 LNA 2字节
        //接收方逻辑节点地址：该字段为消息接收方的 LNA 2字节
        buf.writeBytes(ByteConvertor.toBin(sNode, 1));
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
        Data data = DataAddBuild.fpId(FpIdEnums.Assign_Contr_Id, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = new byte[] {Cache.getInstance().getLocalSubNode(), Cache.getInstance().getLocalNode()};
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
        return buf;
    }

    /**
     * 打开一个关闭的 FP
     * 数据库: FP_ID
     * Data_id: 3CH
     *
     * @param fpId  加油点标识
     * @return {@link ByteBuf }
     */
    public static ByteBuf openFp(int subNode, int node, int fpId) {
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
        Data data = DataAddBuild.fpId(FpIdEnums.Open_FP, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = new byte[] {0x00};
//        msg.writeBytes(ByteConvertor.toBin(bytes.length, 1));

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
        return buf;
    }

    /**
     * 用来关闭一个 FP
     * 数据库: FP_ID
     * Data_id: 3DH
     *
     * @param fpId  加油点标识
     * @return {@link byte[] }
     */
    public static ByteBuf closeFp(int subNode, int node, int fpId) {
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
        Data data = DataAddBuild.fpId(FpIdEnums.Close_FP, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = new byte[] {0x00};

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
        return buf;
    }

    /**
     * 加油点的加油模式（FM_ID）
     * 数据库: FP_ID
     * Data_id: 21H
     *
     * @param fpId 加油点标识
     * @param fmId 加油模式(1-8)
     * @return {@link ByteBuf }
     */
    public static ByteBuf fuellingMode(int subNode, int node, int fpId, int fmId) {
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
        Data data = DataAddBuild.fpId(FpIdEnums.Fuelling_Mode, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = ByteConvertor.toBin(fmId, 1);
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
        return buf;
    }

    /**
     * 允许 CD 授权一个或多个逻辑油枪
     * 数据库: FP_ID
     * Data_id: 19H
     *
     * @param node       节点
     * @param sNode      s节点
     * @param fpId       加油点标识
     * @param lnIdStatus key: 逻辑油枪号 value: 逻辑油状态 1 = 油枪被授权;0 = 油枪不被授权
     * @return {@link ByteBuf }
     */
    public static ByteBuf logNozMask(int subNode, int node, int fpId, Map<Integer, Integer> lnIdStatus) {
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
        Data data = DataAddBuild.fpId(FpIdEnums.Fuelling_Mode, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = new byte[1];
        for (Map.Entry<Integer, Integer> entry : lnIdStatus.entrySet()) {
            int lnId = entry.getKey();
            int status = entry.getValue();
            if (status == 1) {
                // 授权 => 该位设置为1
                bytes[0] |= (byte) (1 << (lnId - 1));
            }
        }
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
     * 用来表明 FP 的状态。请参看加油点状态图中每个状态的详细情况
     * 数据库: FP_ID
     * Data_id: 14H
     *
     * @param node       节点
     * @param sNode      s节点
     * @param fpId       加油点标识
     * @return {@link ByteBuf }
     */
    public static ByteBuf fpState(int subNode, int node, int fpId) {
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
        Data data = DataAddBuild.fpId(FpIdEnums.FP_State, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());
        //----- 主体消息构建 end ------

        //消息长度：用于指明消息的字节数（数据库、数据）2字节
        buf.writeBytes(ByteConvertor.toBin(msg.readableBytes(), 2));

        //将消息主体写入
        buf.writeBytes(msg);

        return buf;
    }

    /**
     * 允许读取所有逻辑油枪的状态
     * 数据库: FP_ID
     * Data_id: 15H
     *
     * @param node       节点
     * @param sNode      s节点
     * @param fpId       加油点标识
     * @return {@link ByteBuf }
     */
    public static ByteBuf logNozState(int subNode, int node, int fpId) {
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
        Data data = DataAddBuild.fpId(FpIdEnums.Log_Noz_State, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());
        //----- 主体消息构建 end ------

        //消息长度：用于指明消息的字节数（数据库、数据）2字节
        buf.writeBytes(ByteConvertor.toBin(msg.readableBytes(), 2));

        //将消息主体写入
        buf.writeBytes(msg);

        return buf;
    }

    /**
     * 授权或预授权来启动一个交易
     * 数据库: FP_ID
     * Data_id: 3EH
     *
     * @param node       节点
     * @param subNode      s节点
     * @param fpId       加油点标识
     * @return {@link ByteBuf }
     */
    public static ByteBuf releaseFp(int subNode, int node, int fpId) {
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
        Data data = DataAddBuild.fpId(FpIdEnums.Release_FP, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = new byte[]{0x00};

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
     * 授权或预授权来启动一个交易
     * 数据库: FP_ID
     * Data_id: 3EH
     *
     * @param node       节点
     * @param subNode      s节点
     * @param fpId       加油点标识
     * @return {@link ByteBuf }
     */
    public static ByteBuf currentTrSeqNb(int subNode, int node, int fpId) {
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
        Data data = DataAddBuild.fpId(FpIdEnums.Current_TR_Seq_Nb, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = ByteConvertor.toBcd(1, 2);
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
     * ：描述未支付交易数
     * 数据库: FP_ID
     * Data_id: 02H
     *
     * @param subNode s节点
     * @param node    节点
     * @param fpId    加油点标识
     * @param msgType 消息类型
     * @return {@link ByteBuf }
     */
    public static ByteBuf nbTranBufferNotPaid(int subNode, int node, int fpId, MsgType msgType) {
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
        Data data = DataAddBuild.fpId(FpIdEnums.Nb_Tran_Buffer_Not_Paid, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = ByteConvertor.toBin(5, 1);
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
     * 为运行的交易指定释放 FP 的控制器
     * 数据库: FP_ID
     * Data_id: 1EH
     *
     * @param subNode s节点
     * @param node    节点
     * @param fpId    加油点标识
     * @param msgType 消息类型
     * @return {@link ByteBuf }
     */
    public static ByteBuf releaseContrId(int subNode, int node, int fpId, MsgType msgType) {
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
        Data data = DataAddBuild.fpId(FpIdEnums.Nb_Tran_Buffer_Not_Paid, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());

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
     * 这顶运行交易发送消息的频率，以10秒为单位 (0 = 未激活 1-999 = 以十秒为单位的延迟。例如：2代表有20秒的间隔)
     * 数据库: FP_ID
     * Data_id: 3BH
     *
     * @param subNode s节点
     * @param node    节点
     * @param fpId    加油点标识
     * @param num    频率 以秒为单位 (0 = 未激活 1-999 = 以1秒为单位的延迟。例如：2代表有2秒的间隔)
     * @return {@link ByteBuf }
     */
    public static ByteBuf runningTransactionMessageFrequency(int subNode, int node, int fpId, int num) {
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
        Data data = DataAddBuild.fpId(FpIdEnums.Running_Transaction_Message_Frequency, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
        byte[] bytes = ByteConvertor.toBcd(num, 2);
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
     * 这顶运行交易发送消息的频率，以10秒为单位 (0 = 未激活 1-999 = 以十秒为单位的延迟。例如：2代表有20秒的间隔)
     * 数据库: FP_ID
     * Data_id: 3BH
     *
     * @param subNode s节点
     * @param node    节点
     * @param fpId    加油点标识
     * @return {@link ByteBuf }
     */
    public static ByteBuf readRunningTransactionMessageFrequency(int subNode, int node, int fpId) {
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
        Data data = DataAddBuild.fpId(FpIdEnums.Running_Transaction_Message_Frequency, fpId);
        msg.writeByte(data.getDataAddLength());

        //数据库地址：用于指明所选设备的数据库。根据消息的类型数据库可以定位于发送方也可定位于接收方  1-8字节
        msg.writeBytes(data.getDataAdd());

        //数据标识符：应用数据元素的标识符 1字节
        msg.writeByte(data.getDataId());

        //数据长度：应用数据元素的长度。如果数据元素的长度大于 254 字节，那么 Data_Lg 将取值 255，并且之后的 2 个字节指明了数据长度 1或者3字节
//        byte[] bytes = ByteConvertor.toBcd(num, 2);
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


    public static void main(String[] args) {
        System.out.println(ByteConvertor.bcdToDecimal(ByteConvertor.hexStringToByteArray("01 01 00 00")));
        System.out.println(ByteConvertor.bcdToDecimal(ByteConvertor.hexStringToByteArray("00 12")));
        System.out.println(MsgUtil.bytesToHex(FpIdBuild.assignContrId(1, 1, 1)));
        System.out.println(MsgUtil.bytesToHex(FpIdBuild.releaseContrId(1, 1, 1, MsgType.WRITE)));
        System.out.println(MsgUtil.bytesToHex(FpIdBuild.currentTrSeqNb(1, 1, 1)));
        System.out.println("授权"+MsgUtil.bytesToHex(FpIdBuild.releaseFp(1, 1, 1)));
        System.out.println("关闭"+MsgUtil.bytesToHex(FpIdBuild.closeFp(1, 1, 1)));
        System.out.println("打开"+MsgUtil.bytesToHex(FpIdBuild.openFp(1, 1, 1)));
        System.out.println("读取逻辑油枪油品"+MsgUtil.bytesToHex(FpIdLnIdBuild.prId(1, 1, 1, 1, 1, MsgType.READ)));
        System.out.println("设置逻辑油枪油品"+MsgUtil.bytesToHex(FpIdLnIdBuild.prId(1, 1, 1, 1, 1, MsgType.WRITE)));
        System.out.println("读取油品编号"+MsgUtil.bytesToHex(PrIdBuild.prodNb(1, 1, 1, 1,  MsgType.READ)));
        System.out.println("读取价格"+MsgUtil.bytesToHex(PrDatProdNbFmIdBuild.redUpdatePrice(1, 1, 95, 1)));
        System.out.println("清除交易"+MsgUtil.bytesToHex(FpIdTrDatTrSeqNbBuild.clearTransaction(1, 1, 1, 21)));
        System.out.println("设置交易频率"+MsgUtil.bytesToHex(FpIdBuild.runningTransactionMessageFrequency(1, 1, 1, 1)));
        System.out.println("获取交易频率"+MsgUtil.bytesToHex(FpIdBuild.readRunningTransactionMessageFrequency(1, 1, 1)));
        System.out.println("设置主动通知"+MsgUtil.bytesToHex(ComSvBuild.add(1, 1)));
        //01 01 02 01 00 41 00 04 01 21 3D 00
        //01 01 02 01 00 46 00 05 01 21 3D 01 00
        //01 01 02 01 00 45 00 05 01 21 3E 01 00
        //授权
        //01 01 02 01 00 41 00 08 01 21 1e 02 01 01 3e 00
        //获取交易1
        //01 01 02 01 00 01 00 0C 04 21 21 00 01 01 05 06 07 08 CC CD
        //删除交易1
        //01 01 02 01 00 41 00 07 04 21 21 00 01 1e 00
        //删除交易2
        //01 01 02 01 00 41 00 07 04 21 21 00 02 1E 00
        //授权
        //01 01 02 01 00 42 00 05 01 21 3E 01 00
    }

}
