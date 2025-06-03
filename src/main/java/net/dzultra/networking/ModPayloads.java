package net.dzultra.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ModPayloads {

    public static void registerModPayloads() {
        PayloadTypeRegistry.playS2C().register(SyncTCBSpawnPillarBlockEntityS2CPayload.ID, SyncTCBSpawnPillarBlockEntityS2CPayload.CODEC);
    }
}
