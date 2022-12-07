package io.wispforest.gadget.client.field;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.gui.ComponentAdditionRound;
import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.client.gui.SubObjectContainer;
import io.wispforest.gadget.client.gui.search.SearchAnchorComponent;
import io.wispforest.gadget.client.nbt.KeyAdderWidget;
import io.wispforest.gadget.client.nbt.NbtDataIsland;
import io.wispforest.gadget.client.nbt.NbtPath;
import io.wispforest.gadget.desc.*;
import io.wispforest.gadget.desc.edit.PrimitiveEditData;
import io.wispforest.gadget.path.FieldPathStep;
import io.wispforest.gadget.util.WeakObservableDispatcher;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.path.ObjectPath;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FieldDataIsland {
    private static final WeakObservableDispatcher<List<String>> HIDDEN_FIELDS = new WeakObservableDispatcher<>();

    static {
        Gadget.CONFIG.subscribeToHiddenFields(HIDDEN_FIELDS::handle);
    }

    protected final Map<ObjectPath, ClientFieldData> fields = new TreeMap<>();
    private final VerticalFlowLayout mainContainer;
    private Consumer<ObjectPath> pathRequester = path -> {};
    private boolean shortenNames = false;
    private ComponentAdditionRound currentRound = null;
    private boolean generateSearchAnchors;
    BiConsumer<ObjectPath, PrimitiveEditData> primitiveSetter = null;
    BiConsumer<ObjectPath, NbtCompound> nbtCompoundSetter = null;

    public FieldDataIsland() {
        this.mainContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
    }

    public void pathRequester(Consumer<ObjectPath> pathRequester) {
        this.pathRequester = pathRequester;
    }

    public void primitiveSetter(BiConsumer<ObjectPath, PrimitiveEditData> primitiveSetter) {
        this.primitiveSetter = primitiveSetter;
    }

    public void nbtCompoundSetter(BiConsumer<ObjectPath, NbtCompound> nbtCompoundSetter) {
        this.nbtCompoundSetter = nbtCompoundSetter;
    }

    public void generateSearchAnchors(boolean generateSearchAnchors) {
        this.generateSearchAnchors = generateSearchAnchors;
    }

    public void targetObject(Object obj, boolean settable) {
        this.pathRequester = (path) -> {
            Object sub = path.follow(obj);

            FieldObjects.collectAllData(path, sub)
                .forEach(this::addFieldData);
            commitAdditions();
        };

        if (settable) {
            this.primitiveSetter = (path, data) -> {
                path.set(obj, data.toObject());

                var parentPath = path.parent();

                FieldObjects.collectAllData(parentPath, parentPath.follow(obj))
                    .forEach(this::addFieldData);
                commitAdditions();
            };

            this.nbtCompoundSetter = (path, data) -> {
                path.set(obj, data);

                var parentPath = path.parent();

                FieldObjects.collectAllData(parentPath, parentPath.follow(obj))
                    .forEach(this::addFieldData);
                commitAdditions();
            };
        }

        FieldObjects.collectAllData(ObjectPath.EMPTY, obj)
            .forEach(this::addFieldData);
        commitAdditions();
    }

    public void shortenNames() {
        this.shortenNames = true;
    }

    public VerticalFlowLayout mainContainer() {
        return mainContainer;
    }

    private void makeComponent(VerticalFlowLayout container, ObjectPath path, ClientFieldData data, ComponentAdditionRound round) {
        var rowContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        var row = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        data.containerComponent = rowContainer;

        rowContainer.child(row);

        if (generateSearchAnchors) {
            SearchAnchorComponent anchor = new SearchAnchorComponent(row, path::name);
            row.child(anchor);
        }

        var nameText = Text.literal(path.name());

        if (data.isMixin)
            nameText.formatted(Formatting.GRAY)
                .styled(x -> x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    Text.literal("Mixin-injected field")
                        .formatted(Formatting.YELLOW))));

        MutableText rowText = Text.literal("")
            .append(Text.literal(data.obj.type().charAt(0) + " ")
                .styled(x -> x.withColor(data.obj.color())))
            .append(nameText);
        var rowLabel = Components.label(rowText);

        row.child(rowLabel);

        if (data.obj instanceof PrimitiveFieldObject pfo) {
            if (!data.isFinal && primitiveSetter != null && pfo.editData().isPresent()) {
                rowText.append(Text.literal(" = ")
                    .formatted(Formatting.GRAY));
                row.child(new PrimitiveFieldWidget(this, path, pfo));
            } else {
                rowText.append(Text.literal(" = " + pfo.contents())
                    .formatted(Formatting.GRAY));
            }
        } else if (data.obj instanceof ErrorFieldObject efo) {
            rowText.append(Text.literal(" " + efo.exceptionClass())
                .styled(x -> x
                    .withColor(Formatting.RED)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(efo.fullExceptionText())))));
        } else if (data.obj instanceof ComplexFieldObject cfo) {
            var subContainer = new SubObjectContainer(
                unused -> pathRequester.accept(path),
                SubObjectContainer::clearChildren);
            data.subObjectContainer = subContainer;
            rowContainer.child(subContainer);

            String text = cfo.text();

            if (shortenNames)
                text = text.substring(text.lastIndexOf('.') + 1);

            rowText.append(
                Text.literal(" " + text + " ")
                    .formatted(Formatting.GRAY)
            );

            row
                .child(subContainer.getSpinnyBoi()
                    .sizing(Sizing.fixed(10), Sizing.content()));
        } else if (data.obj instanceof NbtCompoundFieldObject nfo) {
            var subContainer = new SubObjectContainer(
                unused -> { },
                unused -> { });
            data.subObjectContainer = subContainer;
            rowContainer.child(subContainer);

            Consumer<NbtCompound> reloader = null;

            if (nbtCompoundSetter != null)
                reloader = newData -> nbtCompoundSetter.accept(path, newData);

            var island = new NbtDataIsland(nfo.data(), reloader);

            subContainer.child(island);

            row
                .child(subContainer.getSpinnyBoi()
                    .sizing(Sizing.fixed(10), Sizing.content()));

            if (nbtCompoundSetter != null) {
                var plusLabel = Components.label(Text.of("+"));

                GuiUtil.semiButton(plusLabel, (mouseX, mouseY) ->
                    island.typeSelector(
                        (int) (plusLabel.x() + mouseX),
                        (int) (plusLabel.y() + mouseY),
                        type -> data.subObjectContainer.child(new KeyAdderWidget(island, NbtPath.EMPTY, type, unused -> true)))
                );

                row.child(plusLabel);
            }
        }

        if (path.last() instanceof FieldPathStep step) {
            rowLabel.mouseDown().subscribe((mouseX, mouseY, button) -> {
                if (button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return false;

                GuiUtil.contextMenu(rowLabel, mouseX, mouseY)
                    .button(Text.translatable("text.gadget.hide_field"), unused -> {
                        container.removeChild(rowContainer);
                        ArrayList<String> hiddenFields = new ArrayList<>(Gadget.CONFIG.hiddenFields());
                        hiddenFields.add(step.fieldId());
                        Gadget.CONFIG.hiddenFields(hiddenFields);
                    });

                return true;
            });

            HIDDEN_FIELDS.register(value -> {
                if (value.contains(step.fieldId())) {
                    container.removeChild(rowContainer);
                    return true;
                }

                return false;
            });
        }

        row
            .margins(Insets.both(0, 2))
            .allowOverflow(true);

        round.addTo(container, rowContainer);
    }

    public void addFieldData(ObjectPath path, FieldData data) {
        if (path.last() instanceof FieldPathStep step
         && Gadget.CONFIG.hiddenFields().contains(step.fieldId())) {
            return;
        }

        if (mainContainer == null) {
            fields.put(path, new ClientFieldData(data));
            return;
        }

        if (currentRound == null)
            currentRound = new ComponentAdditionRound();

        ClientFieldData old = fields.get(path);
        VerticalFlowLayout container;

        if (path.steps().length == 1) {
            container = mainContainer;
        } else {
            container = fields.get(path.parent()).subObjectContainer;
        }

        if (old != null) {
            currentRound.removeFrom(container, old.containerComponent);
        }

        ClientFieldData newData = new ClientFieldData(data);

        makeComponent(container, path, newData, currentRound);

        fields.put(path, newData);
    }

    public void commitAdditions() {
        if (currentRound == null) return;

        currentRound.commit();
        currentRound = null;
    }
}
