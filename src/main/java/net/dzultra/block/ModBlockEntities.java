package net.dzultra.block;

import net.dzultra.TrialChamberBossMod;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<BossSpawnPillarBlockEntity> SPAWN_PILLAR_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(TrialChamberBossMod.MOD_ID, "spawn_pillar_be"),
                    BlockEntityType.Builder.create(BossSpawnPillarBlockEntity::new, ModBlocks.BOSS_SPAWN_PILLAR).build(null));
}
