package com.zmjy.command;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "tcp-client")
public class TcpClient {

    public static void main(String[] args) throws InterruptedException {
        TcpClient tcpClient = new TcpClient();
        tcpClient.tpcClient("192.168.4.230", 8767);
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(new byte[]{01, 01});
        tcpClient.send(buffer, "hello world");
    }

    //用于构建token,线程安全的int
    private static final AtomicInteger token = new AtomicInteger(1);
    private final long intervalMillis;
    private volatile boolean running = true;

    public TcpClient() {
        this.intervalMillis = 1500;
        startWorker();
    }

    //发送队列
    private final BlockingQueue<MessageWrapper> queue = new LinkedBlockingQueue<>();


    @Getter
    private Channel channel;

    public void tpcClient(String ip, Integer port) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
            }
        });
        ChannelFuture future = bootstrap.connect(ip, port);
        boolean success = future.await(3, TimeUnit.SECONDS);
        if (!success) {
            log.info("服务连接{}超时", ip + ":" + port);
            throw new RuntimeException("tcp-client服务连接超时");
        } else {
            if (future.isSuccess()) {
                log.info("服务启动成功: {}", ip + ":" + port);
                channel = future.channel();
            } else {
                log.info("服务启动失败: {}", ip + ":" + port);
                throw new RuntimeException("tcp-client服务启动失败");
            }
        }
    }


    /**
     * 发送入口：所有 send() 都只是放入队列，不直接发
     */
    public void send(ByteBuf buf, String msg) {
        if (!running) {
            throw new IllegalStateException("发送器已停止");
        }
        queue.offer(new MessageWrapper(buf.retain(), msg));
    }

    /**
     * 启动后台线程，循环发送
     */
    private void startWorker() {
        Thread senderThread = new Thread(() -> {
            while (running) {
                try {
                    // 阻塞获取一条消息（1 秒超时防止死锁）
                    MessageWrapper message = queue.poll(1, TimeUnit.SECONDS);
                    if (message == null) {
                        continue;
                    }

                    byte[] data = new byte[message.buf.readableBytes()];
                    message.buf.getBytes(0, data);

                    ChannelFuture future = channel.writeAndFlush(message.buf).sync();
                    if (future.isSuccess()) {
                        log.info("发送成功 [{}]: {}", message.msg, MsgUtil.bytesToHex(data));
                    } else {
                        log.error("发送失败 [{}]", message.msg, future.cause());
                    }

                    // 每次发送后强制间隔
                    Thread.sleep(intervalMillis);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("发送线程被中断");
                } catch (Exception e) {
                    log.error("发送异常", e);
                }
            }
        }, "throttled-sender-thread");

        senderThread.setDaemon(true);
        senderThread.start();
    }

    /**
     * 停止发送线程
     */
    public void stop() {
        running = false;
        log.info("发送器已停止");
    }

    private static class MessageWrapper {

        final ByteBuf buf;
        final String msg;

        MessageWrapper(ByteBuf buf, String msg) {
            this.buf = buf;
            this.msg = msg;
        }
    }
}
