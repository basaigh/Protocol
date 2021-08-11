package org.cloudburstmc.protocol.bedrock.raknet.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import org.cloudburstmc.protocol.bedrock.BedrockClient;
import org.cloudburstmc.protocol.bedrock.BedrockServer;

public class ClientChannelInitializer extends ChannelInitializer<Channel> {

    private final BedrockClient client;

    public ClientChannelInitializer(BedrockClient client) {
        this.client = client;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(ClientPeerInitializer.NAME, new ClientPeerInitializer(this.client));
        // TODO: ping handler?
    }
}
