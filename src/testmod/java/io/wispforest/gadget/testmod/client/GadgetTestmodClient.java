package io.wispforest.gadget.testmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GadgetTestmodClient implements ClientModInitializer {
    public static final FunnyItem FUNNY_ITEM = new FunnyItem();

    @Override
    public void onInitializeClient() {
        Registry.register(Registries.ITEM, new Identifier("gadget-testmod", "funny"), FUNNY_ITEM);

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
