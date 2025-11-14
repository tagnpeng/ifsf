package com.zmjy.command;

import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.zmjy.command.dto.Heartbeat;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "upd")
public class UpdServer {

    public static void main(String[] args) throws InterruptedException {
        UpdServer updServer = new UpdServer();
        updServer.upd("0.0.0.0", 9821);
        byte[] pack = new byte[10];
        int tpcServerPort = 8083;
        String[] localIps = "192.168.254.65".split("\\.");
        for (int i = 0; i < localIps.length; i++) {
            pack[i] = (byte) Integer.parseInt(localIps[i]);
        }
        pack[4] = (byte) ((tpcServerPort >> 8) & 0xFF);
        pack[5] = (byte) (tpcServerPort & 0xFF);
        pack[6] = (byte) (1 & 0xFF);
        pack[7] = (byte) (2 & 0xFF);
        pack[8] = 0x01;
        pack[9] = 0x00;
        ByteBuf buf = Unpooled.wrappedBuffer(pack);
        InetSocketAddress remoteAddress = new InetSocketAddress("255.255.255.255", 8081);
        DatagramPacket packet = new DatagramPacket(buf.retainedDuplicate(), remoteAddress);
        updServer.send(packet, "心跳协议");
    }

    @Getter
    private Channel channel;

    public void upd(String localIp, Integer port) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group).option(ChannelOption.SO_BROADCAST, true)
            .channelFactory((ChannelFactory<NioDatagramChannel>) () -> new NioDatagramChannel(InternetProtocolFamily.IPv4))
            .option(ChannelOption.SO_BROADCAST, true).handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    //消息解析
                    pipeline.addLast(SimpleChannelInboundHandler());
                }
            });

        ChannelFuture future = bootstrap.bind(localIp, port);
        boolean success = future.await(3, TimeUnit.SECONDS);
        if (!success) {
            log.info("服务创建{}超时", localIp + ":" + port);
            throw new RuntimeException("服务连接超时");
        } else {
            if (future.isSuccess()) {
                log.info("服务启动成功");
                channel = future.channel();
                channel.config().setOption(ChannelOption.SO_BROADCAST, true);
            } else {
                log.info("服务启动失败:{}", localIp + ":" + port);
                throw new RuntimeException("服务启动失败");
            }
        }
    }

    public SimpleChannelInboundHandler<DatagramPacket> SimpleChannelInboundHandler() {
        return new SimpleChannelInboundHandler<DatagramPacket>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
                ByteBuf buf = msg.content();
                int length = buf.readableBytes();
                byte[] bytes = new byte[length];
                buf.readBytes(bytes);
                String ip = CharSequenceUtil.format("{}.{}.{}.{}", bytes[0] & 0xff, bytes[1] & 0xff, bytes[2] & 0xff, bytes[3] & 0xff);
                // TODO: 2025/11/11 改成动态
                if (ObjectUtil.equal(ip, Main.getLocalIp())) {
                    log.info("本地广播的心跳，忽略");
                    return;
                }
                byte[] portsByte = new byte[]{0x00, 0x00, bytes[4], bytes[5]};
                int port = ByteBuffer.wrap(portsByte).getInt();
                // 获取节点
                Integer node = bytes[7] & 0xff;
                Heartbeat heartbeat = new Heartbeat(ip, port, node);
                log.info("接收到协议 原始数据:{} 解析后数据:{}", MsgUtil.bytesToHex(bytes), JSONUtil.toJsonStr(heartbeat));
                Cache.getInstance().setReceiveHeartbeat(Boolean.TRUE);
                Cache.getInstance().setHeartbeat(heartbeat);
            }
        };
    }

    public void send(DatagramPacket packet, String msg) {
        try {
            channel.config().setOption(ChannelOption.SO_BROADCAST, true);
            ChannelFuture future = channel.writeAndFlush(packet).sync();
            log.info("发送数据成功 {}:{}", msg, MsgUtil.bytesToHex(packet));
            if (!future.isSuccess()) {
                log.error("发送数据异常", future.cause());
            }
        } catch (InterruptedException e) {
            log.error("发送数据异常", e);
            throw new RuntimeException(e);
        }
    }
}
