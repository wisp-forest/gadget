package io.wispforest.gadget.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;

// get it? it calls .slice() on passed in bufs?
public class SlicingPacketByteBuf extends PacketByteBuf {
    public SlicingPacketByteBuf(ByteBuf parent) {
        super(parent);
    }

    @Override
    public PacketByteBuf writeBytes(ByteBuf byteBuf) {
        return super.writeBytes(byteBuf.slice());
    }

    @Override
    public PacketByteBuf writeBytes(ByteBuf byteBuf, int i) {
        return super.writeBytes(byteBuf.slice(), i);
    }

    @Override
    public PacketByteBuf setBytes(int i, ByteBuf byteBuf) {
        return super.setBytes(i, byteBuf.slice());
    }

    @Override
    public PacketByteBuf setBytes(int i, ByteBuf byteBuf, int j) {
        return super.setBytes(i, byteBuf.slice(), j);
    }
}
