package io.wispforest.gadget.path;

import io.wispforest.owo.network.serialization.SealedPolymorphic;

@SealedPolymorphic
public sealed interface PathStep permits FieldPathStep, IndexPathStep, MapPathStep {
    Object follow(Object o);

    void set(Object o, Object to);
}
