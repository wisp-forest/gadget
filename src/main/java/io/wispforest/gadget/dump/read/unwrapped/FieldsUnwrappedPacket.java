package io.wispforest.gadget.dump.read.unwrapped;

import io.wispforest.gadget.client.field.FieldDataIsland;
import io.wispforest.gadget.field.DefaultFieldDataHolder;
import io.wispforest.gadget.field.LocalFieldDataSource;
import io.wispforest.gadget.util.ErrorSink;
import io.wispforest.gadget.util.FormattedDumper;
import io.wispforest.gadget.util.ReflectionUtil;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

/**
 * An unwrapped packet represented by an object with important field data.
 */
public interface FieldsUnwrappedPacket extends UnwrappedPacket {
    /**
     * {@return the object to read field data from, null if no fields are present}
     */
    @Nullable Object rawFieldsObject();

    default OptionalInt packetId() {
        return OptionalInt.empty();
    }

    default @Nullable Text headText() {
        MutableText headText = Text.literal(ReflectionUtil.nameWithoutPackage(rawFieldsObject().getClass()));

        if (packetId().isPresent()) {
            headText.append(Text.literal(" #" + packetId().getAsInt())
                .formatted(Formatting.GRAY));
        }

        return headText;
    }

    @Override
    default void gatherSearchText(StringBuilder out, ErrorSink errSink) {
        Text headText = headText();

        if (headText != null)
            out.append(" ").append(headText.getString());
    }

    @Override
    default void dumpAsPlainText(FormattedDumper out, int indent, ErrorSink errSink) {
        Text headText = headText();

        if (headText != null)
            out.write(indent, headText.getString());

        Object rawFields = rawFieldsObject();

        if (rawFields != null) {
            DefaultFieldDataHolder holder = new DefaultFieldDataHolder(
                new LocalFieldDataSource(rawFields, false),
                true
            );

            holder.dumpToText(out, indent, holder.root(), 5)
                .join();
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    default void render(FlowLayout out, ErrorSink errSink) {
        Text headText = headText();

        if (headText != null)
            out.child(Components.label(headText));

        Object rawFields = rawFieldsObject();

        if (rawFields != null) {
            FieldDataIsland island = new FieldDataIsland(
                new LocalFieldDataSource(rawFields, false),
                true,
                false
            );

            out.child(island.mainContainer());
        }
    }
}
