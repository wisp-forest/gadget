package io.wispforest.gadget.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.wispforest.gadget.Gadget;
import io.wispforest.owo.config.ui.ConfigScreen;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class ConfigCommand {
    private ConfigCommand() {

    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            literal("gadget")
                .then(literal("config")
                    .executes(ConfigCommand::open)));
    }

    private static int open(CommandContext<FabricClientCommandSource> ctx) {
        var client = ctx.getSource().getClient();

        client.send(() -> {
            client.setScreen(ConfigScreen.create(Gadget.CONFIG, null));
        });

        return 1;
    }
}
