package edu.washington.cs.cellasecure.storage;

import android.content.Context;
import android.os.AsyncTask;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DeviceUtils {
    private static final String mFilename = "address_to_name_map.dat";

    // mapToFile
    public static void mapToFile(Context context, Map<String, String> addrNameMap) throws IOException {
        FileOutputStream outputStream;

        outputStream = context.openFileOutput(mFilename, Context.MODE_PRIVATE);
        for (Map.Entry<String, String> e : addrNameMap.entrySet()) {
            String line = e.getKey() + "\t" + e.getValue() + "\n";
            outputStream.write(line.getBytes());
        }
        outputStream.close();

    }

    // fileToMap
    public static Map<String, String> fileToMap(Context context) {
        Map<String, String> addrNameMap = new HashMap<String, String>();
        try {
            Scanner scan = new Scanner(context.openFileInput(mFilename));
            String line;
            String[] splitLine;
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                splitLine = line.split("\t");
                if (splitLine.length != 2) throw new IllegalStateException("Bad file data");
                addrNameMap.put(splitLine[0], splitLine[1]);
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
        return addrNameMap;
    }

}
