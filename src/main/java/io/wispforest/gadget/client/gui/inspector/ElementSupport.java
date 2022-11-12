package io.wispforest.gadget.client.gui.inspector;

import net.minecraft.client.gui.Element;

import java.util.function.ToIntFunction;

public interface ElementSupport<T extends Element> {
    static <T extends Element> ElementSupport<T> fromLambda(ToIntFunction<T> getX,
                                                            ToIntFunction<T> getY,
                                                            ToIntFunction<T> getWidth,
                                                            ToIntFunction<T> getHeight) {
        return new ElementSupport<>() {
            @Override
            public int getX(T element) {
                return getX.applyAsInt(element);
            }

            @Override
            public int getY(T element) {
                return getY.applyAsInt(element);
            }

            @Override
            public int getWidth(T element) {
                return getWidth.applyAsInt(element);
            }

            @Override
            public int getHeight(T element) {
                return getHeight.applyAsInt(element);
            }
        };
    }

    int getX(T element);

    int getY(T element);

    int getWidth(T element);

    int getHeight(T element);
}
