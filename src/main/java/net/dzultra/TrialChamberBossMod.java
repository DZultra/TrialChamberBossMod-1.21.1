package net.dzultra;

import net.dzultra.entity.ModEntities;
import net.dzultra.item.ModItemGroups;
import net.dzultra.item.ModItems;
import net.dzultra.networking.ModPayloads;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrialChamberBossMod implements ModInitializer {
	public static final String MOD_ID = "tcb-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[TCB Mod] Init: Loading Mod Contents");
		ModPayloads.registerModPayloads();
		ModEntities.registerModEntities();
		ModItems.registerModItems();
		ModItemGroups.registerModItemGroups();
	}
}