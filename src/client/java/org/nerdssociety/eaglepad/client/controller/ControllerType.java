package org.nerdssociety.eaglepad.client.controller;

import org.lwjgl.sdl.SDLGamepad;

public enum ControllerType {
    SWITCH_PRO,
    JOYCON_LEFT,
    JOYCON_RIGHT,
    JOYCON_PAIR,
    OTHER;

    public static ControllerType fromSDL(int type) {
        if (type == SDLGamepad.SDL_GAMEPAD_TYPE_NINTENDO_SWITCH_PRO) {
            return SWITCH_PRO;
        }

        if (type == SDLGamepad.SDL_GAMEPAD_TYPE_NINTENDO_SWITCH_JOYCON_LEFT) {
            return JOYCON_LEFT;
        }

        if (type == SDLGamepad.SDL_GAMEPAD_TYPE_NINTENDO_SWITCH_JOYCON_RIGHT) {
            return JOYCON_RIGHT;
        }

        if (type == SDLGamepad.SDL_GAMEPAD_TYPE_NINTENDO_SWITCH_JOYCON_PAIR) {
            return JOYCON_PAIR;
        }

        return OTHER;
    }

    public String getDisplayName() {
        return switch (this) {
            case SWITCH_PRO -> "Nintendo Switch Pro Controller";
            case JOYCON_LEFT -> "Nintendo Switch Left Joy-Con";
            case JOYCON_RIGHT -> "Nintendo Switch Right Joy-Con";
            case JOYCON_PAIR -> "Nintendo Switch Joy-Con Pair";
            case OTHER -> "Other Controller";
        };
    }
}