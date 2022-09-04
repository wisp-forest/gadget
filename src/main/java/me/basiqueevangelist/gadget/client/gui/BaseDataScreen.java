package me.basiqueevangelist.gadget.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import me.basiqueevangelist.gadget.desc.ComplexFieldObject;
import me.basiqueevangelist.gadget.desc.ErrorFieldObject;
import me.basiqueevangelist.gadget.desc.SimpleFieldObject;
import me.basiqueevangelist.gadget.network.FieldData;
import me.basiqueevangelist.gadget.path.ObjectPath;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.TreeMap;

public abstract class BaseDataScreen extends BaseOwoScreen<VerticalFlowLayout> {
    protected final Map<ObjectPath, ClientFieldData> fields = new TreeMap<>();
    protected VerticalFlowLayout mainContainer;

    protected abstract void requestPath(ObjectPath path);

    public abstract boolean isClient();

    protected abstract void switchToClient();

    protected abstract void switchToServer();

    @Override
    protected @NotNull OwoUIAdapter<VerticalFlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(VerticalFlowLayout verticalFlowLayout) {
        verticalFlowLayout
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)
            .surface(Surface.VANILLA_TRANSLUCENT);


        VerticalFlowLayout main = Containers.verticalFlow(Sizing.fill(100), Sizing.content());

        ScrollContainer<VerticalFlowLayout> scroll = Containers.verticalScroll(Sizing.fill(95), Sizing.fill(100), main);

        verticalFlowLayout.child(scroll.child(main));

        this.mainContainer = main;

        main
            .padding(Insets.of(15));

        for (var entry : fields.entrySet()) {
            if (entry.getKey().steps().length != 1) continue;

            var data = entry.getValue();

            makeComponent(main, entry.getKey(), data);
        }

        VerticalFlowLayout sidebar = Containers.verticalFlow(Sizing.content(), Sizing.content());

        var switchButton = Containers.verticalFlow(Sizing.fixed(16), Sizing.fixed(16))
            .child(Components.label(Text.translatable("text.gadget." + (isClient() ? "client" : "server") + "_current"))
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .positioning(Positioning.absolute(5, 4))
                .cursorStyle(CursorStyle.HAND)
            );

        switchButton
            .cursorStyle(CursorStyle.HAND)
            .tooltip(Text.translatable("text.gadget.switch_to_" + (isClient() ? "server" : "client")));

        switchButton.mouseEnter().subscribe(
            () -> switchButton.surface(Surface.flat(0x80ffffff)));

        switchButton.mouseLeave().subscribe(
            () -> switchButton.surface(Surface.BLANK));

        switchButton.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

            UISounds.playButtonSound();

            if (isClient())
                switchToServer();
            else
                switchToClient();

            return true;
        });

        sidebar
            .child(switchButton)
            .positioning(Positioning.absolute(0, 0))
            .padding(Insets.of(5));

        verticalFlowLayout.child(sidebar);
    }

    private void makeComponent(VerticalFlowLayout container, ObjectPath path, ClientFieldData data) {
        var rowContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        var row = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());

        data.containerComponent = rowContainer;

        rowContainer.child(row);

        var nameText = Text.literal(path.name());

        if (data.isMixin)
            nameText.formatted(Formatting.GRAY)
                .styled(x -> x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.literal("Mixin-injected field")
                        .formatted(Formatting.YELLOW))));

        row.child(Components.label(Text.literal(data.obj.type().substring(0, 1)).styled(x -> x.withColor(data.obj.color())))
                .margins(Insets.right(5)))
            .child(Components.label(nameText)
                .margins(Insets.right(0)))
            .margins(Insets.both(0, 2))
            .allowOverflow(true);

        if (data.obj instanceof SimpleFieldObject sfo) {
            row.child(
                Components.label(
                    Text.literal(" = " + sfo.contents())
                        .formatted(Formatting.GRAY)
                )
            );
        } else if (data.obj instanceof ErrorFieldObject efo) {
            row.child(Components.label(Text.literal(" " + efo.exceptionClass())
                .styled(x -> x
                    .withColor(Formatting.RED)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(efo.fullExceptionText()))))));
        } else if (data.obj instanceof ComplexFieldObject cfo) {
            var subContainer = new SubObjectContainer(
                () -> requestPath(path),
                () -> { });
            data.subObjectContainer = subContainer;
            rowContainer.child(subContainer);

            row
                .child(
                    Components.label(
                        Text.literal(" " + cfo.text() + " ")
                            .formatted(Formatting.GRAY)
                    )
                )
                .child(subContainer.getSpinnyBoi()
                    .sizing(Sizing.fixed(10), Sizing.content()));
        }

        container.child(rowContainer);
    }

    protected void addFieldData(ObjectPath path, FieldData data) {
        ClientFieldData old = fields.get(path);
        VerticalFlowLayout container;

        if (path.steps().length == 1) {
            container = mainContainer;
        } else {
            container = fields.get(path.parent()).subObjectContainer;
        }

        if (old != null) {
            container.removeChild(old.containerComponent);
        }

        ClientFieldData newData = new ClientFieldData(data);

        makeComponent(container, path, newData);

        fields.put(path, newData);
    }
}
