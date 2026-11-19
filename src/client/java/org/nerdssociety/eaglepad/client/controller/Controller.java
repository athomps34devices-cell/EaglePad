package org.nerdssociety.eaglepad.client.controller;

public record Controller(
    int instanceId,
    String name,
    ControllerType type
) {
}