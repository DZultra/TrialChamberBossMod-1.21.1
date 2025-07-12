package net.dzultra.item;

import net.dzultra.TrialChamberBossMod;
import net.dzultra.entity.ModEntities;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {

    public static final Item TRIAL_CHAMBER_BOSS_SPAWN_EGG = registerItem("tcb_spawn_egg",
            new SpawnEggItem(ModEntities.TRIAL_CHAMBER_BOSS, 0xFFD67B5B, 0xFFE3826C, new Item.Settings()));

    public static final Item SPAWN_SHARD = registerItem("spawn_shard",
            new Item(new Item.Settings().rarity(Rarity.EPIC)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(TrialChamberBossMod.MOD_ID, name), item);
    }

    public static void registerModItems() {}
}

