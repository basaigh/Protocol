package org.cloudburstmc.protocol.java.codec.v754.serializer.play.serverbound;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloudburstmc.protocol.common.exception.PacketSerializeException;
import org.cloudburstmc.protocol.java.JavaPacketHelper;
import org.cloudburstmc.protocol.java.JavaPacketSerializer;
import org.cloudburstmc.protocol.java.packet.play.serverbound.CommandSuggestionPacket;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandSuggestionSerializer_v754 implements JavaPacketSerializer<CommandSuggestionPacket> {
    public static final CommandSuggestionSerializer_v754 INSTANCE = new CommandSuggestionSerializer_v754();

    @Override
    public void serialize(ByteBuf buffer, JavaPacketHelper helper, CommandSuggestionPacket packet) throws PacketSerializeException {
        helper.writeVarInt(buffer, packet.getId());
        helper.writeString(buffer, packet.getCommand());
    }

    @Override
    public void deserialize(ByteBuf buffer, JavaPacketHelper helper, CommandSuggestionPacket packet) throws PacketSerializeException {
        packet.setId(helper.readVarInt(buffer));
        packet.setCommand(helper.readString(buffer));
    }
}
