//package io.wispforest.gadget.client.config;
//
//import io.wispforest.gadget.client.gui.GuiUtil;
//import io.wispforest.owo.config.ui.component.OptionComponent;
//import io.wispforest.owo.ui.component.Components;
//import io.wispforest.owo.ui.component.DropdownComponent;
//import io.wispforest.owo.ui.component.LabelComponent;
//import io.wispforest.owo.ui.container.FlowLayout;
//import io.wispforest.owo.ui.container.HorizontalFlowLayout;
//import io.wispforest.owo.ui.core.*;
//import net.minecraft.text.Text;
//import org.lwjgl.glfw.GLFW;
//
//import java.util.List;
//import java.util.function.Function;
//
//public class DropdownSelectorWidget<T> extends HorizontalFlowLayout implements OptionComponent {
//    private T value;
//    private Function<T, Text> textifier;
//    private final LabelComponent currentLabel;
//    private DropdownComponent dropdown;
//    private final List<T> possibleValues;
//    private boolean editable = true;
//
//    public DropdownSelectorWidget(List<T> possibleValues, Function<T, Text> textifier) {
//        super(Sizing.fixed(0), Sizing.fixed(10));
//        this.possibleValues = possibleValues;
//        this.value = possibleValues.get(0);
//        this.textifier = textifier;
//        surface(Surface.outline(0xffa0a0a0));
//        verticalAlignment(VerticalAlignment.CENTER);
//        padding(Insets.of(2));
//
//        this.currentLabel = Components.label(textifier.apply(value));
//        child(currentLabel);
//
//        reload();
//    }
//
//    public void editable(boolean editable) {
//        this.editable = editable;
//
//        reload();
//    }
//
//    private void reload() {
//        currentLabel.text(Text.literal("")
//            .styled(x -> x.withColor(editable ? 0xe0e0e0 : 0x707070))
//            .append(textifier.apply(value)));
//    }
//
//    @Override
//    public boolean onMouseDown(double mouseX, double mouseY, int button) {
//        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && dropdown == null && editable) {
//            var root = (FlowLayout) GuiUtil.root(this);
//
//            dropdown = Components.dropdown(Sizing.fixed(width));
//
//            dropdown.positioning(Positioning.absolute((int) mouseX, (int) mouseY));
//
//            for (T possible : possibleValues) {
//                dropdown.button(textifier.apply(possible), unused -> {
//                    value = possible;
//                    reload();
//                });
//            }
//
//            root.child(dropdown);
//            dropdown.focusLost().subscribe(() -> dropdown.queue(() -> root.removeChild(dropdown)));
//
//            return true;
//        }
//
//        return super.onMouseDown(mouseX, mouseY, button);
//    }
//
//    @Override
//    public boolean isValid() {
//        return true;
//    }
//
//    @Override
//    public T parsedValue() {
//        return value;
//    }
//}
