package org.cloudburstmc.protocol.java.codec;

import org.cloudburstmc.protocol.common.exception.PacketSerializeException;
import org.cloudburstmc.protocol.common.serializer.PacketSerializer;
import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.java.packet.JavaPacket;
import org.cloudburstmc.protocol.java.JavaSession;

public interface JavaPacketSerializer<T extends JavaPacket<?>> extends PacketSerializer<T, JavaCodecHelper> {

    @Override
    default void serialize(ByteBuf buffer, JavaCodecHelper helper, T packet) throws PacketSerializeException {
    }

    default void serialize(ByteBuf buffer, JavaCodecHelper helper, T packet, JavaSession session) throws PacketSerializeException {
        this.serialize(buffer, helper, packet);
    }

    @Override
    default void deserialize(ByteBuf buffer, JavaCodecHelper helper, T packet) throws PacketSerializeException {
    }

    default void deserialize(ByteBuf buffer, JavaCodecHelper helper, T packet, JavaSession session) throws PacketSerializeException {
        this.deserialize(buffer, helper, packet);
    }
}
