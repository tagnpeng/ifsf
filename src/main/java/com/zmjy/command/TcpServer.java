package com.zmjy.command;

import com.zmjy.command.dto.enums.ErrorCode;
import com.zmjy.command.dto.enums.FpStatusEnums;
import com.zmjy.command.dto.enums.MsgType;
import com.zmjy.command.util.ByteConvertor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.Buffer;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "tcp-server")
public class TcpServer {

    @Getter
    private Channel channel;

    public void tpcServer(String localIp, Integer port) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap.group(group, workerGroup)
            // 指定通道类型为 NIO 模式（非阻塞）
            .channel(NioServerSocketChannel.class)
            // 配置子通道（每一个连接）的 pipeline 初始化逻辑
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    // 自定义ChannelInboundHandlerAdapter
                    pipeline.addLast(simpleChannelInboundHandler());
                }
            })
            // 设置 TCP 参数（服务端接受连接队列大小）
            .option(ChannelOption.SO_BACKLOG, 128)
            // 开启 TCP KeepAlive，保持长连接
            .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(localIp, port);
        boolean success = future.await(3, TimeUnit.SECONDS);
        if (!success) {
            log.info("服务连接{}超时", localIp + ":" + port);
            throw new RuntimeException("tcp-server服务连接超时");
        } else {
            if (future.isSuccess()) {
                log.info("服务启动成功:{}", localIp + ":" + port);
                channel = future.channel();
            } else {
                log.info("服务启动失败", future.cause());
                throw new RuntimeException("tcp-server服务启动失败");
            }
        }
    }

    public ByteToMessageDecoder simpleChannelInboundHandler() {
        return new ByteToMessageDecoder() {
            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
                try {
                    InetSocketAddress inetSocket = (InetSocketAddress) ctx.channel().remoteAddress();
                    String ip = inetSocket.getAddress().getHostAddress();
                    int port = inetSocket.getPort();
                    customizeChannelRead(buf, ip, port);
                } catch (Exception e) {
                    log.error("服务端处理数据异常", e);
                }
            }

            @Override
            public void handlerAdded(ChannelHandlerContext ctx) {
                InetSocketAddress inetSocket = (InetSocketAddress) ctx.channel().remoteAddress();
                String ip = inetSocket.getAddress().getHostAddress();
                int port = inetSocket.getPort();
                log.info("有新连接注册:{}", ip + ":" + port);
            }

        };
    }

    public static void main(String[] args) {
        byte[] bytes = ByteConvertor.hexStringToByteArray("03 01 01 01 00 FB 00 06 04 21 20 00 00 00");
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(bytes);

        TcpServer tcpServer = new TcpServer();
        tcpServer.customizeChannelRead(buf, "", 1);
    }

    private void customizeChannelRead(ByteBuf buf, String ip, int port) {
        while (buf.readableBytes() > 0) {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
//            if (buf.readableBytes() < 13) {
//                log.info("接收到不完整消息 {}", MsgUtil.bytesToHex(bytes));
//                return;
//            }
            //测试不考虑粘包和分包
//            log.info("接收到协议 原始数据:{}", MsgUtil.bytesToHex(bytes));

            //标记开始位置，用于回滚消息
            buf.markReaderIndex();
            //接收方逻辑节点地址
            buf.readByte();
            buf.readByte();

            //发送方逻辑节点地址
            buf.readByte();
            buf.readByte();

            //消息代码
            buf.readByte();

            //消息状态 todo 只处理应答消息
            byte mSt = buf.readByte();
            MsgType msgType = Cache.getInstance().parseTypeByte(mSt);
//            byte[] bytes1 = new byte[buf.readableBytes()];
//            buf.readBytes(bytes1);
//            log.info("本条消息为: {} 后续字段为: {}", msgType.name(), MsgUtil.bytesToHex(bytes1));
//            if (1 == 1) {
//                return;
//            }
            switch (msgType) {
                case CONFIRM: {
                    //消息长度
                    int msgLength = ((buf.readByte() << 8) & 0xFF) | ((buf.readByte()) & 0xFF);
                    ByteBuf msgBuf = buf.readBytes(msgLength);
                    //数据库地址长度
                    int dataAddLength = msgBuf.readByte() & 0xFF;
                    //数据库地址
                    ByteBuf dataAdd = msgBuf.readBytes(dataAddLength);
                    //消息确认状态
                    int msgAck = msgBuf.readByte() & 0xFF;

                    //下面的就是详情，如果消息确认状态的取值为 0～3、6 或者 9，则在确认消息中没有附加信息要被发送
                    ByteBuf msgDetail = msgBuf.readBytes(msgBuf.readableBytes());

                    log.info("({})本条消息为确认消息,原始数据:{} 消息长度为:{} 数据库地址长度为:{} 数据库地址为:{} 消息确认状态为:{} 消息详情为:{}",
                        ip + ":" + port, MsgUtil.bytesToHex(bytes), msgLength,
                        dataAddLength, MsgUtil.bytesToHex(dataAdd), msgAck, MsgUtil.bytesToHex(msgDetail));

                    msgBuf.release();
                    dataAdd.release();
                    msgDetail.release();
                    break;
                }

                case RESPONSE: {
                    //消息长度
                    int msgLength = ((buf.readByte() << 8) & 0xFF) | ((buf.readByte()) & 0xFF);
                    ByteBuf msgBuf = buf.readBytes(msgLength);
                    //数据库地址长度
                    int dataAddLength = msgBuf.readByte() & 0xFF;
                    //数据库地址
                    ByteBuf dataAdd = msgBuf.readBytes(dataAddLength);

                    //下面的就是详情，如果消息确认状态的取值为 0～3、6 或者 9，则在确认消息中没有附加信息要被发送
                    ByteBuf msgDetail = msgBuf.readBytes(msgBuf.readableBytes());

                    log.info("({})本条消息为应答消息,原始数据:{} 消息长度为:{} 数据库地址长度为:{} 数据库地址为:{} 消息详情为:{}", ip + ":" + port,
                        MsgUtil.bytesToHex(bytes), msgLength,
                        dataAddLength, MsgUtil.bytesToHex(dataAdd), MsgUtil.bytesToHex(msgDetail));

                    msgBuf.release();
                    dataAdd.release();
                    msgDetail.release();
                    break;
                }
                case ACTIVE_NO_ACK:{
                    //消息长度
                    int msgLength = ((buf.readByte() << 8) & 0xFF) | ((buf.readByte()) & 0xFF);
                    ByteBuf msgBuf = buf.readBytes(msgLength);
                    //数据库地址长度
                    int dataAddLength = msgBuf.readByte() & 0xFF;
                    //数据库地址
                    ByteBuf dataAdd = msgBuf.readBytes(dataAddLength);

                    //下面的就是详情，如果消息确认状态的取值为 0～3、6 或者 9，则在确认消息中没有附加信息要被发送
                    ByteBuf msgDetail = msgBuf.readBytes(msgBuf.readableBytes());

                    log.info("({})本条消息为不带确认的主动消息,原始数据:{} 消息长度为:{} 数据库地址长度为:{} 数据库地址为:{} 消息详情为:{}", ip + ":" + port,
                        MsgUtil.bytesToHex(bytes), msgLength,
                        dataAddLength, MsgUtil.bytesToHex(dataAdd), MsgUtil.bytesToHex(msgDetail));

                    msgBuf.release();
                    dataAdd.release();
                    msgDetail.release();
                    break;
                }
                case ACTIVE_WITH_ACK:{
                    //消息长度
                    int msgLength = ((buf.readByte() << 8) & 0xFF) | ((buf.readByte()) & 0xFF);
                    ByteBuf msgBuf = buf.readBytes(msgLength);
                    //数据库地址长度
                    int dataAddLength = msgBuf.readByte() & 0xFF;
                    //数据库地址
                    ByteBuf dataAdd = msgBuf.readBytes(dataAddLength);

                    //下面的就是详情，如果消息确认状态的取值为 0～3、6 或者 9，则在确认消息中没有附加信息要被发送
                    ByteBuf msgDetail = msgBuf.readBytes(msgBuf.readableBytes());

                    log.info("({})本条消息为需要确认的主动消息,原始数据:{} 消息长度为:{} 数据库地址长度为:{} 数据库地址为:{} 消息详情为:{}", ip + ":" + port,
                        MsgUtil.bytesToHex(bytes), msgLength,
                        dataAddLength, MsgUtil.bytesToHex(dataAdd), MsgUtil.bytesToHex(msgDetail));

                    msgBuf.release();
                    dataAdd.release();
                    msgDetail.release();
                    break;
                }
                default:
                    log.info("({})未对接此消息类型{} {}", ip + ":" + port, msgType, MsgUtil.bytesToHex(bytes));
                    return;
            }

            if (1 == 1) {
                return;
            }

            //消息长度
            int msgLength = (buf.readByte() << 8 & 0xFF) | (buf.readByte() & 0xFF);
            if (msgLength > buf.readableBytes()) {
                log.info("接收到不完整消息，消息长度:{}, 剩余可读长度:{}", msgLength, buf.readableBytes());
                buf.resetReaderIndex();
                return;
            }

            //消息主体(一个主体可以携带多条信息)
            ByteBuf msg = buf.readBytes(msgLength);
            try {
//                msgHandle(msg);
            } finally {
                msg.release();
            }
        }
    }

    public void msgHandle(ByteBuf msg) {
        while (msg.readableBytes() > 0) {
            //数据库地址长度
            int dataAddLength = msg.readByte() & 0xFF;

            ByteBuf dataAdd = msg.readBytes(dataAddLength);
            try {
                //解析数据库地址，最高八位
                byte byte1 = dataAddLength >= 1 ? dataAdd.readByte() : 0x00;
                byte byte2 = dataAddLength >= 2 ? dataAdd.readByte() : 0x00;
                byte byte3 = dataAddLength >= 3 ? dataAdd.readByte() : 0x00;
                byte byte4 = dataAddLength >= 4 ? dataAdd.readByte() : 0x00;
                byte byte5 = dataAddLength >= 5 ? dataAdd.readByte() : 0x00;
                byte byte6 = dataAddLength >= 6 ? dataAdd.readByte() : 0x00;
                byte byte7 = dataAddLength >= 7 ? dataAdd.readByte() : 0x00;
                byte byte8 = dataAddLength >= 8 ? dataAdd.readByte() : 0x00;

                if (dataAddLength == 1 && byte1 == 0x00) {
                    log.info("数据库地址(通讯服务数据:COM_SV):{}", MsgUtil.bytesToHex(dataAdd));
                } else if (dataAddLength == 1 && byte1 == 0x01) {
                    log.info("数据库地址(计数器数据:C_DAT):{}", MsgUtil.bytesToHex(dataAdd));
                } else if (dataAddLength == 1 && byte1 >= 0x21 && byte1 <= 0x24) {
                    log.info("数据库地址(加油点标识:FP_ID):{}", MsgUtil.bytesToHex(dataAdd));
                    fpIdDaterIdDataHandle(msg);
                } else if (dataAddLength == 1 && byte1 >= (byte) 0x41 && byte1 <= (byte) 0x48) {
                    log.info("数据库地址(油品标识:PR_ID):{}", MsgUtil.bytesToHex(dataAdd));
                } else if (dataAddLength == 1 && byte1 == 0x61) {
                    log.info("数据库地址(油品数据:PR_DAT):{}", MsgUtil.bytesToHex(dataAdd));
                } else if (dataAddLength == 1 && byte1 >= (byte) 0x81 && byte1 <= (byte) 0x90) {
                    log.info("数据库地址(计量表标识:M_ID):{}", MsgUtil.bytesToHex(dataAdd));
                } else if (dataAddLength == 1 && byte1 == (byte) 0xA1) {
                    log.info("数据库地址(数据和软件下载:SW_DAT):{}", MsgUtil.bytesToHex(dataAdd));
                } else if (dataAddLength == 2 && (byte1 >= (byte) 0x21 && byte1 <= (byte) 0x24) && (byte2 >= (byte) 0x11 && byte2 <= (byte) 0x18)) {
                    log.info("数据库地址为 逻辑油枪数据库 DB_Ad = FP_ID （21H～24H） + LN_ID （11H～18H） :{}", MsgUtil.bytesToHex(dataAdd));
                } else if (dataAddLength == 3 && (byte1 >= (byte) 0x21 && byte1 <= (byte) 0x24) && byte2 == (byte) 0x41 && (byte3 >= (byte) 0x01
                    && (byte3 & 0xFF) <= 0xFF)) {
                    log.info("数据库地址为 错误码数据库 DB_Ad = FP_ID（21H～24H） + ER_DAT（41H） + ER_ID（01H～40H）:{}", MsgUtil.bytesToHex(dataAdd));
                    log.info("错误码:{}", ErrorCode.fromByte(byte3));
                    fpIderDaterIdDataHandle(msg);
                } else if (dataAddLength == 4 && (byte1 >= (byte) 0x21 && byte1 <= (byte) 0x24) && byte2 == (byte) 0x21 && (
                    ByteConvertor.bcdToDecimal(new byte[]{byte3, byte4}) >= 1 && ByteConvertor.bcdToDecimal(new byte[]{byte3, byte4}) <= 9999)) {
                    log.info("数据库地址为 加油交易数据库详细描述 DB_Ad = FP_ID （21H～24H） + TR_DAT （21H） + TR_Seq_Nb （0001～9999）:{}",
                        MsgUtil.bytesToHex(dataAdd));
                } else if (dataAddLength == 6 && byte1 == (byte) 0x61 && (ByteConvertor.bcdToDecimal(new byte[]{byte2, byte3, byte4, byte5}) >= 1
                    && ByteConvertor.bcdToDecimal(new byte[]{byte2, byte3, byte4, byte5}) <= 99999999) && (byte6 >= (byte) 0x11
                    && byte6 <= (byte) 0x18)) {
                    log.info("数据库地址为 加油模式下的油品数据库详细描述 DB_Ad= PR_DAT（61H）+Prod_Nb（00000001～99999999）+FM_ID（11H～18H）:{}",
                        MsgUtil.bytesToHex(dataAdd));
                } else {
                    log.error("未知的数据库地址,丢弃本条消息之后的数据:{} - {}", MsgUtil.bytesToHex(dataAdd), MsgUtil.bytesToHex(msg));
                    return;
                }
            } finally {
                dataAdd.release();
            }
        }
    }

    /**
     * 加油点数据库详细描述
     * DB_Ad = FP_ID （21H～24H）
     *
     * @param buf 缓冲区
     */
    public void fpIdDaterIdDataHandle(ByteBuf buf) {
        byte dataId = buf.readByte();
        int dataLength = 0;
        byte dataLength1 = buf.readByte();
        dataLength = dataLength1 & 0xFF;
        if (dataLength1 > (byte) 0xFE) {
            byte dataLength2 = buf.readByte();
            dataLength = ((dataLength1 << 8) & 0xFF) | (dataLength2 & 0xFF);
        }
        ByteBuf dataE = buf.readBytes(dataLength);

        if (dataId == (byte) 0x14) {
            byte[] bytes = new byte[dataE.readableBytes()];
            dataE.readBytes(bytes);
            log.info("接收到FP状态消息: {}", FpStatusEnums.getByCode(ByteConvertor.binToDecimal(bytes)));
        } else if (dataId == (byte) 0x64) {
            byte[] bytes = new byte[dataE.readableBytes()];
            dataE.readBytes(bytes);
            log.info("接收到主动FP状态消息: {}", MsgUtil.bytesToHex(bytes));
        }
        dataE.release();
    }

    /**
     * 错误码数据库处理
     * DB_Ad = FP_ID（21H～24H） + ER_DAT（41H） + ER_ID（01H～40H）
     *
     * @param buf 缓冲区
     */
    public void fpIderDaterIdDataHandle(ByteBuf buf) {
        byte dataId = buf.readByte();
        int dataLength = 0;
        byte dataLength1 = buf.readByte();
        dataLength = dataLength1 & 0xFF;
        if (dataLength1 > (byte) 0xFE) {
            byte dataLength2 = buf.readByte();
            dataLength = ((dataLength1 << 8) & 0xFF) | (dataLength2 & 0xFF);
        }
        ByteBuf dataE = buf.readBytes(dataLength);

        if (dataId == (byte) 0x01) {
            //这里只有一个字节
            ErrorCode errorCode = ErrorCode.fromByte(dataE.readByte());
            log.info("接收到错误码消息:{}", errorCode);
        } else if (dataId == (byte) 0x02) {
            byte[] bytes = new byte[dataLength];
            dataE.readBytes(bytes);
            log.info("接收到错误描述消息:{}", ByteConvertor.ascToString(bytes));
        } else if (dataId == (byte) 0x05) {
            byte[] bytes = new byte[dataLength];
            dataE.readBytes(bytes);
            log.info("接收到指定当最后一次错误（由 ER_ID 选定）发生FP 所处的状态消息:{}", FpStatusEnums.getByCode(ByteConvertor.binToDecimal(bytes)));
        } else if (dataId == (byte) 0x64) {
            byte[] bytes = new byte[dataLength];
            dataE.readBytes(bytes);
            int fpErrorType = ByteConvertor.binToDecimal(new byte[]{bytes[1]});
            int fpErrorState = ByteConvertor.binToDecimal(new byte[]{bytes[1]});
            log.info("接收到当一个错误发生，必须发送一个主动的FP_Error_Type_Mes 的消息:{} - {}", fpErrorType, fpErrorState);
        } else {
            log.info("未知的错误码数据库ID:{}", dataId);
        }
        dataE.release();
    }
}



