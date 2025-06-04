package net.dzultra.networking;

import net.dzultra.TrialChamberBossMod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public record SyncTCBSpawnPillarBlockEntityS2CPayload(BlockPos blockPos, DefaultedList<ItemStack> inventory) implements CustomPayload {
    public static final Identifier SYNC_SPAWN_PILLAR_BLOCK_ENTITY_PAYLOAD_ID = Identifier.of(TrialChamberBossMod.MOD_ID, "sync_spawn_pillar_block_entity");
    public static final CustomPayload.Id<SyncTCBSpawnPillarBlockEntityS2CPayload> ID = new CustomPayload.Id<>(SYNC_SPAWN_PILLAR_BLOCK_ENTITY_PAYLOAD_ID);

    // Define a PacketCodec for DefaultedList<ItemStack>
    public static final PacketCodec<RegistryByteBuf, DefaultedList<ItemStack>> ITEM_STACK_LIST_CODEC = new PacketCodec<>() {
        @Override
        public DefaultedList<ItemStack> decode(RegistryByteBuf buf) {
            // Read the size of the list
            int size = buf.readVarInt();
            // Create a DefaultedList with the specified size
            DefaultedList<ItemStack> list = DefaultedList.ofSize(size, ItemStack.EMPTY);
            // Read each ItemStack from the buffer
            for (int i = 0; i < size; i++) {
                // Decode the ItemStack, allowing empty stacks
                ItemStack stack = ItemStack.OPTIONAL_PACKET_CODEC.decode(buf);
                list.set(i, stack);
            }
            return list;
        }

        @Override
        public void encode(RegistryByteBuf buf, DefaultedList<ItemStack> list) {
            // Write the size of the list
            buf.writeVarInt(list.size());
            // Write each ItemStack to the buffer
            for (ItemStack stack : list) {
                // Encode the ItemStack, allowing empty stacks
                ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, stack);
            }
        }
    };

    // Define the PacketCodec
    public static final PacketCodec<RegistryByteBuf, SyncTCBSpawnPillarBlockEntityS2CPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SyncTCBSpawnPillarBlockEntityS2CPayload::blockPos, // Handle BlockPos
            ITEM_STACK_LIST_CODEC, SyncTCBSpawnPillarBlockEntityS2CPayload::inventory, // Handle DefaultedList<ItemStack>
            SyncTCBSpawnPillarBlockEntityS2CPayload::new // Constructor
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
