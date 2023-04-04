package io.wispforest.gadget.testmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GadgetTestmodClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("gadget-testmod")
                .then(literal("epic")
                    .executes(ctx -> {
                        ClientPlayNetworking.send(new EpicPacket("cringe"));
                        return 1;
                    })));
        });

        ServerPlayNetworking.registerGlobalReceiver(EpicPacket.TYPE, (pkt, player, sender) -> {
            // Do nothing.
        });
    }
}
