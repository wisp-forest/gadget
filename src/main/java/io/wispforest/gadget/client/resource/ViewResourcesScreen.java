package io.wispforest.gadget.client.resource;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.client.gui.SubObjectContainer;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ViewResourcesScreen extends BaseOwoScreen<HorizontalFlowLayout> {
    private static final Identifier FILE_TEXTURE_ID = Gadget.id("file_texture");

    private final Screen parent;
    private final ResourceManager manager;
    private NativeImageBackedTexture prevTexture;
    private VerticalFlowLayout contents;

    public ViewResourcesScreen(Screen parent, ResourceManager manager) {
        this.parent = parent;
        this.manager = manager;
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
        ScrollContainer<VerticalFlowLayout> contentsScroll = Containers.verticalScroll(Sizing.content(), Sizing.fill(100), contents)
            .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF)));
        treeScroll.surface(Surface.outline(11184810));

        rootComponent
            .child(treeScroll
                .margins(Insets.right(3)))
            .child(contentsScroll);

        var resources = manager.findAllResources("", x -> true);
        TreeEntry root = new TreeEntry("", tree);

        for (var pair : resources.entrySet()) {
            String fullPath = pair.getKey().getNamespace() + "/" + pair.getKey().getPath();
            String[] split = fullPath.split("/");
            TreeEntry parent = root;

            for (int i = 0; i < split.length - 1; i++) {
                parent = parent.directory(split[i]);
            }

            if (pair.getValue().size() > 1) {
                SubObjectContainer sub = new SubObjectContainer(unused -> {}, unused -> {});
                VerticalFlowLayout entryContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
                HorizontalFlowLayout row = Containers.horizontalFlow(Sizing.content(), Sizing.content());

                parent.container
                    .child(entryContainer
                        .child(row
                            .child(Components.label(Text.literal(split[split.length - 1])))
                            .child(sub.getSpinnyBoi()
                                .margins(Insets.left(3))))
                        .child(sub));

                int i = 0;
                for (var resource : pair.getValue()) {
                    sub.child(makeRecipeRow(String.valueOf(i), pair.getKey(), resource::getInputStream));
                    i++;
                }

            } else {
                parent.container.child(makeRecipeRow(split[split.length - 1], pair.getKey(), pair.getValue().get(0)::getInputStream));
            }
        }
    }

    private HorizontalFlowLayout makeRecipeRow(String name, Identifier key, Callable<InputStream> isGetter) {
        var row = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        var fileLabel = Components.label(Text.literal(name));

        row.child(fileLabel);
        row.mouseEnter().subscribe(
            () -> row.surface(Surface.flat(0x80ffffff)));

        row.mouseLeave().subscribe(
            () -> row.surface(Surface.BLANK));

        row.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

            openFile(key, isGetter);

            return true;
        });

        return row;
    }
    private void openFile(Identifier id, Callable<InputStream> isGetter) {
        if (prevTexture != null) {
            prevTexture.close();
            prevTexture = null;
        }

        try {
            var is = isGetter.call();
            contents.clearChildren();

            if (id.getPath().endsWith(".png")) {
                prevTexture = new NativeImageBackedTexture(NativeImage.read(is));
                client.getTextureManager().registerTexture(FILE_TEXTURE_ID, prevTexture);

                contents
                    .child(Components.texture(
                        FILE_TEXTURE_ID,
                        0,
                        0,
                        prevTexture.getImage().getWidth(),
                        prevTexture.getImage().getHeight(),
                        prevTexture.getImage().getWidth(),
                        prevTexture.getImage().getHeight()))
                    .child(Components.label(
                        Text.translatable(
                            "text.gadget.image_size",
                            prevTexture.getImage().getWidth(),
                            prevTexture.getImage().getHeight(),
                            "PNG"
                        )));

                return;
            }

            is = new BufferedInputStream(is);

            boolean isText = id.getPath().endsWith(".txt")
                || id.getPath().endsWith(".json")
                || id.getPath().endsWith(".fsh")
                || id.getPath().endsWith(".vsh");

            if (isText) {
                var reader = new InputStreamReader(is);
                var lines = IOUtils.readLines(reader);
                int i = 0;
                int maxWidth = Integer.toString(lines.size() - 1).length();
                for (var line : lines) {
                    contents.child(Components.label(
                            Text.literal(" ")
                                .append(Text.literal(StringUtils.leftPad(Integer.toString(i), maxWidth) + " ")
                                    .formatted(Formatting.GRAY))
                                .append(Text.literal(line.replace("\t", "    "))
                                    .styled(x -> x.withFont(Gadget.id("monocraft")))))
                        .horizontalSizing(Sizing.fill(75)));

                    i++;
                }
                return;
            }

            // Display as bytes.
            contents.child(GuiUtil.hexDump(is.readAllBytes()));
        } catch (Exception e) {
            CharArrayWriter writer = new CharArrayWriter();
            e.printStackTrace(new PrintWriter(writer));
            String fullExceptionText = writer.toString();
            contents.child(Components.label(
                Text.literal(fullExceptionText.replace("\t", "    "))
                    .styled(x -> x.withFont(Gadget.id("monocraft")))
                    .formatted(Formatting.RED)));
        }
    }

    @Override
    public void close() {
        client.setScreen(parent);
        if (prevTexture != null) {
            prevTexture.close();
        }
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

            SubObjectContainer sub = new SubObjectContainer(unused -> {}, unused -> {});
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
