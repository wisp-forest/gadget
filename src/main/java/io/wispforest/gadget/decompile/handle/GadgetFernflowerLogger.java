package io.wispforest.gadget.decompile.handle;

import net.minecraft.text.Text;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

public class GadgetFernflowerLogger extends IFernflowerLogger {
    private final QuiltflowerHandlerImpl handler;

    public GadgetFernflowerLogger(QuiltflowerHandlerImpl handler) {
        this.handler = handler;
    }

    @Override
    public void writeMessage(String message, Severity severity) {
        if (severity == Severity.INFO) {
            handler.logConsumer.accept(Text.translatable("text.gadget.quiltflower_log.info", message));
        } else if (severity == Severity.WARN) {
            handler.logConsumer.accept(Text.translatable("text.gadget.quiltflower_log.warn", message));
        } else if (severity == Severity.ERROR) {
            handler.logConsumer.accept(Text.translatable("text.gadget.quiltflower_log.error", message));
        }
    }

    @Override
    public void writeMessage(String message, Severity severity, Throwable t) {
        CharArrayWriter writer = new CharArrayWriter();
        t.printStackTrace(new PrintWriter(writer));
        String fullExceptionText = writer.toString().replace("\t", "    ");

        if (severity == Severity.INFO) {
            handler.logConsumer.accept(
                Text.translatable("text.gadget.quiltflower_log.info.with_error", message, fullExceptionText));
        } else if (severity == Severity.WARN) {
            handler.logConsumer.accept(
                Text.translatable("text.gadget.quiltflower_log.warn.with_error", message, fullExceptionText));
        } else if (severity == Severity.ERROR) {
            handler.logConsumer.accept(
                Text.translatable("text.gadget.quiltflower_log.error.with_error", message, fullExceptionText));
        }
    }
}
