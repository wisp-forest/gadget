package me.basiqueevangelist.gadget.desc;

import io.wispforest.owo.network.serialization.SealedPolymorphic;

@SealedPolymorphic
public sealed interface FieldObject permits ComplexFieldObject, ErrorFieldObject, SimpleFieldObject {
    String type();

    int color();
}
