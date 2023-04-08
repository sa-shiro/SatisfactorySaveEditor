package net.sashiro.satisfactory.savefile;

import net.sashiro.satisfactory.zlib.ZLibCompressor;

import java.io.*;

public class CompressedSaveFileBody {

    private long uePackageSignature;
    private long maxChunkSize;
    private long compressedSize_1;
    private long uncompressedSize_1;
    private long compressedSize_2;
    private long uncompressedSize_2;
    private byte[] compressedBytes;


    public CompressedSaveFileBody(byte[] data) {
        try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data))) {
            this.uePackageSignature = getUnsignedLong(stream);
            stream.readInt(); // padding
            this.maxChunkSize = getUnsignedLong(stream);
            stream.readInt(); // padding
            this.compressedSize_1 = getUnsignedLong(stream);
            stream.readInt(); // padding
            this.uncompressedSize_1 = getUnsignedLong(stream);
            stream.readInt(); // padding
            this.compressedSize_2 = getUnsignedLong(stream);
            stream.readInt(); // padding
            this.uncompressedSize_2 = getUnsignedLong(stream);
            stream.readInt(); // padding
            this.compressedBytes = stream.readNBytes((int) compressedSize_1);

            ZLibCompressor compressor = new ZLibCompressor();

            try (DataOutputStream decoderStream = new DataOutputStream(new FileOutputStream("D:\\04-Projects\\Java\\SatisfactorySaveEditor\\decoded_body"))) {
                decoderStream.write(compressor.decompress(compressedBytes));
            } catch (IOException e) {

            }

            // for debugging purposes
            System.out.println("---\n" + uePackageSignature + "\n" + maxChunkSize + "\n" + compressedSize_1 + "\n" + uncompressedSize_1 + "\n" + compressedSize_2 + "\n" + uncompressedSize_2 + "\n" + compressedBytes + "\n" + compressor.decompressToString(compressedBytes) + "---");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static long getUnsignedLong(DataInputStream stream) throws IOException {
        return Integer.toUnsignedLong((Integer.reverseBytes(stream.readInt())));
    }
}