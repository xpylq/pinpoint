/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.ByteSizeUnit;
import com.navercorp.pinpoint.grpc.client.ClientOption;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcTransportConfig {

    private static final String DEFAULT_IP = "127.0.0.1";
    private static final long DEFAULT_CLIENT_REQUEST_TIMEOUT = 6000;
    private static final int DEFAULT_AGENT_SENDER_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_METADATA_SENDER_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_SPAN_SENDER_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_STAT_SENDER_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_AGENT_COLLECTOR_PORT = 9991;
    private static final int DEFAULT_STAT_COLLECTOR_PORT = 9992;
    private static final int DEFAULT_SPAN_COLLECTOR_PORT = 9993;
    private static final int DEFAULT_AGENT_CHANNEL_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_METADATA_CHANNEL_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_STAT_CHANNEL_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_SPAN_CHANNEL_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_METADATA_RETRY_MAX_COUNT = 3;
    private static final int DEFAULT_METADATA_RETRY_DELAY_MILLIS = 1000;

    private String agentCollectorIp = DEFAULT_IP;
    private int agentCollectorPort = DEFAULT_AGENT_COLLECTOR_PORT;

    private String metadataCollectorIp = DEFAULT_IP;
    private int metadataCollectorPort = DEFAULT_AGENT_COLLECTOR_PORT;

    private String statCollectorIp = DEFAULT_IP;
    private int statCollectorPort = DEFAULT_STAT_COLLECTOR_PORT;

    private String spanCollectorIp = DEFAULT_IP;
    private int spanCollectorPort = DEFAULT_SPAN_COLLECTOR_PORT;

    private ClientOption agentClientOption = new ClientOption.Builder().build();
    private ClientOption metadataClientOption = new ClientOption.Builder().build();
    private ClientOption statClientOption = new ClientOption.Builder().build();
    private ClientOption spanClientOption = new ClientOption.Builder().build();

    private int agentSenderExecutorQueueSize = DEFAULT_AGENT_SENDER_EXECUTOR_QUEUE_SIZE;
    private int metadataSenderExecutorQueueSize = DEFAULT_METADATA_SENDER_EXECUTOR_QUEUE_SIZE;
    private int spanSenderExecutorQueueSize = DEFAULT_SPAN_SENDER_EXECUTOR_QUEUE_SIZE;
    private int statSenderExecutorQueueSize = DEFAULT_STAT_SENDER_EXECUTOR_QUEUE_SIZE;

    private int agentChannelExecutorQueueSize = DEFAULT_AGENT_CHANNEL_EXECUTOR_QUEUE_SIZE;
    private int metadataChannelExecutorQueueSize = DEFAULT_METADATA_CHANNEL_EXECUTOR_QUEUE_SIZE;
    private int statChannelExecutorQueueSize = DEFAULT_STAT_CHANNEL_EXECUTOR_QUEUE_SIZE;
    private int spanChannelExecutorQueueSize = DEFAULT_SPAN_CHANNEL_EXECUTOR_QUEUE_SIZE;

    private long agentRequestTimeout = DEFAULT_CLIENT_REQUEST_TIMEOUT;
    private long metadataRequestTimeout = DEFAULT_CLIENT_REQUEST_TIMEOUT;
    private long spanRequestTimeout = DEFAULT_CLIENT_REQUEST_TIMEOUT;
    private long statRequestTimeout = DEFAULT_CLIENT_REQUEST_TIMEOUT;

    private int metadataRetryMaxCount = DEFAULT_METADATA_RETRY_MAX_COUNT;
    private int metadataRetryDelayMillis = DEFAULT_METADATA_RETRY_DELAY_MILLIS;

    public void read(ProfilerConfig profilerConfig) {
        final ProfilerConfig.ValueResolver placeHolderResolver = new DefaultProfilerConfig.PlaceHolderResolver();
        // Agent
        this.agentCollectorIp = profilerConfig.readString("profiler.transport.grpc.agent.collector.ip", DEFAULT_IP, placeHolderResolver);
        this.agentCollectorPort = profilerConfig.readInt("profiler.transport.grpc.agent.collector.port", DEFAULT_AGENT_COLLECTOR_PORT);
        this.agentClientOption = readAgentClientOption(profilerConfig);
        this.agentRequestTimeout = profilerConfig.readLong("profiler.transport.grpc.agent.sender.request.timeout.millis", DEFAULT_CLIENT_REQUEST_TIMEOUT);
        this.agentSenderExecutorQueueSize = profilerConfig.readInt("profiler.transport.grpc.agent.sender.executor.queue.size", DEFAULT_AGENT_SENDER_EXECUTOR_QUEUE_SIZE);
        this.agentChannelExecutorQueueSize = profilerConfig.readInt("profiler.transport.grpc.agent.sender.channel.executor.queue.size", DEFAULT_AGENT_CHANNEL_EXECUTOR_QUEUE_SIZE);

        // Metadata
        this.metadataCollectorIp = profilerConfig.readString("profiler.transport.grpc.metadata.collector.ip", DEFAULT_IP, placeHolderResolver);
        this.metadataCollectorPort = profilerConfig.readInt("profiler.transport.grpc.metadata.collector.port", DEFAULT_AGENT_COLLECTOR_PORT);
        this.metadataClientOption = readMetadataClientOption(profilerConfig);
        this.metadataRequestTimeout = profilerConfig.readLong("profiler.transport.grpc.metadata.sender.request.timeout.millis", DEFAULT_CLIENT_REQUEST_TIMEOUT);
        this.metadataSenderExecutorQueueSize = profilerConfig.readInt("profiler.transport.grpc.metadata.sender.executor.queue.size", DEFAULT_METADATA_SENDER_EXECUTOR_QUEUE_SIZE);
        this.metadataChannelExecutorQueueSize = profilerConfig.readInt("profiler.transport.grpc.metadata.sender.channel.executor.queue.size", DEFAULT_METADATA_CHANNEL_EXECUTOR_QUEUE_SIZE);
        this.metadataRetryMaxCount = profilerConfig.readInt("profiler.transport.grpc.metadata.sender.retry.max.count", DEFAULT_METADATA_RETRY_MAX_COUNT);
        this.metadataRetryDelayMillis = profilerConfig.readInt("profiler.transport.grpc.metadata.sender.retry.delay.millis", DEFAULT_METADATA_RETRY_DELAY_MILLIS);

        // Stat
        this.statCollectorIp = profilerConfig.readString("profiler.transport.grpc.stat.collector.ip", DEFAULT_IP, placeHolderResolver);
        this.statCollectorPort = profilerConfig.readInt("profiler.transport.grpc.stat.collector.port", DEFAULT_STAT_COLLECTOR_PORT);
        this.statClientOption = readStatClientOption(profilerConfig);
        this.statRequestTimeout = profilerConfig.readLong("profiler.transport.grpc.stat.sender.request.timeout.millis", DEFAULT_CLIENT_REQUEST_TIMEOUT);
        this.statSenderExecutorQueueSize = profilerConfig.readInt("profiler.transport.grpc.stat.sender.executor.queue.size", DEFAULT_STAT_SENDER_EXECUTOR_QUEUE_SIZE);
        this.statChannelExecutorQueueSize = profilerConfig.readInt("profiler.transport.grpc.stat.sender.channel.executor.queue.size", DEFAULT_STAT_CHANNEL_EXECUTOR_QUEUE_SIZE);

        // Span
        this.spanCollectorIp = profilerConfig.readString("profiler.transport.grpc.span.collector.ip", DEFAULT_IP, placeHolderResolver);
        this.spanCollectorPort = profilerConfig.readInt("profiler.transport.grpc.span.collector.port", DEFAULT_SPAN_COLLECTOR_PORT);
        this.spanClientOption = readSpanClientOption(profilerConfig);
        this.spanRequestTimeout = profilerConfig.readLong("profiler.transport.grpc.span.sender.request.timeout.millis", DEFAULT_CLIENT_REQUEST_TIMEOUT);
        this.spanSenderExecutorQueueSize = profilerConfig.readInt("profiler.transport.grpc.span.sender.executor.queue.size", DEFAULT_SPAN_SENDER_EXECUTOR_QUEUE_SIZE);
        this.spanChannelExecutorQueueSize = profilerConfig.readInt("profiler.transport.grpc.span.sender.channel.executor.queue.size", DEFAULT_SPAN_CHANNEL_EXECUTOR_QUEUE_SIZE);
    }

    private ClientOption readAgentClientOption(final ProfilerConfig profilerConfig) {
        return readClientOption(profilerConfig, "profiler.transport.grpc.agent.sender");
    }

    private ClientOption readMetadataClientOption(final ProfilerConfig profilerConfig) {
        return readClientOption(profilerConfig, "profiler.transport.grpc.metadata.sender");
    }

    private ClientOption readStatClientOption(final ProfilerConfig profilerConfig) {
        return readClientOption(profilerConfig, "profiler.transport.grpc.stat.sender");
    }

    private ClientOption readSpanClientOption(final ProfilerConfig profilerConfig) {
        return readClientOption(profilerConfig, "profiler.transport.grpc.span.sender");
    }

    private ClientOption readClientOption(final ProfilerConfig profilerConfig, final String transportName) {
        final ClientOption.Builder builder = new ClientOption.Builder();
        builder.setKeepAliveTime(profilerConfig.readLong(transportName + ".keepalive.time.millis", ClientOption.DEFAULT_KEEPALIVE_TIME));
        builder.setKeepAliveTimeout(profilerConfig.readLong(transportName + ".keepalive.timeout.millis", ClientOption.DEFAULT_KEEPALIVE_TIMEOUT));
        builder.setConnectTimeout(profilerConfig.readInt(transportName + ".connect.timeout.millis", ClientOption.DEFAULT_CONNECT_TIMEOUT));
        builder.setMaxHeaderListSize(readByteSize(profilerConfig, transportName + ".headers.size.max", ClientOption.DEFAULT_MAX_HEADER_LIST_SIZE));
        builder.setMaxInboundMessageSize(readByteSize(profilerConfig, transportName + ".message.inbound.size.max", ClientOption.DEFAULT_MAX_MESSAGE_SIZE));
        builder.setFlowControlWindow(readByteSize(profilerConfig, transportName + ".flow-control.window.size", ClientOption.DEFAULT_FLOW_CONTROL_WINDOW));
        builder.setWriteBufferHighWaterMark(readByteSize(profilerConfig, transportName + ".write.buffer.highwatermark", ClientOption.DEFAULT_WRITE_BUFFER_HIGH_WATER_MARK));
        builder.setWriteBufferLowWaterMark(readByteSize(profilerConfig, transportName + ".write.buffer.lowwatermark", ClientOption.DEFAULT_WRITE_BUFFER_LOW_WATER_MARK));

        return builder.build();
    }

    private int readByteSize(final ProfilerConfig profilerConfig, final String propertyName, final int defaultValue) {
        return (int) ByteSizeUnit.getByteSize(profilerConfig.readString(propertyName, ""), defaultValue);
    }

    public String getAgentCollectorIp() {
        return agentCollectorIp;
    }

    public int getAgentCollectorPort() {
        return agentCollectorPort;
    }

    public String getMetadataCollectorIp() {
        return metadataCollectorIp;
    }

    public int getMetadataCollectorPort() {
        return metadataCollectorPort;
    }

    public String getStatCollectorIp() {
        return statCollectorIp;
    }

    public int getStatCollectorPort() {
        return statCollectorPort;
    }

    public String getSpanCollectorIp() {
        return spanCollectorIp;
    }

    public int getSpanCollectorPort() {
        return spanCollectorPort;
    }

    public int getAgentSenderExecutorQueueSize() {
        return agentSenderExecutorQueueSize;
    }

    public int getMetadataSenderExecutorQueueSize() {
        return metadataSenderExecutorQueueSize;
    }

    public int getSpanSenderExecutorQueueSize() {
        return spanSenderExecutorQueueSize;
    }

    public int getStatSenderExecutorQueueSize() {
        return statSenderExecutorQueueSize;
    }

    public long getAgentRequestTimeout() {
        return agentRequestTimeout;
    }

    public long getMetadataRequestTimeout() {
        return metadataRequestTimeout;
    }

    public long getStatRequestTimeout() {
        return statRequestTimeout;
    }

    public long getSpanRequestTimeout() {
        return spanRequestTimeout;
    }

    public ClientOption getAgentClientOption() {
        return agentClientOption;
    }

    public ClientOption getMetadataClientOption() {
        return metadataClientOption;
    }

    public ClientOption getStatClientOption() {
        return statClientOption;
    }

    public ClientOption getSpanClientOption() {
        return spanClientOption;
    }

    public int getAgentChannelExecutorQueueSize() {
        return agentChannelExecutorQueueSize;
    }

    public int getMetadataChannelExecutorQueueSize() {
        return metadataChannelExecutorQueueSize;
    }

    public int getStatChannelExecutorQueueSize() {
        return statChannelExecutorQueueSize;
    }

    public int getSpanChannelExecutorQueueSize() {
        return spanChannelExecutorQueueSize;
    }

    public int getMetadataRetryMaxCount() {
        return metadataRetryMaxCount;
    }

    public int getMetadataRetryDelayMillis() {
        return metadataRetryDelayMillis;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GrpcTransportConfig{");
        sb.append("agentCollectorIp='").append(agentCollectorIp).append('\'');
        sb.append(", agentCollectorPort=").append(agentCollectorPort);
        sb.append(", metadataCollectorIp='").append(metadataCollectorIp).append('\'');
        sb.append(", metadataCollectorPort=").append(metadataCollectorPort);
        sb.append(", statCollectorIp='").append(statCollectorIp).append('\'');
        sb.append(", statCollectorPort=").append(statCollectorPort);
        sb.append(", spanCollectorIp='").append(spanCollectorIp).append('\'');
        sb.append(", spanCollectorPort=").append(spanCollectorPort);
        sb.append(", agentClientOption=").append(agentClientOption);
        sb.append(", metadataClientOption=").append(metadataClientOption);
        sb.append(", statClientOption=").append(statClientOption);
        sb.append(", spanClientOption=").append(spanClientOption);
        sb.append(", agentSenderExecutorQueueSize=").append(agentSenderExecutorQueueSize);
        sb.append(", metadataSenderExecutorQueueSize=").append(metadataSenderExecutorQueueSize);
        sb.append(", spanSenderExecutorQueueSize=").append(spanSenderExecutorQueueSize);
        sb.append(", statSenderExecutorQueueSize=").append(statSenderExecutorQueueSize);
        sb.append(", agentChannelExecutorQueueSize=").append(agentChannelExecutorQueueSize);
        sb.append(", metadataChannelExecutorQueueSize=").append(metadataChannelExecutorQueueSize);
        sb.append(", statChannelExecutorQueueSize=").append(statChannelExecutorQueueSize);
        sb.append(", spanChannelExecutorQueueSize=").append(spanChannelExecutorQueueSize);
        sb.append(", agentRequestTimeout=").append(agentRequestTimeout);
        sb.append(", metadataRequestTimeout=").append(metadataRequestTimeout);
        sb.append(", spanRequestTimeout=").append(spanRequestTimeout);
        sb.append(", statRequestTimeout=").append(statRequestTimeout);
        sb.append('}');
        return sb.toString();
    }
}