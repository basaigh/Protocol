package org.cloudburstmc.protocol.bedrock.data.inventory;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Value;
import org.cloudburstmc.protocol.bedrock.data.defintions.ItemDefinition;

@Value
public class MaterialReducer {
    int inputId;
    Object2IntMap<ItemDefinition> itemCounts;
}
