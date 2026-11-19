package org.nerdssociety.eaglepad;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import org.nerdssociety.eaglepad.client.controller.ControllerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EaglePadClient implements ClientModInitializer {

    public static final String MOD_ID = "eaglepad";

    public static final Logger LOGGER =
        LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {

        LOGGER.info("EaglePad client initialized.");

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            LOGGER.info("Starting EaglePad controller detection.");

            ControllerManager.initialize();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ControllerManager.tick();
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ControllerManager.shutdown();
        });
    }
}