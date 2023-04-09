package net.sashiro.satisfactory;

import net.sashiro.satisfactory.savefile.SaveFileHeader;
import net.sashiro.satisfactory.zlib.ZLibCompressor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Program {
    private final static String inputStr = "Hello World!";

    public static void main(String[] args) throws IOException {
        //path to save file -- fixme: change to File()
        Files.createDirectories(Paths.get(".\\chunks"));
        Files.createDirectories(Paths.get(".\\decoded_chunks"));
        Files.createDirectories(Paths.get(".\\unknown_data"));
        SaveFileHeader file = new SaveFileHeader(new File(".\\SaveFile.sav"));

        file.printData();

    }
}