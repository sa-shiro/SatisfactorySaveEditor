package net.sashiro.satisfactory.savefile;

import java.io.*;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

public class SaveFileHeader {
    private final int headerVersion;
    private final int saveVersion;
    private final int builtVersion;
    private final String mapName;
    private String mapOptions;
    private String sessionName;
    private final int playedSeconds;
    private final long saveTimestamp;
    private byte sessionVisibility;
    private final int editorObjectVersion;
    private final String modMetadata;
    private final int modFlags;
    private final String saveIdentifier;
    public byte[] body;

    public SaveFileHeader(File file) {
        try (DataInputStream stream = new DataInputStream(new FileInputStream(file))) {
            this.headerVersion = Integer.reverseBytes(stream.readInt());
            this.saveVersion = Integer.reverseBytes(stream.readInt());
            this.builtVersion =  Integer.reverseBytes(stream.readInt());
            this.mapName = convertLEStringToBE(stream);
            this.mapOptions =  convertLEStringToBE(stream);
            this.sessionName =  convertLEStringToBE(stream);
            this.playedSeconds = Integer.reverseBytes(stream.readInt());
            this.saveTimestamp = Long.reverseBytes(stream.readLong());
            this.sessionVisibility = stream.readByte();
            this.editorObjectVersion = Integer.reverseBytes(stream.readInt());
            this.modMetadata =  convertLEStringToBE(stream);
            this.modFlags = Integer.reverseBytes(stream.readInt());
            this.saveIdentifier =  convertLEStringToBE(stream);
            this.body = stream.readAllBytes();
            CompressedSaveFileBody b = new CompressedSaveFileBody(body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected SaveFileHeader(int headerVersion, int saveVersion, int builtVersion, String mapName, String mapOptions, String sessionName, int playedSeconds, long saveTimestamp, byte sessionVisibility, int editorObjectVersion, String modMetadata, int modFlags, String saveIdentifier) {
        this.headerVersion = headerVersion;
        this.saveVersion = saveVersion;
        this.builtVersion = builtVersion;
        this.mapName = mapName;
        this.mapOptions = mapOptions;
        this.sessionName = sessionName;
        this.playedSeconds = playedSeconds;
        this.saveTimestamp = saveTimestamp;
        this.sessionVisibility = sessionVisibility;
        this.editorObjectVersion = editorObjectVersion;
        this.modMetadata = modMetadata;
        this.modFlags = modFlags;
        this.saveIdentifier = saveIdentifier;
    }

    public SaveFileHeader read(File file) {
        return new SaveFileHeader(file);
    }

    public void save(File file) {
        try (DataOutputStream stream = new DataOutputStream(new FileOutputStream(file))) {
            stream.writeInt(Integer.reverseBytes(this.headerVersion));
            stream.writeInt(Integer.reverseBytes(this.saveVersion));
            stream.writeInt(Integer.reverseBytes(this.builtVersion));
            stream.writeInt(Integer.reverseBytes(mapName.length()));
            stream.write(mapName.getBytes());
            stream.writeInt(Integer.reverseBytes(mapOptions.length()));
            stream.write(mapOptions.getBytes());
            stream.writeInt(Integer.reverseBytes(sessionName.length()));
            stream.write((sessionName).getBytes());
            stream.writeInt(Integer.reverseBytes(this.playedSeconds));
            stream.writeLong(Long.reverseBytes(this.saveTimestamp));
            stream.writeByte(this.sessionVisibility);
            stream.writeInt(Integer.reverseBytes(this.editorObjectVersion));
            stream.writeInt(Integer.reverseBytes(modMetadata.length()));
            stream.write((modMetadata).getBytes());
            stream.writeInt(Integer.reverseBytes(this.modFlags));
            stream.writeInt(Integer.reverseBytes(saveIdentifier.length()));
            stream.write((saveIdentifier).getBytes());
            //stream.write(this.body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final int getHeaderVersion() {
        return headerVersion;
    }

    public final int getSaveVersion() {
        return saveVersion;
    }

    public final int getBuiltVersion() {
        return builtVersion;
    }

    public final String getMapName() {
        return mapName;
    }

    public String getMapOptions() {
        return mapOptions;
    }

    public void setMapOptions(String mapOptions) {
        if (!mapOptions.equals("")) this.mapOptions = mapOptions  + "\00";
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        if (!sessionName.equals("")) {
            this.mapOptions = this.mapOptions.replace(this.sessionName, sessionName);
            this.sessionName = sessionName + "\00";
        }
    }

    public final int getPlayedSeconds() {
        return playedSeconds;
    }

    public final long getSaveTimestamp() {
        return saveTimestamp;
    }

    public String getSessionVisibility() {
        if (sessionVisibility == 0) return "private";
        return "friends only";
    }

    public void setSessionVisibility(int sessionVisibility) {
        if (sessionVisibility == 0 || sessionVisibility == 1) this.sessionVisibility = (byte) sessionVisibility;
    }

    public final int getEditorObjectVersion() {
        return editorObjectVersion;
    }

    public final String getModMetadata() {
        if (modMetadata == null || modMetadata.equals("")) return "empty";
        return modMetadata;
    }

    public final String getModFlags() {
        if (modFlags == 0) return "vanilla";
        return "modded";
    }

    public final String getSaveIdentifier() {
        return saveIdentifier;
    }

    public void printData() {
        System.out.println("Header Version: " + getHeaderVersion());
        System.out.println("Save Version: " + getSaveVersion());
        System.out.println("Built Version: " + getBuiltVersion());
        System.out.println("Map Name: " + getMapName().replace("\00", ""));
        System.out.println("Map Options: " + getMapOptions().replace("\00", ""));
        System.out.println("Session Name: " + getSessionName().replace("\00", ""));
        System.out.println("Played Seconds: " + getPlayedSeconds() + " (" + (float) ((float) getPlayedSeconds() / 60 / 60) + " H)");
        System.out.println("Save Timestamp: " + getSaveTimestamp());
        System.out.println("Session Visibility: " + getSessionVisibility());
        System.out.println("Editor Object Version: " + getEditorObjectVersion());
        System.out.println("Mod Metadata: " + getModMetadata().replace("\00", ""));
        System.out.println("Mod Flags: " + getModFlags());
        System.out.println("Save Identifier: " + getSaveIdentifier().replace("\00", ""));
    }

    /**
     * Helper function to convert a <b>Little Endian</b> String to <b>Big Endian</b>.<br>
     * This function assumes that the data uses 4 bytes to store the length of the corresponding string.
     * @param stream the stream which contains the data.
     * @return new String in <b>Big Endian</b> format.
     * @throws IOException if stream is null.
     */
    private String convertLEStringToBE(DataInputStream stream) throws IOException {
        int stringLength = Integer.reverseBytes(stream.readInt());
        if (stringLength <= 0) return "";
        byte[] stringBuffer = new byte[stringLength];
        stream.read(stringBuffer, 0, stringLength);

        char[] chars = new char[stringLength];

        for (int i = 0; i < stringLength; i++) {
            chars[i] = (char) (stringBuffer[i]);
        }
        return String.valueOf(chars);
    }

    private String convertBEStringtoLE(String string) {
        char[] chars = string.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            chars[i] = Character.reverseBytes((char)chars[i]);
        }

        return new String(chars);
    }

    public void decompressBody() throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(body);
        InflaterInputStream inflaterStream = new InflaterInputStream(byteStream);
        String result = "";

        byte[] buf = new byte[5];
        int rlen = -1;
        while ((rlen = inflaterStream.read(buf)) != -1) {
            result += new String(Arrays.copyOf(buf, rlen));
        }
        // now result will contain "Hello World!"
        System.out.println("Decompress result: " + result);
    }
}
