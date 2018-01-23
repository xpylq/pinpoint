/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.tcp;

import com.codahale.metrics.MetricRegistry;
import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperClusterService;
import com.navercorp.pinpoint.collector.config.AgentBaseDataReceiverConfiguration;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.DispatchWorker;
import com.navercorp.pinpoint.collector.receiver.DispatchWorkerOption;
import com.navercorp.pinpoint.collector.rpc.handler.AgentLifeCycleHandler;
import com.navercorp.pinpoint.collector.service.AgentEventService;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.util.MapUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class AgentBaseDataReceiver {

    private final Logger logger = LoggerFactory.getLogger(AgentBaseDataReceiver.class);

    private PinpointServerAcceptor serverAcceptor;

    private final AgentBaseDataReceiverConfiguration configuration;
    private final List<String> l4IpList;

    private final ZookeeperClusterService clusterService;

    @Autowired
    private MetricRegistry metricRegistry;

    private DispatchWorker worker;

    private final SendPacketHandler sendPacketHandler;
    private final RequestPacketHandler requestPacketHandler;


    @Resource(name = "agentEventService")
    private AgentEventService agentEventService;

    @Resource(name = "agentLifeCycleHandler")
    private AgentLifeCycleHandler agentLifeCycleHandler;

    //这个暂时没有加任何状态变更的handler
    @Resource(name = "channelStateChangeEventHandlers")
    private List<ServerStateChangeEventHandler> channelStateChangeEventHandlers = Collections.emptyList();

    public AgentBaseDataReceiver(AgentBaseDataReceiverConfiguration configuration, List<String> l4IpList, DispatchHandler dispatchHandler) {
        this(configuration, l4IpList, dispatchHandler, null);
    }

    public AgentBaseDataReceiver(AgentBaseDataReceiverConfiguration configuration, List<String> l4IpList, DispatchHandler dispatchHandler, ZookeeperClusterService service) {
        Assert.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
        this.configuration = Assert.requireNonNull(configuration, "config must not be null");

        this.l4IpList = l4IpList;
        //创建分发worker的配置
        DispatchWorkerOption dispatchWorkerOption = new DispatchWorkerOption("Pinpoint-AgentBaseDataReceiver-Worker", configuration.getWorkerThreadSize(), configuration.getWorkerQueueSize(), 1, configuration.isWorkerMonitorEnable());
        //创建对应的分发worker线程池
        this.worker = new DispatchWorker(dispatchWorkerOption);

        this.sendPacketHandler = new SendPacketHandler(dispatchHandler);
        this.requestPacketHandler = new RequestPacketHandler(dispatchHandler);

        this.clusterService = service;
    }

    @PostConstruct
    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("start() started");
        }

        //创建netty-server服务
        PinpointServerAcceptor acceptor = new PinpointServerAcceptor();
        //准备阶段,增加状态处理handler
        prepare(acceptor);

        worker.setMetricRegistry(metricRegistry);
        worker.start();

        // 这个ServerMessageListener，才是处理真正不同类型命令的函数入口，它也是做一个转发作用，真正的处理逻辑在worker线程里
        // take care when attaching message handlers as events are generated from the IO thread.
        // pass them to a separate queue and handle them in a different thread.
        acceptor.setMessageListener(new ServerMessageListener() {

            @Override
            public HandshakeResponseCode handleHandshake(Map properties) {
                if (properties == null) {
                    return HandshakeResponseType.ProtocolError.PROTOCOL_ERROR;
                }

                boolean hasRequiredKeys = HandshakePropertyType.hasRequiredKeys(properties);
                if (!hasRequiredKeys) {
                    return HandshakeResponseType.PropertyError.PROPERTY_ERROR;
                }

                boolean supportServer = MapUtils.getBoolean(properties, HandshakePropertyType.SUPPORT_SERVER.getName(), true);
                if (supportServer) {
                    return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
                } else {
                    return HandshakeResponseType.Success.SIMPLEX_COMMUNICATION;
                }
            }

            @Override
            public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
                receive(sendPacket, pinpointSocket);
            }

            @Override
            public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
                requestResponse(requestPacket, pinpointSocket);
            }

            @Override
            public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
                recordPing(pingPacket, pinpointServer);
            }
        });
        //启动netty-server
        acceptor.bind(configuration.getBindIp(), configuration.getBindPort());

        this.serverAcceptor = acceptor;

        if (logger.isInfoEnabled()) {
            logger.info("start() completed");
        }
    }

    private void prepare(PinpointServerAcceptor acceptor) {
        //是否开启集群
        if (clusterService != null && clusterService.isEnable()) {
            //clusterService.getChannelStateChangeEventHandler()=ZookeeperProfilerClusterManager
            acceptor.addStateChangeEventHandler(clusterService.getChannelStateChangeEventHandler());
        }

        for (ServerStateChangeEventHandler channelStateChangeEventHandler : this.channelStateChangeEventHandlers) {
            acceptor.addStateChangeEventHandler(channelStateChangeEventHandler);
        }

        setL4TcpChannel(acceptor, l4IpList);
    }

    private void setL4TcpChannel(PinpointServerAcceptor acceptor, List<String> l4ipList) {
        if (l4ipList == null) {
            return;
        }
        try {
            List<InetAddress> inetAddressList = new ArrayList<>();
            for (int i = 0; i < l4ipList.size(); i++) {
                String l4Ip = l4ipList.get(i);
                if (StringUtils.isBlank(l4Ip)) {
                    continue;
                }

                InetAddress address = InetAddress.getByName(l4Ip);
                if (address != null) {
                    inetAddressList.add(address);
                }
            }

            InetAddress[] inetAddressArray = new InetAddress[inetAddressList.size()];
            acceptor.setIgnoreAddressList(inetAddressList.toArray(inetAddressArray));
        } catch (UnknownHostException e) {
            logger.warn("l4ipList error {}", l4ipList, e);
        }
    }

    private void receive(SendPacket sendPacket, PinpointSocket pinpointSocket) {
        worker.execute(new Runnable() {
            @Override
            public void run() {
                sendPacketHandler.handle(sendPacket, pinpointSocket);
            }
        });
    }

    private void requestResponse(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
        worker.execute(new Runnable() {
            @Override
            public void run() {
                requestPacketHandler.handle(requestPacket, pinpointSocket);
            }
        });
    }

    private void recordPing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
        final int eventCounter = pingPacket.getPingId();
        long pingTimestamp = System.currentTimeMillis();
        try {
            if (!(eventCounter < 0)) {
                agentLifeCycleHandler.handleLifeCycleEvent(pinpointServer, pingTimestamp, AgentLifeCycleState.RUNNING, eventCounter);
            }
            agentEventService.handleEvent(pinpointServer, pingTimestamp, AgentEventType.AGENT_PING);
        } catch (Exception e) {
            logger.warn("Error handling ping event", e);
        }
    }

    @PreDestroy
    public void stop() {
        if (logger.isInfoEnabled()) {
            logger.info("stop() started");
        }

        if (serverAcceptor != null) {
            serverAcceptor.close();
        }

        if (worker != null) {
            worker.shutdown();
        }

        if (logger.isInfoEnabled()) {
            logger.info("stop() completed");
        }
    }


}
