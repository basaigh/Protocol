package org.cloudburstmc.protocol.java;

import lombok.Data;
import org.cloudburstmc.protocol.common.MinecraftPacket;
import org.cloudburstmc.protocol.common.PacketHandler;
import org.cloudburstmc.protocol.java.packet.type.JavaPacketType;

@Data
public abstract class JavaPacket<T extends PacketHandler> implements MinecraftPacket {

    public abstract boolean handle(T handler);

    public abstract JavaPacketType getPacketType();
}
