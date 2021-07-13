package org.cloudburstmc.protocol.java.packet;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.cloudburstmc.protocol.common.PacketHandler;
import org.cloudburstmc.protocol.java.packet.type.JavaPacketType;

public interface BidirectionalJavaPacket<T extends PacketHandler> extends JavaPacket<T> {
    @Getter
    @Setter(AccessLevel.PACKAGE)
    JavaPacketType.Direction sendingDirection = null;
}
