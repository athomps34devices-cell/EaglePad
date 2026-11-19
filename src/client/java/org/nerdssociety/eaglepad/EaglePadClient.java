package org.nerdssociety.eaglepad;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EaglePadClient implements ClientModInitializer {
    public static final String MOD_ID = "eaglepad";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("EaglePad client initialized.");

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if (client.player != null) {
                client.player.sendSystemMessage(
                    Component.literal(
                        "EaglePad loaded. Controller input support is being initialized."
                    )
                );
            }
        });
    }
}