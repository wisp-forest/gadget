package me.basiqueevangelist.gadget.desc;

public record SimpleFieldObject(String contents) implements FieldObject {
    @Override
    public String type() {
        return "simple";
    }

    @Override
    public int color() {
        return 0xFFFF00;
    }
}
