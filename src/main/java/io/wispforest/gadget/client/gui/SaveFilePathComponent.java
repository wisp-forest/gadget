package io.wispforest.gadget.client.gui;

import io.wispforest.gadget.client.DialogUtil;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.util.Observable;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SaveFilePathComponent extends FlowLayout {
    private final String title;
    private final Observable<String> path;
    private final LabelComponent label;
    private List<String> patterns = new ArrayList<>();
    private String filterDescription = null;

    public SaveFilePathComponent(String title, String defaultPath) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
        this.title = title;
        this.path = Observable.of(defaultPath);

        this.label = Components.label(Text.literal("breh"));

        this.configureLabel(defaultPath);
        path.observe(this::configureLabel);

        child(label);

        padding(Insets.of(2));
        surface(Surface.flat(0x55555555));

        mouseEnter().subscribe(() -> surface(Surface.flat(0xaaaaaaaa)));
        mouseLeave().subscribe(() -> surface(Surface.flat(0x55555555)));

        cursorStyle(CursorStyle.HAND);
        label.cursorStyle(CursorStyle.HAND);
    }

    private void configureLabel(String newPath) {
        int neededSlash = newPath.lastIndexOf('/', 40);

        if (neededSlash != -1) {
            newPath = "..." + newPath.substring(neededSlash);
        }

        this.label.text(Text.literal(newPath));
    }

    public List<String> patterns() {
        return patterns;
    }

    public SaveFilePathComponent patterns(List<String> patterns) {
        this.patterns = patterns;
        return this;
    }

    public SaveFilePathComponent pattern(String pattern) {
        patterns.add(pattern);
        return this;
    }

    public String filterDescription() {
        return filterDescription;
    }

    public SaveFilePathComponent filterDescription(String description) {
        this.filterDescription = description;
        return this;
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Thread thread = new Thread(() -> {
                String selected = DialogUtil.saveFileDialog(
                    title,
                    path.get(),
                    patterns.isEmpty() ? null : patterns,
                    filterDescription
                );

                if (selected != null) {
                    this.path.set(selected);
                }
            }, "Save File Dialog Thread for " + this);

            thread.start();

            return true;
        }

        return super.onMouseDown(mouseX, mouseY, button);
    }

    public Observable<String> path() {
        return path;
    }
}
