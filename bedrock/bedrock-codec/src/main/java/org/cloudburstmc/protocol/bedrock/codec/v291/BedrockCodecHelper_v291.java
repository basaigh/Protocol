package org.cloudburstmc.protocol.bedrock.codec.v291;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.nbt.NBTInputStream;
import com.nukkitx.nbt.NBTOutputStream;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.cloudburstmc.protocol.bedrock.codec.BaseBedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.data.GameRuleData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandEnumConstraint;
import org.cloudburstmc.protocol.bedrock.data.command.CommandEnumData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginType;
import org.cloudburstmc.protocol.bedrock.data.defintions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.entity.*;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.common.util.TypeMap;
import org.cloudburstmc.protocol.common.util.VarInts;
import org.cloudburstmc.protocol.common.util.stream.LittleEndianByteBufOutputStream;

import java.io.IOException;
import java.util.*;

import static org.cloudburstmc.protocol.bedrock.data.entity.EntityData.Type;
import static org.cloudburstmc.protocol.common.util.Preconditions.checkNotNull;

public class BedrockCodecHelper_v291 extends BaseBedrockCodecHelper {

    public BedrockCodecHelper_v291(TypeMap<EntityData> entityData, TypeMap<EntityData.Type> entityDataTypes,
                                   TypeMap<EntityFlag> entityFlags, TypeMap<Class<?>> gameRulesTypes) {
        super(entityData, entityDataTypes, entityFlags, gameRulesTypes);
    }

    @Override
    public EntityLinkData readEntityLink(ByteBuf buffer) {

        long from = VarInts.readLong(buffer);
        long to = VarInts.readLong(buffer);
        int type = buffer.readUnsignedByte();
        boolean immediate = buffer.readBoolean();

        return new EntityLinkData(from, to, EntityLinkData.Type.values()[type], immediate);
    }

    @Override
    public void writeEntityLink(ByteBuf buffer, EntityLinkData entityLink) {
        checkNotNull(entityLink, "entityLink");

        VarInts.writeLong(buffer, entityLink.getFrom());
        VarInts.writeLong(buffer, entityLink.getTo());
        buffer.writeByte(entityLink.getType().ordinal());
        buffer.writeBoolean(entityLink.isImmediate());
    }

    @Override
    public ItemData readNetItem(ByteBuf buffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeNetItem(ByteBuf buffer, ItemData item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemData readItem(ByteBuf buffer) {
        int runtimeId = VarInts.readInt(buffer);
        if (runtimeId == 0) {
            // We don't need to read anything extra.
            return ItemData.AIR;
        }
        ItemDefinition definition = this.itemDefinitions.getDefinition(runtimeId);
        int aux = VarInts.readInt(buffer);
        int damage = (short) (aux >> 8);
        if (damage == Short.MAX_VALUE) damage = -1;
        int count = aux & 0xff;
        short nbtSize = buffer.readShortLE();

        NbtMap compoundTag = null;
        if (nbtSize > 0) {
            try (NBTInputStream reader = NbtUtils.createReaderLE(new ByteBufInputStream(buffer.readSlice(nbtSize)))) {
                Object tag = reader.readTag();
                if (tag instanceof NbtMap) {
                    compoundTag = (NbtMap) tag;
                }
            } catch (IOException e) {
                throw new IllegalStateException("Unable to load NBT data", e);
            }
        }

        String[] canPlace = readArray(buffer, new String[0], this::readString);
        String[] canBreak = readArray(buffer, new String[0], this::readString);

        return ItemData.builder()
                .definition(definition)
                .damage(damage)
                .count(count)
                .tag(compoundTag)
                .canPlace(canPlace)
                .canBreak(canBreak)
                .build();
    }

    @Override
    public void writeItem(ByteBuf buffer, ItemData item) {
        checkNotNull(item, "item");

        // Write id
        ItemDefinition definition = item.getDefinition();
        if (isAir(definition)) {
            // We don't need to write anything extra.
            buffer.writeByte(0);
            return;
        }
        VarInts.writeInt(buffer, definition.getRuntimeId());

        // Write damage and count
        int damage = item.getDamage();
        if (damage == -1) damage = Short.MAX_VALUE;
        VarInts.writeInt(buffer, (damage << 8) | (item.getCount() & 0xff));

        // Remember this position, since we'll be writing the true NBT size here later:
        int sizeIndex = buffer.writerIndex();
        buffer.writeShortLE(0);

        if (item.getTag() != null) {
            int afterSizeIndex = buffer.writerIndex();
            try (NBTOutputStream stream = new NBTOutputStream(new LittleEndianByteBufOutputStream(buffer))) {
                stream.writeTag(item.getTag());
            } catch (IOException e) {
                // This shouldn't happen (as this is backed by a Netty ByteBuf), but okay...
                throw new IllegalStateException("Unable to save NBT data", e);
            }
            // Set to the written NBT size
            buffer.setShortLE(sizeIndex, buffer.writerIndex() - afterSizeIndex);
        }

        writeArray(buffer, item.getCanPlace(), this::writeString);
        writeArray(buffer, item.getCanBreak(), this::writeString);
    }

    @Override
    public ItemData readItemInstance(ByteBuf buffer) {
        return readItem(buffer);
    }

    @Override
    public void writeItemInstance(ByteBuf buffer, ItemData item) {
        writeItem(buffer, item);
    }

    @Override
    public CommandOriginData readCommandOrigin(ByteBuf buffer) {
        CommandOriginType origin = CommandOriginType.values()[VarInts.readUnsignedInt(buffer)];
        UUID uuid = readUuid(buffer);
        String requestId = readString(buffer);
        long varLong = -1;
        if (origin == CommandOriginType.DEV_CONSOLE || origin == CommandOriginType.TEST) {
            varLong = VarInts.readLong(buffer);
        }
        return new CommandOriginData(origin, uuid, requestId, varLong);
    }

    @Override
    public void writeCommandOrigin(ByteBuf buffer, CommandOriginData originData) {
        checkNotNull(originData, "commandOriginData");
        VarInts.writeUnsignedInt(buffer, originData.getOrigin().ordinal());
        writeUuid(buffer, originData.getUuid());
        writeString(buffer, originData.getRequestId());
        if (originData.getOrigin() == CommandOriginType.DEV_CONSOLE || originData.getOrigin() == CommandOriginType.TEST) {
            VarInts.writeLong(buffer, originData.getEvent());
        }
    }

    @Override
    public GameRuleData<?> readGameRule(ByteBuf buffer) {

        String name = readString(buffer);
        int type = VarInts.readUnsignedInt(buffer);

        switch (type) {
            case 1:
                return new GameRuleData<>(name, buffer.readBoolean());
            case 2:
                return new GameRuleData<>(name, VarInts.readUnsignedInt(buffer));
            case 3:
                return new GameRuleData<>(name, buffer.readFloatLE());
        }
        throw new IllegalStateException("Invalid gamerule type received");
    }

    @Override
    public void writeGameRule(ByteBuf buffer, GameRuleData<?> gameRule) {
        checkNotNull(gameRule, "gameRule");

        Object value = gameRule.getValue();
        int type = this.gameRuleType.getId(value.getClass());

        writeString(buffer, gameRule.getName());
        VarInts.writeUnsignedInt(buffer, type);
        switch (type) {
            case 1:
                buffer.writeBoolean((boolean) value);
                break;
            case 2:
                VarInts.writeUnsignedInt(buffer, (int) value);
                break;
            case 3:
                buffer.writeFloatLE((float) value);
                break;
        }
    }

    @Override
    public void readEntityData(ByteBuf buffer, EntityDataMap entityDataMap) {
        checkNotNull(entityDataMap, "entityDataDictionary");

        int length = VarInts.readUnsignedInt(buffer);

        for (int i = 0; i < length; i++) {
            int metadataInt = VarInts.readUnsignedInt(buffer);
            EntityData entityData = this.entityData.getType(metadataInt);
            EntityData.Type type = this.entityDataTypes.getType(VarInts.readUnsignedInt(buffer));
            if (entityData != null && entityData.isFlags()) {
                if (type != Type.LONG) {
                    throw new IllegalArgumentException("Expected long value for flags, got " + type.name());
                }
                type = Type.FLAGS;
            }

            Object object;
            switch (type) {
                case BYTE:
                    object = buffer.readByte();
                    break;
                case SHORT:
                    object = buffer.readShortLE();
                    break;
                case INT:
                    object = VarInts.readInt(buffer);
                    break;
                case FLOAT:
                    object = buffer.readFloatLE();
                    break;
                case STRING:
                    object = readString(buffer);
                    break;
                case NBT:
                    object = this.readItem(buffer);
                    break;
                case VECTOR3I:
                    object = readVector3i(buffer);
                    break;
                case FLAGS:
                    int index = entityData == EntityData.FLAGS_2 ? 1 : 0;
                    entityDataMap.getOrCreateFlags().set(VarInts.readLong(buffer), index, this.entityFlags);
                    continue;
                case LONG:
                    object = VarInts.readLong(buffer);
                    break;
                case VECTOR3F:
                    object = readVector3f(buffer);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown entity data type received");
            }
            if (entityData != null) {
                entityDataMap.put(entityData, object);
            } else {
                log.debug("Unknown entity data: {} type {} value {}", metadataInt, type, object);
            }
        }
    }

    @Override
    public void writeEntityData(ByteBuf buffer, EntityDataMap entityDataMap) {
        checkNotNull(entityDataMap, "entityDataDictionary");

        VarInts.writeUnsignedInt(buffer, entityDataMap.size());

        for (Map.Entry<EntityData, Object> entry : entityDataMap.entrySet()) {
            int index = buffer.writerIndex();
            VarInts.writeUnsignedInt(buffer, this.entityData.getId(entry.getKey()));
            Object object = entry.getValue();
            EntityData.Type type = EntityData.Type.from(object);

            int typeId;
            if (type == EntityData.Type.FLAGS) {
                typeId = this.entityDataTypes.getId(EntityData.Type.LONG);
            } else {
                typeId = this.entityDataTypes.getId(type);
            }
            VarInts.writeUnsignedInt(buffer, typeId);

            switch (type) {
                case BYTE:
                    buffer.writeByte((byte) object);
                    break;
                case SHORT:
                    buffer.writeShortLE((short) object);
                    break;
                case INT:
                    VarInts.writeInt(buffer, (int) object);
                    break;
                case FLOAT:
                    buffer.writeFloatLE((float) object);
                    break;
                case STRING:
                    writeString(buffer, (String) object);
                    break;
                case NBT:
                    ItemData item;
                    if (object instanceof NbtMap) {
                        item = ItemData.builder()
                                .definition(ItemDefinition.LEGACY_FIREWORK)
                                .damage(0)
                                .count(1)
                                .tag((NbtMap) object)
                                .build();
                    } else {
                        item = (ItemData) object;
                    }
                    this.writeItem(buffer, item);
                    break;
                case VECTOR3I:
                    writeVector3i(buffer, (Vector3i) object);
                    break;
                case FLAGS:
                    int flagsIndex = entry.getKey() == EntityData.FLAGS_2 ? 1 : 0;
                    object = ((EntityFlags) object).get(flagsIndex, this.entityFlags);
                case LONG:
                    VarInts.writeLong(buffer, (long) object);
                    break;
                case VECTOR3F:
                    writeVector3f(buffer, (Vector3f) object);
                    break;
                default:
                    buffer.writerIndex(index);
                    break;
            }
        }
    }

    @Override
    public CommandEnumData readCommandEnum(ByteBuf buffer, boolean soft) {

        String name = readString(buffer);

        int count = VarInts.readUnsignedInt(buffer);
        LinkedHashMap<String, Set<CommandEnumConstraint>> values = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            values.put(readString(buffer), Collections.emptySet());
        }
        return new CommandEnumData(name, values, soft);
    }

    @Override
    public void writeCommandEnum(ByteBuf buffer, CommandEnumData enumData) {
        checkNotNull(enumData, "enumData");

        writeString(buffer, enumData.getName());

        Set<String> values = enumData.getValues().keySet();
        VarInts.writeUnsignedInt(buffer, values.size());
        for (String value : values) {
            writeString(buffer, value);
        }
    }
}
