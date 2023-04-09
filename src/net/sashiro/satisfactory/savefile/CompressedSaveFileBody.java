package net.sashiro.satisfactory.savefile;

import net.sashiro.satisfactory.zlib.ZLibCompressor;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class CompressedSaveFileBody {

    private long uePackageSignature;
    private long maxChunkSize;
    private long compressedSize_1;
    private long uncompressedSize_1;
    private long compressedSize_2;
    private long uncompressedSize_2;
    private byte[] compressedBytes;

    private int objectCount = 0;
    private final ArrayList<CompressedSaveFileBody> chunks = new ArrayList<>();
    ArrayList<byte[]> compressedData = new ArrayList<>();

    public CompressedSaveFileBody(long uePackageSignature, long maxChunkSize, long compressedSize, long uncompressedSize, byte[] compressedBytes) {
        this.uePackageSignature = uePackageSignature;
        this.maxChunkSize = maxChunkSize;
        this.compressedSize_1 = compressedSize;
        this.compressedSize_2 = compressedSize;
        this.uncompressedSize_1 = uncompressedSize;
        this.uncompressedSize_2 = uncompressedSize;
        this.compressedBytes = compressedBytes;
    }

    public CompressedSaveFileBody read(byte[] data) {
        try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data))) {
            // --- HEADER ---
            this.uePackageSignature = getUnsignedInt(stream);
            if (uePackageSignature != Long.parseLong("2653586369")) throw new RuntimeException(String.format("Invalid Package Signature. Expected %s but got %d", "2653586369", uePackageSignature));
            stream.skipNBytes(4); // padding
            this.maxChunkSize = getUnsignedInt(stream);
            stream.skipNBytes(4); // padding
            this.compressedSize_1 = getUnsignedInt(stream);
            stream.skipNBytes(4); // padding
            this.uncompressedSize_1 = getUnsignedInt(stream);
            stream.skipNBytes(4); // padding
            this.compressedSize_2 = getUnsignedInt(stream);
            stream.skipNBytes(4); // padding
            this.uncompressedSize_2 = getUnsignedInt(stream);
            stream.skipNBytes(4); // padding
            if (compressedSize_1 != compressedSize_2) throw new RuntimeException(String.format("This file might be corrupted. Expected a stream length of %d bytes but got %d bytes", compressedSize_1, compressedSize_2));
            if (uncompressedSize_1 != uncompressedSize_2) throw new RuntimeException(String.format("This file might be corrupted. Expected a stream length of %d bytes but got %d bytes", uncompressedSize_1, uncompressedSize_2));
            // --- BODY ---
            this.compressedBytes = stream.readNBytes((int) compressedSize_1);
            stream.reset();

            DataOutputStream outStream = new DataOutputStream(new FileOutputStream(".\\chunks\\chunk_" + objectCount));
            outStream.write(stream.readAllBytes());

            return new CompressedSaveFileBody(uePackageSignature, maxChunkSize, compressedSize_1, uncompressedSize_1, compressedBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createChunks(byte[] data) {
        try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data))) {
            int available = stream.available();
            int dataLength = 0;
            byte[] bytes;

            // --- CHUNK HEADER ---
            stream.mark(16384);
            stream.skipNBytes(16);
            dataLength = Integer.reverseBytes(stream.readInt());
            stream.skipNBytes(28);
            available = stream.available();

            // -- COMPRESSED CHUNK BODY ---
            bytes = new byte[dataLength];
            stream.readNBytes(bytes, 0, dataLength);

            // --- SAVE BODY CONTENT FOR DECODING ---
            compressedData.add(bytes);
            stream.reset();

            // --- CHUNK ( HEADER + COMPRESSED BODY ) ---
            dataLength += 48;
            bytes = new byte[dataLength];
            stream.readNBytes(bytes, 0, dataLength);
            chunks.add(this.read(bytes));
            objectCount++;

            if (stream.available() > 0) createChunks(stream.readAllBytes());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public CompressedSaveFileBody(byte[] data) throws IOException {
        createChunks(data);
        System.out.println("Chunk count: " + chunks.size());
        ZLibCompressor compressor = new ZLibCompressor();
        DataOutputStream outStream = new DataOutputStream(new FileOutputStream(".\\decoded"));
        for (byte[] b : compressedData) {
            outStream.write(compressor.decompress(b));
        }
    }

    private static long getUnsignedInt(DataInputStream stream) throws IOException {
        return Integer.toUnsignedLong((Integer.reverseBytes(stream.readInt())));
    }
}