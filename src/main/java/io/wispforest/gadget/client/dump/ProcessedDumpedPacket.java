package io.wispforest.gadget.client.dump;

import io.wispforest.owo.ui.core.Component;

public record ProcessedDumpedPacket(DumpedPacket packet, Component component, String searchText) {
}
