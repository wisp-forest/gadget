package io.wispforest.gadget.client.resource;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.asm.GadgetMixinExtension;
import io.wispforest.gadget.client.gui.SubObjectContainer;
import io.wispforest.gadget.decompile.QuiltflowerHandler;
import io.wispforest.gadget.decompile.QuiltflowerManager;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ViewClassesScreen extends BaseOwoScreen<HorizontalFlowLayout> {
    private final Screen parent;
    private VerticalFlowLayout contents;
    private final QuiltflowerHandler decompiler = QuiltflowerManager.loadHandler();

    public ViewClassesScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    protected @NotNull OwoUIAdapter<HorizontalFlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::horizontalFlow);
    }

    @Override
    protected void build(HorizontalFlowLayout rootComponent) {
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .padding(Insets.of(5));

        VerticalFlowLayout tree = Containers.verticalFlow(Sizing.content(), Sizing.content());
        ScrollContainer<VerticalFlowLayout> treeScroll = Containers.verticalScroll(Sizing.fill(25), Sizing.fill(100), tree)
            .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF)));
        contents = Containers.verticalFlow(Sizing.content(), Sizing.content());
        ScrollContainer<VerticalFlowLayout> contentsScroll = Containers.verticalScroll(Sizing.fill(72), Sizing.fill(100), contents)
            .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF)));

        rootComponent
            .child(treeScroll
                .margins(Insets.right(3)))
            .child(contentsScroll);

        TreeEntry root = new TreeEntry("", tree);

        for (var name : GadgetMixinExtension.DUMPED_CLASSES) {
            String fullPath = name.replace('.', '/') + ".class";
            String[] split = fullPath.split("/");
            TreeEntry parent = root;

            for (int i = 0; i < split.length - 1; i++) {
                parent = parent.directory(split[i]);
            }

            parent.container.child(makeRecipeRow(split[split.length - 1], fullPath));
        }
    }

    private HorizontalFlowLayout makeRecipeRow(String name, String fullPath) {
        var row = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        var fileLabel = Components.label(Text.literal(name));

        row.child(fileLabel);
        row.mouseEnter().subscribe(
            () -> row.surface(Surface.flat(0x80ffffff)));

        row.mouseLeave().subscribe(
            () -> row.surface(Surface.BLANK));

        row.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

            UISounds.playInteractionSound();

            contents.clearChildren();

            try {
                var text = decompiler.decompileClass(Class.forName(fullPath.replace(".class", "").replace('/', '.')));
                var lines = text.lines().toList();
                int i = 0;
                int maxWidth = Integer.toString(lines.size() - 1).length();
                for (var line : lines) {
                    contents.child(Components.label(
                            Text.literal(" ")
                                .append(Text.literal(StringUtils.leftPad(Integer.toString(i), maxWidth) + " ")
                                    .formatted(Formatting.GRAY))
                                .append(Text.literal(line.replace("\t", "    "))
                                    .styled(x -> x.withFont(Gadget.id("monocraft")))))
                        .horizontalSizing(Sizing.fill(74)));

                    i++;
                }
            } catch (ClassNotFoundException e) {
                CharArrayWriter writer = new CharArrayWriter();
                e.printStackTrace(new PrintWriter(writer));
                String fullExceptionText = writer.toString();
                contents.child(Components.label(
                    Text.literal(fullExceptionText.replace("\t", "    "))
                        .styled(x -> x.withFont(Gadget.id("monocraft")))
                        .formatted(Formatting.RED)));
            }

            return true;
        });

        return row;
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    private static class TreeEntry {
        private final String name;
        private final List<TreeEntry> children = new ArrayList<>();
        private final VerticalFlowLayout container;

        private TreeEntry(String name, VerticalFlowLayout container) {
            this.name = name;
            this.container = container;
        }

        public TreeEntry directory(String name) {
            for (TreeEntry entry : children)
                if (entry.name.equals(name))
                    return entry;

            SubObjectContainer sub = new SubObjectContainer(unused -> {
            }, unused -> {
            });
            VerticalFlowLayout entryContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
            HorizontalFlowLayout row = Containers.horizontalFlow(Sizing.content(), Sizing.content());

            container
                .child(entryContainer
                    .child(row
                        .child(Components.label(Text.literal(name)))
                        .child(sub.getSpinnyBoi()
                            .margins(Insets.left(3))))
                    .child(sub));

            TreeEntry entry = new TreeEntry(name, sub);
            children.add(entry);
            return entry;
        }
    }
}
