package io.wispforest.gadget.dump.read.unwrapped;

import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.util.ErrorSink;
import io.wispforest.gadget.util.FormattedDumper;
import io.wispforest.owo.ui.container.FlowLayout;

public record UnprocessedUnwrappedPacket(byte[] bytes) implements UnwrappedPacket {
    @Override
    public void dumpAsPlainText(FormattedDumper out, int indent, ErrorSink errSink) {
        out.writeHexDump(indent, bytes);
    }

    @Override
    public void render(FlowLayout out, ErrorSink errSink) {
        out.child(GuiUtil.hexDump(bytes, true));
    }
}
