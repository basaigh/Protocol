package org.cloudburstmc.protocol.java.packet.play.clientbound;

import com.nukkitx.math.vector.Vector2f;
import com.nukkitx.math.vector.Vector3d;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cloudburstmc.protocol.common.PacketSignal;
import org.cloudburstmc.protocol.java.data.entity.EntityType;
import org.cloudburstmc.protocol.java.data.entity.spawn.SpawnData;
import org.cloudburstmc.protocol.java.packet.JavaPacket;
import org.cloudburstmc.protocol.java.packet.handler.JavaPlayPacketHandler;
import org.cloudburstmc.protocol.java.packet.type.JavaPacketType;
import org.cloudburstmc.protocol.java.packet.type.JavaPlayPacketType;

import java.util.UUID;

@Data
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
public class AddEntityPacket implements JavaPacket<JavaPlayPacketHandler> {
    private int entityId;
    private UUID uuid;
    private EntityType entityType;
    private Vector3d position;
    private Vector2f rotation;
    private SpawnData spawnData;
    private Vector3d velocity;

    @Override
    public PacketSignal handle(JavaPlayPacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public JavaPacketType getPacketType() {
        return JavaPlayPacketType.ADD_ENTITY;
    }
}
