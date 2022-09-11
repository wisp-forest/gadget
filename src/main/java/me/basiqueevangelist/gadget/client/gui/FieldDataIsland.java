package me.basiqueevangelist.gadget.client.gui;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import me.basiqueevangelist.gadget.desc.ComplexFieldObject;
import me.basiqueevangelist.gadget.desc.ErrorFieldObject;
import me.basiqueevangelist.gadget.desc.FieldObjects;
import me.basiqueevangelist.gadget.desc.PrimitiveFieldObject;
import me.basiqueevangelist.gadget.desc.edit.PrimitiveEditData;
import me.basiqueevangelist.gadget.network.FieldData;
import me.basiqueevangelist.gadget.path.ObjectPath;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FieldDataIsland {
    protected final Map<ObjectPath, ClientFieldData> fields = new TreeMap<>();
    private final VerticalFlowLayout mainContainer;
    private Consumer<ObjectPath> pathRequester = path -> {};
    BiConsumer<ObjectPath, PrimitiveEditData> primitiveSetter = null;

    public FieldDataIsland() {
        this.mainContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
    }

    public void pathRequester(Consumer<ObjectPath> pathRequester) {
        this.pathRequester = pathRequester;
    }

    public void primitiveSetter(BiConsumer<ObjectPath, PrimitiveEditData> primitiveSetter) {
        this.primitiveSetter = primitiveSetter;
    }

    public void targetObject(Object obj, boolean settable) {
        this.pathRequester = (path) -> {
            Object sub = path.follow(obj);

            FieldObjects.collectAllData(path, sub)
                .forEach(this::addFieldData);
        };

        if (settable)
            this.primitiveSetter = (path, data) -> {
                path.set(obj, data.toObject());

                var parentPath = path.parent();

                FieldObjects.collectAllData(parentPath, parentPath.follow(obj))
                    .forEach(this::addFieldData);
            };

        FieldObjects.collectAllData(ObjectPath.EMPTY, obj)
            .forEach(this::addFieldData);
    }

    public VerticalFlowLayout mainContainer() {
        return mainContainer;
    }

    private void makeComponent(VerticalFlowLayout container, ObjectPath path, ClientFieldData data) {
        var rowContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        var row = Containers.horizontalFlow(Sizing.content(), Sizing.content());

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

        if (data.obj instanceof PrimitiveFieldObject sfo) {
            row.child(new PrimitiveFieldWidget(this, path, data.isFinal, sfo));
        } else if (data.obj instanceof ErrorFieldObject efo) {
            row.child(Components.label(Text.literal(" " + efo.exceptionClass())
                .styled(x -> x
                    .withColor(Formatting.RED)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(efo.fullExceptionText()))))));
        } else if (data.obj instanceof ComplexFieldObject cfo) {
            var subContainer = new SubObjectContainer(
                () -> pathRequester.accept(path),
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

    public void addFieldData(ObjectPath path, FieldData data) {
        if (mainContainer == null) {
            fields.put(path, new ClientFieldData(data));
            return;
        }

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
