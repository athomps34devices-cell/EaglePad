package org.nerdssociety.eaglepad.client.controller;

import org.lwjgl.sdl.SDLGamepad;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLStdinc;
import org.nerdssociety.eaglepad.EaglePadClient;

import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ControllerManager {

    private static final Map<Integer, Controller> CONTROLLERS = new LinkedHashMap<>();

    private static final int SCAN_INTERVAL_TICKS = 20;

    private static boolean initialized;
    private static boolean ownsGamepadSubsystem;
    private static int scanTicks;

    private ControllerManager() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        try {
            int activeSubsystems = SDLInit.SDL_WasInit(SDLInit.SDL_INIT_GAMEPAD);

            if ((activeSubsystems & SDLInit.SDL_INIT_GAMEPAD) == 0) {
                EaglePadClient.LOGGER.info("Initializing SDL Gamepad subsystem.");

                if (!SDLInit.SDL_InitSubSystem(SDLInit.SDL_INIT_GAMEPAD)) {
                    EaglePadClient.LOGGER.error("Could not initialize SDL Gamepad subsystem.");
                    return;
                }

                ownsGamepadSubsystem = true;
            } else {
                EaglePadClient.LOGGER.info("Using the existing SDL Gamepad subsystem.");
            }

            initialized = true;

            EaglePadClient.LOGGER.info("EaglePad controller detection started.");

            scanControllers();

        } catch (Throwable exception) {
            EaglePadClient.LOGGER.error(
                "Failed to initialize controller detection.",
                exception
            );
        }
    }

    public static void tick() {
        if (!initialized) {
            return;
        }

        scanTicks++;

        if (scanTicks >= SCAN_INTERVAL_TICKS) {
            scanTicks = 0;
            scanControllers();
        }
    }

    private static void scanControllers() {
        IntBuffer gamepadIds = null;

        try {
            gamepadIds = SDLGamepad.SDL_GetGamepads();

            if (gamepadIds == null) {
                EaglePadClient.LOGGER.warn("SDL returned no gamepad list.");
                return;
            }

            Set<Integer> connectedIds = new HashSet<>();

            for (int i = gamepadIds.position(); i < gamepadIds.limit(); i++) {
                int instanceId = gamepadIds.get(i);

                connectedIds.add(instanceId);

                if (CONTROLLERS.containsKey(instanceId)) {
                    continue;
                }

                int sdlType = SDLGamepad.SDL_GetGamepadTypeForID(instanceId);

                String name = SDLGamepad.SDL_GetGamepadNameForID(instanceId);

                if (name == null || name.isBlank()) {
                    name = "Unknown Controller";
                }

                ControllerType type = ControllerType.fromSDL(sdlType);

                Controller controller = new Controller(
                    instanceId,
                    name,
                    type
                );

                CONTROLLERS.put(instanceId, controller);

                EaglePadClient.LOGGER.info(
                    "Controller connected: {} | Type: {} | ID: {}",
                    controller.name(),
                    controller.type().getDisplayName(),
                    controller.instanceId()
                );
            }

            Iterator<Map.Entry<Integer, Controller>> iterator =
                CONTROLLERS.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Integer, Controller> entry = iterator.next();

                if (!connectedIds.contains(entry.getKey())) {
                    Controller controller = entry.getValue();

                    EaglePadClient.LOGGER.info(
                        "Controller disconnected: {} | ID: {}",
                        controller.name(),
                        controller.instanceId()
                    );

                    iterator.remove();
                }
            }

        } catch (Throwable exception) {
            EaglePadClient.LOGGER.error(
                "Error while scanning connected controllers.",
                exception
            );

        } finally {
            if (gamepadIds != null) {
                SDLStdinc.SDL_free(gamepadIds);
            }
        }
    }

    public static Map<Integer, Controller> getControllers() {
        return Map.copyOf(CONTROLLERS);
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void shutdown() {
        CONTROLLERS.clear();

        if (ownsGamepadSubsystem) {
            try {
                SDLInit.SDL_QuitSubSystem(SDLInit.SDL_INIT_GAMEPAD);
            } catch (Throwable exception) {
                EaglePadClient.LOGGER.error(
                    "Failed to release SDL Gamepad subsystem.",
                    exception
                );
            }
        }

        ownsGamepadSubsystem = false;
        initialized = false;
        scanTicks = 0;

        EaglePadClient.LOGGER.info("EaglePad controller detection stopped.");
    }
}