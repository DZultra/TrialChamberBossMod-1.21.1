package net.dzultra;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrialChamberBossMod implements ModInitializer {
	public static final String MOD_ID = "tcb-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[TCB Mod] Init: Loading Mod Contents");
	}
}