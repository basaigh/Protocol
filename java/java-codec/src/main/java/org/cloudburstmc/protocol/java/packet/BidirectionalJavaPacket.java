package org.cloudburstmc.protocol.java.packet;

import org.cloudburstmc.protocol.common.PacketHandler;
import org.cloudburstmc.protocol.java.packet.type.JavaPacketType;

public interface BidirectionalJavaPacket<T extends PacketHandler> extends JavaPacket<T> {
    JavaPacketType.Direction sendingDirection = null;
}
