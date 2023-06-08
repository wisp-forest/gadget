package io.wispforest.gadget.dump.read.unwrapped;

import io.wispforest.gadget.util.ErrorSink;
import io.wispforest.gadget.util.FormattedDumper;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

/**
 * Represents a deserialized custom packet.
 */
public interface UnwrappedPacket {
    UnwrappedPacket NULL = new UnwrappedPacket() {
        @Override
        public void dumpAsPlainText(FormattedDumper out, int indent, ErrorSink errSink) {
        }

        @Override
        public void render(FlowLayout out, ErrorSink errSink) {
        }
    };

    default void gatherSearchText(StringBuilder out, ErrorSink errSink) {
    }

    default void dumpAsPlainText(FormattedDumper out, int indent, ErrorSink errSink) {
        out.write(indent, "! " + getClass().getName() + " doesn't support plain-text dumps.");
    }

    @Environment(EnvType.CLIENT)
    default void render(FlowLayout out, ErrorSink errSink) {
        out.child(Components.label(Text.translatable("text.gadget.deserialized_packet_no_render", getClass().getName())));
    }
}
