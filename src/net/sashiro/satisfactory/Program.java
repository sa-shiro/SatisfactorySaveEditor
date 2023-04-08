package net.sashiro.satisfactory;

import net.sashiro.satisfactory.savefile.SaveFileHeader;
import net.sashiro.satisfactory.zlib.ZLibCompressor;

import java.io.File;
import java.io.IOException;

public class Program {
    private final static String inputStr = "Hello World!";

    public static void main(String[] args) throws IOException {
        //path to save file -- fixme: change to File()
        SaveFileHeader file = new SaveFileHeader(new File("D:\\04-Projects\\Java\\SatisfactorySaveEditor\\SaveFile.sav"));

        file.printData();

        //file.setMapOptions("?startloc=Grass Fields?sessionName=Test Map?Visibility=SV_FriendsOnly");
        file.setSessionName("Test Session");
        file.save(new File("D:\\04-Projects\\Java\\SatisfactorySaveEditor\\SaveFile_new.sav"));

    }
}