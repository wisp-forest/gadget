package io.wispforest.gadget.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.wispforest.gadget.shell.Shell;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class RunCommand {
    private RunCommand() {

    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            literal("grun")
                .then(argument("code", StringArgumentType.greedyString())
                    .executes(RunCommand::run)));
    }

    private static int run(CommandContext<FabricClientCommandSource> ctx) {
        FabricClientCommandSource src = ctx.getSource();
        String code = StringArgumentType.getString(ctx, "code");

        CompletableFuture.runAsync(() -> {
            try (Shell sh = new Shell(line -> src.getClient().execute(() -> src.sendFeedback(Text.literal(line))), line -> src.getClient().execute(() -> src.sendError(Text.literal(line))))) {
                sh.run(code);
            }
        });

        return 0;
    }
}
