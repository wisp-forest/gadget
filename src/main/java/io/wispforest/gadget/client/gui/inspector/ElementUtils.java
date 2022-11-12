package io.wispforest.gadget.client.gui.inspector;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ElementUtils {
    private static final List<Pair<Class<?>, ElementSupport<?>>> ELEMENT_SUPPORTS = new ArrayList<>();
    private static final List<BiConsumer<Screen, List<ParentElement>>> ROOT_LISTERS = new ArrayList<>();

    static {
        VanillaSupport.init();
        if (FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
            REISupport.init();
        }
    }

    @SuppressWarnings("unchecked")
    private static int getThroughSupport(Element element, ElementSupportGetter getter) {
        for (var pair : ELEMENT_SUPPORTS) {
            if (pair.getLeft().isInstance(element)) {
                ElementSupport<Element> support = (ElementSupport<Element>) pair.getRight();
                int val = getter.get(support, element);

                if (val != -1)
                    return val;
            }
        }

        return -1;
    }

    public static <T extends Element> void registerElementSupport(Class<T> klass, ElementSupport<T> support) {
        ELEMENT_SUPPORTS.add(new Pair<>(klass, support));
    }

    public static void registerRootLister(BiConsumer<Screen, List<ParentElement>> rootLister) {
        ROOT_LISTERS.add(rootLister);
    }

    public static List<ParentElement> listRootElements(Screen screen) {
        List<ParentElement> parents = new ArrayList<>();

        for (var rootLister : ROOT_LISTERS) {
            rootLister.accept(screen, parents);
        }

        return parents;
    }

    public static int x(Element element) {
        return getThroughSupport(element, ElementSupport::getX);
    }

    public static int y(Element element) {
        return getThroughSupport(element, ElementSupport::getY);
    }

    public static int width(Element element) {
        return getThroughSupport(element, ElementSupport::getWidth);
    }

    public static int height(Element element) {
        return getThroughSupport(element, ElementSupport::getHeight);
    }

    public static boolean isVisible(Element element) {
        if (element instanceof ClickableWidget widget)
            return widget.visible;
        else
            return true;
    }

    public static boolean inBoundingBox(Element e, int x, int y) {
        if (x(e) == -1) return false;

        return x >= x(e)
            && y >= y(e)
            && x < (x(e) + width(e))
            && y < (y(e) + height(e));
    }

    public static void collectChildren(ParentElement root, List<Element> children) {
        for (var child : root.children()) {
            if (child instanceof ParentElement parent)
                collectChildren(parent, children);

            children.add(child);
        }
    }

    private interface ElementSupportGetter {
        <T extends Element> int get(ElementSupport<T> support, T element);
    }
}
