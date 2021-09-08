package org.cloudburstmc.protocol.bedrock.packet;

import io.netty.buffer.ByteBuf;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCounted;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@ToString(doNotUseGetters = true, exclude = {"data"})
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
public class LevelChunkPacket extends AbstractReferenceCounted implements BedrockPacket {
    private int chunkX;
    private int chunkZ;
    private int subChunksLength;
    private boolean cachingEnabled;
    private final LongList blobIds = new LongArrayList();
    private ByteBuf data;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.LEVEL_CHUNK;
    }

    @Override
    public LevelChunkPacket touch(Object hint) {
        this.data.touch(hint);
        return this;
    }

    @Override
    protected void deallocate() {
        this.data.release();
    }
}
