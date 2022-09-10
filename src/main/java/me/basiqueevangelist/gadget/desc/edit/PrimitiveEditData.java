package me.basiqueevangelist.gadget.desc.edit;

public record PrimitiveEditData(PrimitiveEditType<?> type, String data) {
    @SuppressWarnings("unchecked")
    public static PrimitiveEditData forObject(Object o) {
        if (o == null)
            return null;

        var type = (PrimitiveEditType<Object>) PrimitiveEditTypes.getFor(o.getClass());

        if (type != null)
            return new PrimitiveEditData(type, type.toPacket(o));
        else
            return null;
    }

    public Object toObject() {
        if (data.equals("null")) {
            return null;
        }

        return type.fromPacket(data);
    }
}
