package io.wispforest.gadget.desc;

public record ComplexFieldObject(String text) implements FieldObject {
    @Override
    public String type() {
        return "complex";
    }

    @Override
    public int color() {
        return 0x0000FF;
    }
}
