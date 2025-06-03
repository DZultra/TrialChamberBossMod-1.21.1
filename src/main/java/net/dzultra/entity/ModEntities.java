package net.dzultra.entity;

import net.dzultra.TrialChamberBossMod;
import net.dzultra.entity.custom.BossEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<BossEntity> TRIAL_CHAMBER_BOSS = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(TrialChamberBossMod.MOD_ID, "trial_chamber_boss_entity"),
            EntityType.Builder.create(BossEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.5f, 0.5f).build());


    public static void registerModEntities() {

    }
}
