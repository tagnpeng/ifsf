package com.zmjy.command;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.zmjy.command.build.CDatBuild;
import com.zmjy.command.build.ComSvBuild;
import com.zmjy.command.build.FpIdBuild;
import com.zmjy.command.build.FpIdLnIdBuild;
import com.zmjy.command.build.PrDatProdNbFmIdBuild;
import com.zmjy.command.build.PrIdBuild;
import com.zmjy.command.dto.Heartbeat;
import com.zmjy.command.dto.Mapping;
import com.zmjy.command.dto.enums.MsgType;
import com.zmjy.command.util.ByteConvertor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    //远程地址
    private static final String remoteIp = "192.168.4.156";

    //心跳广播端口
    private static final Integer heartbeatBroadcastPort = 3486;

    //本机ip
    @Getter
    private static final String localIp = "192.168.4.64";
    //updIp
    private static final String updIp = "0.0.0.0";
    //upd端口
    private static final Integer updPort = 3486;
    //tpc-server端口
    private static final Integer tpcServerPort = 8888;

    public static void main(String[] args) throws Exception {
        //初始化信息
        List<Mapping> mappings = initMappings();

        //启动upd服务
        UpdServer updServer = new UpdServer();
        updServer.upd(updIp, updPort);
        sendHeartbeat(updServer, mappings);

        //启动tpc-server
        TcpServer tcpServer = new TcpServer();
        tcpServer.tpcServer(localIp, tpcServerPort);

        //等待油机先接入
        Thread.sleep(10 * 1000);

        TcpClient tcpClient = new TcpClient();

        while (tcpClient.getChannel() == null) {
            log.info("等待接收心跳信息...");
            Thread.sleep(3000);
            //接收到心跳信息后连接ifsf-tpc服务
            if (Cache.getInstance().getReceiveHeartbeat()) {
                Heartbeat heartbeat = Cache.getInstance().getHeartbeat();
                tcpClient.tpcClient(heartbeat.getIp(), heartbeat.getPort());
            }
        }

        //发送初始化信息
//        cDatInit(tcpClient, mappings);
//        prIdInit(tcpClient, mappings);
//        fpIdInit(tcpClient, mappings);
//        prDatProdNbFmIdInit(tcpClient, mappings);
//        fpIdlnIdInit(tcpClient, mappings);

        //等待初始化完成
//        Thread.sleep(15 * 1000);
        Thread.sleep(3000);

        //自定义命令
        Thread customize = new Thread(() -> {
            while (true) {
                log.info("请输入命令：\n" + "1: 发送自定义16进制命令(01 01 01)\n" + "2: 选择指定命令\n" + "0: 退出程序");
                Scanner scanner = new Scanner(System.in);
                int num = scanner.nextInt();
                switch (num) {
                    case 1:
                        log.info("请输入16进制命令：");
                        scanner = new Scanner(System.in);
                        String cmd = scanner.nextLine();
                        ByteBuf buf = Unpooled.wrappedBuffer(ByteConvertor.hexStringToByteArray(cmd));
                        tcpClient.send(buf, "自定义命令");
                        break;
                    case 2:
                        log.info("请输入命令编号：\n" + "1: 获取所有FP状态\n" + "2: 获取所有逻辑油枪状态\n" + "3: 发送打开fp命令\n" + "4: cDat初始化\n"
                            + "5: prId初始化\n" + "6: fpId初始化\n" + "7: prDatProdNbFmId初始化\n" + "8: fpIdLnId初始化\n" + "9: 关闭所有fp\n"
                            + "10: fp允许预授权状态\n" + "11: 一键初始化\n" + "0:返回主菜单");
                        scanner = new Scanner(System.in);
                        int assignCmd = scanner.nextInt();
                        if (assignCmd == 1) {
                            for (Mapping mapping : mappings.stream().collect(Collectors.toMap(
                                it -> StrUtil.concat(true, it.getSubNode().toString(), it.getNode().toString(), it.getFpId().toString()),
                                Function.identity(), (k1, k2) -> k1)).values()) {
                                tcpClient.send(FpIdBuild.fpState(mapping.getSubNode(), mapping.getNode(), mapping.getFpId()),
                                    "获取FP状态 node:" + mapping.getSubNode() + " sNode:" + mapping.getNode() + " fpId:" + mapping.getFpId());
                            }
                        } else if (assignCmd == 2) {
                            for (Mapping mapping : mappings.stream().collect(Collectors.toMap(
                                it -> StrUtil.concat(true, it.getSubNode().toString(), it.getNode().toString(), it.getFpId().toString()),
                                Function.identity(), (k1, k2) -> k1)).values()) {
                                tcpClient.send(FpIdBuild.logNozState(mapping.getSubNode(), mapping.getNode(), mapping.getFpId()),
                                    "获取所有逻辑油枪状态 node:" + mapping.getSubNode() + " sNode:" + mapping.getNode() + " fpId:"
                                        + mapping.getFpId());
                            }
                        } else if (assignCmd == 3) {
                            tcpClient.send(Unpooled.wrappedBuffer(ByteConvertor.hexStringToByteArray("01 01 02 01 00 41 00 04 01 21 3C 00")),
                                "打开FP协议");
                        } else if (assignCmd == 4) {
                            cDatInit(tcpClient, mappings);
                        } else if (assignCmd == 5) {
                            prIdInit(tcpClient, mappings);
                        } else if (assignCmd == 6) {
                            fpIdInit(tcpClient, mappings);
                        } else if (assignCmd == 7) {
                            prDatProdNbFmIdInit(tcpClient, mappings);
                        } else if (assignCmd == 8) {
                            fpIdlnIdInit(tcpClient, mappings);
                        } else if (assignCmd == 9) {
                            Map<String, List<Mapping>> oilEngineGroup = mappings.stream()
                                .collect(Collectors.groupingBy(it -> StrUtil.concat(true, it.getSubNode().toString(), it.getNode().toString())));
                            oilEngineGroup.forEach((k, v) -> {
                                Map<Integer, Mapping> fpGroup = v.stream()
                                    .collect(Collectors.toMap(Mapping::getFpId, Function.identity(), (v1, v2) -> v1));
                                for (Mapping fp : fpGroup.values()) {
                                    tcpClient.send(FpIdBuild.closeFp(fp.getSubNode(), fp.getNode(), fp.getFpId()), "关闭一个 FP");
                                }
                            });
                        } else if (assignCmd == 10) {
                            Map<String, List<Mapping>> oilEngineGroup = mappings.stream()
                                .collect(Collectors.groupingBy(it -> StrUtil.concat(true, it.getSubNode().toString(), it.getNode().toString())));
                            oilEngineGroup.forEach((k, v) -> {
                                tcpClient.send(CDatBuild.authStateMode(v.get(0).getSubNode(), v.get(0).getNode(), 0), "FP允许预授权状态");
                            });
                        } else if (assignCmd == 11) {
                            comSvInit(tcpClient, mappings);
                            cDatInit(tcpClient, mappings);
                            prIdInit(tcpClient, mappings);
                            fpIdInit(tcpClient, mappings);
                            prDatProdNbFmIdInit(tcpClient, mappings);
                            fpIdlnIdInit(tcpClient, mappings);
                        }
                        break;
                    case 0:
                        log.info("退出程序");
                        System.exit(0);
                        break;
                    default:
                        log.info("请输入正确的命令编号");
                        break;
                }
            }
        });

        customize.start();

        customize.join();
    }


    public static List<Mapping> initMappings() {
        return ListUtil.toList(new Mapping(remoteIp, 1, 1, 1, 1, 1, 1, 1, 95, new BigDecimal("8.68")));
    }

    public static void comSvInit(TcpClient tcpClient, List<Mapping> mappings) {
        Map<String, List<Mapping>> oilEngineGroup = mappings.stream()
            .collect(Collectors.groupingBy(it -> StrUtil.concat(true, it.getIp(), it.getSubNode().toString(), it.getNode().toString())));

        oilEngineGroup.forEach((oilEngine, oilEngineMappings) -> {
            Integer subNode = oilEngineMappings.get(0).getSubNode();
            Integer node = oilEngineMappings.get(0).getNode();
            //主动推送地址初始化
            tcpClient.send(ComSvBuild.add(subNode, node), "主动消息地址初始化");
        });
    }

    /**
     * 计算器数据库 C_DAT 初始化
     */
    public static void cDatInit(TcpClient tcpClient, List<Mapping> mappings) {
        //油机分组 ip+节点+子节点
        Map<String, List<Mapping>> oilEngineGroup = mappings.stream()
            .collect(Collectors.groupingBy(it -> StrUtil.concat(true, it.getIp(), it.getSubNode().toString(), it.getNode().toString())));

        oilEngineGroup.forEach((oilEngine, oilEngineMappings) -> {
            Integer node = oilEngineMappings.get(0).getSubNode();
            Integer sNode = oilEngineMappings.get(0).getNode();
            ByteBuf buf = null;
            //油品分组
            Map<Integer, List<Mapping>> prodNbGroup = oilEngineMappings.stream().collect(Collectors.groupingBy(Mapping::getProdNb));
            //加油模式分组
            Map<String, List<Mapping>> fmGroup = oilEngineMappings.stream().collect(Collectors.groupingBy(it -> it.getFmId().toString()));
            //加油点分组
            Map<String, List<Mapping>> fpGroup = oilEngineMappings.stream().collect(Collectors.groupingBy(it -> it.getFpId().toString()));

            // --------- 计算器数据库 C_DAT 初始化 start ---------
            //设定油品种类
            buf = CDatBuild.nbProducts(node, sNode, prodNbGroup.size());
            tcpClient.send(buf, "设定油品种类协议");

            //加油模式的种类
            buf = CDatBuild.nbFuellingModes(node, sNode, fmGroup.size());
            tcpClient.send(buf, "加油模式的种类协议");

            //加油机计算器控制的加油点的数量
            buf = CDatBuild.nbFp(node, sNode, fpGroup.size());
            tcpClient.send(buf, "加油机计算器控制的加油点的数量协议");

            //加油机是否可在自主加油模式中工作
            buf = CDatBuild.standAloneAuth(node, sNode, 0);
            tcpClient.send(buf, "加油机是否可在自主加油模式中工作协议");

            //FP允许预授权状态
            tcpClient.send(CDatBuild.authStateMode(node, sNode, 0), "FP允许预授权状态");

            // --------- 计算器数据库 C_DAT 初始化 end ---------
        });
    }

    /**
     * 计算器数据库 PR_ID 初始化
     */
    public static void prIdInit(TcpClient tcpClient, List<Mapping> mappings) {
        Map<String, List<Mapping>> oilEngineGroup = mappings.stream()
            .collect(Collectors.groupingBy(it -> StrUtil.concat(true, it.getIp(), it.getSubNode().toString(), it.getNode().toString())));

        oilEngineGroup.forEach((oilEngine, oilEngineMappings) -> {
            Integer node = oilEngineMappings.get(0).getSubNode();
            Integer sNode = oilEngineMappings.get(0).getNode();
            ByteBuf buf = null;
            //油品分组
            Map<Integer, Mapping> prodNbGroup = oilEngineMappings.stream()
                .collect(Collectors.toMap(Mapping::getProdNb, Function.identity(), (k1, k2) -> k1));

            // --------- 油品数据库 PR_ID 初始化 start ---------
            for (Integer prodNb : prodNbGroup.keySet()) {
                Integer prId = prodNbGroup.get(prodNb).getPrId();
                //设定油品编号
                buf = PrIdBuild.prodNb(node, sNode, prId, prodNb, MsgType.WRITE);
                tcpClient.send(buf, "设定油品编号协议");

                //设定油品描述
                buf = PrIdBuild.prodDescription(node, sNode, prId, prodNb.toString());
                tcpClient.send(buf, "设定油品描述协议");

            }
            // --------- 油品数据库 PR_ID 初始化 end ---------
        });
    }

    /**
     * 加油点数据库 FP_ID 初始化
     */
    public static void fpIdInit(TcpClient tcpClient, List<Mapping> mappings) {
        Map<String, List<Mapping>> oilEngineGroup = mappings.stream()
            .collect(Collectors.groupingBy(it -> StrUtil.concat(true, it.getIp(), it.getSubNode().toString(), it.getNode().toString())));

        oilEngineGroup.forEach((oilEngine, oilEngineMappings) -> {
            Integer node = oilEngineMappings.get(0).getSubNode();
            Integer sNode = oilEngineMappings.get(0).getNode();
            //加油点分组
            Map<Integer, List<Mapping>> fpIdGroup = oilEngineMappings.stream().collect(Collectors.groupingBy(Mapping::getFpId));

            // --------- 加油点数据库 DB_Ad = FP_ID （21H～24H） 初始化 start ---------
            fpIdGroup.forEach((fpId, fpIdMappings) -> {
                //FP 上逻辑油枪的数量。数量被接受的范围是 1 到 8
                ByteBuf buf = FpIdBuild.nbLogicalNozzle(node, sNode, fpId, (int) fpIdMappings.stream().map(Mapping::getLnId).distinct().count());
                tcpClient.send(buf, "设置FP上逻辑油枪的数量协议");

                //允许 CD 授权一个或多个逻辑油枪
                Map<Integer, Integer> lnIdStatus = fpIdMappings.stream().collect(Collectors.toMap(Mapping::getLnId, it -> 1, (k1, k2) -> k1));
                buf = FpIdBuild.logNozMask(node, sNode, fpId, lnIdStatus);
                tcpClient.send(buf, "允许 CD 授权一个或多个逻辑油枪协议");

                //加油点的加油模式
                Integer fmId = fpIdMappings.get(0).getFmId();
                buf = FpIdBuild.fuellingMode(node, sNode, fpId, fmId);
                tcpClient.send(buf, "加油点的加油模式协议");

                buf = FpIdBuild.assignContrId(fpId, node, sNode);
                tcpClient.send(buf, "用来指定 FP 是否被分配给控制器，分配给哪个控制器");
            });

            // --------- 加油点数据库 DB_Ad = FP_ID （21H～24H） 初始化 end ---------
        });
    }

    /**
     * 加油点数据库详细描述 PR_DAT+Prod_Nb+FM_ID 初始化
     */
    public static void prDatProdNbFmIdInit(TcpClient tcpClient, List<Mapping> mappings) {
        Map<String, List<Mapping>> oilEngineGroup = mappings.stream()
            .collect(Collectors.groupingBy(it -> StrUtil.concat(true, it.getIp(), it.getSubNode().toString(), it.getNode().toString())));

        oilEngineGroup.forEach((oilEngine, oilEngineMappings) -> {
            Integer node = oilEngineMappings.get(0).getSubNode();
            Integer sNode = oilEngineMappings.get(0).getNode();
            //加油点加油模式分组
            Map<String, List<Mapping>> prodNbFmIdGroup = oilEngineMappings.stream()
                .collect(Collectors.groupingBy(it -> StrUtil.concat(true, it.getProdNb().toString(), it.getFmId().toString())));

            // --------- 加油点数据库详细描述 DB_Ad= PR_DAT（61H）+Prod_Nb（00000001～99999999）+FM_ID（11H～18H） 初始化 start ---------
            prodNbFmIdGroup.forEach((prodNbFmId, prodNbMappings) -> {
                for (Mapping mapping : prodNbMappings) {
                    //设置油品单价
                    ByteBuf buf = PrDatProdNbFmIdBuild.updatePrice(node, sNode, mapping.getProdNb(), mapping.getFmId(), mapping.getUnitPrice());
                    tcpClient.send(buf, "设置油品单价协议");

                }
            });
            // --------- 加油点数据库详细描述 DB_Ad= PR_DAT（61H）+Prod_Nb（00000001～99999999）+FM_ID（11H～18H） 初始化 end ---------
        });
    }

    /**
     * 逻辑油枪数据库详细描述 FP_ID （21H～24H） + LN_ID 初始化
     */
    public static void fpIdlnIdInit(TcpClient tcpClient, List<Mapping> mappings) {
        Map<String, List<Mapping>> oilEngineGroup = mappings.stream()
            .collect(Collectors.groupingBy(it -> StrUtil.concat(true, it.getIp(), it.getSubNode().toString(), it.getNode().toString())));

        oilEngineGroup.forEach((oilEngine, oilEngineMappings) -> {
            Integer node = oilEngineMappings.get(0).getSubNode();
            Integer sNode = oilEngineMappings.get(0).getNode();
            //油品分组
            Map<String, List<Mapping>> fpIdLnIdGroup = oilEngineMappings.stream()
                .collect(Collectors.groupingBy(it -> StrUtil.concat(true, it.getFpId().toString(), it.getLnId().toString())));

            // --------- 加油点数据库详细描述 FP_ID （21H～24H） + LN_ID 初始化 start ---------
            fpIdLnIdGroup.forEach((prodNbFmId, prodNbMappings) -> {
                for (Mapping mapping : prodNbMappings) {
                    //设置逻辑油枪的油品
                    ByteBuf buf = FpIdLnIdBuild.prId(node, sNode, mapping.getFpId(), mapping.getLnId(), mapping.getPrId(), MsgType.WRITE);
                    tcpClient.send(buf, "设置逻辑油枪的油品协议");

                    //为该逻辑油枪指定对应物理油枪标识
                    buf = FpIdLnIdBuild.physicalNozId(node, sNode, mapping.getFpId(), mapping.getLnId(), mapping.getGunNo());
                    tcpClient.send(buf, "为该逻辑油枪指定对应物理油枪标识协议");

                }
            });
            // --------- 加油点数据库详细描述 DB_Ad= PR_DAT（61H）+Prod_Nb（00000001～99999999）+FM_ID（11H～18H） 初始化 end ---------
        });
    }

    /**
     * 每隔3秒发送一次心跳
     *
     * @param updServer updServer
     */
    public static void sendHeartbeat(UpdServer updServer, List<Mapping> mappings) {
        //广播tcpServer地址
        new Thread(() -> {
            //三秒发送一次
            while (true) {
                try {
                    Thread.sleep(10 * 1000);
                    mappings.stream()
                        .collect(Collectors.groupingBy(it -> StrUtil.concat(true, it.getIp(), it.getSubNode().toString(), it.getNode().toString())))
                        .forEach((oilEngine, oilEngineMappings) -> {
                            byte[] pack = new byte[10];
                            String[] localIps = localIp.split("\\.");
                            for (int i = 0; i < localIps.length; i++) {
                                pack[i] = (byte) Integer.parseInt(localIps[i]);
                            }
                            pack[4] = (byte) ((tpcServerPort >> 8) & 0xFF);
                            pack[5] = (byte) (tpcServerPort & 0xFF);
                            pack[6] = Cache.getInstance().getLocalSubNode();
                            pack[7] = Cache.getInstance().getLocalNode();
                            pack[8] = 0x01;
                            pack[9] = 0x00;
                            ByteBuf buf = Unpooled.wrappedBuffer(pack);
                            String[] split = remoteIp.split("\\.");
                            InetSocketAddress remoteAddress = new InetSocketAddress(
                                StrUtil.concat(true, split[0], ".", split[1], ".", split[2], ".", "255"), heartbeatBroadcastPort);
                            DatagramPacket packet = new DatagramPacket(buf.retainedDuplicate(), remoteAddress);
                            updServer.send(packet, "心跳协议");
                        });
                } catch (Exception e) {
                    log.error("发送心跳信息异常", e);
                }
            }
        }).start();
    }
}