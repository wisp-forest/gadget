package io.wispforest.gadget.desc;

import io.wispforest.owo.network.serialization.SealedPolymorphic;

@SealedPolymorphic
public sealed interface FieldObject permits BytesFieldObject, ComplexFieldObject, ErrorFieldObject, NbtCompoundFieldObject, PrimitiveFieldObject {
    String type();

    int color();
}
