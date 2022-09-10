package me.basiqueevangelist.gadget.client.dump;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PacketDumpReader {
    private PacketDumpReader() {

    }

    public static List<DumpedPacket> readAll(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        List<DumpedPacket> list = new ArrayList<>();
        PacketByteBuf buf = PacketByteBufs.create();

        try {
            // I know, IntelliJ.
            //noinspection InfiniteLoopStatement
            while (true) {
                int length = dis.readInt();

                buf.readerIndex(0);
                buf.writerIndex(0);

                buf.writeBytes(dis.readNBytes(length));

                list.add(DumpedPacket.read(buf));
            }
        } catch (EOFException e) {
            return list;
        }
    }
}
