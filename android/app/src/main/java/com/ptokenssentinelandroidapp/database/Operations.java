package com.ptokenssentinelandroidapp.database;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.google.android.gms.common.util.IOUtils;

import static android.content.Context.MODE_PRIVATE;
public class Operations {

    public static final String TAG = Operations.class.getName();

    public static String readFile(String name) {
        File dataDirectory = new File("/data/local/tmp");

        Log.v(TAG, "reading " + name + " from " + dataDirectory);
        File fileToRead = new File(dataDirectory, name);

        //Read text from file
        String line;
        StringBuilder text = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader(fileToRead))) {
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "failed to read the file " + fileToRead, e);
            return "";
        }

        return text.toString();
    }

    public static void writeBytes(Context context, String fileName, byte[] signedState) {
        try (FileOutputStream outputStream = context.openFileOutput(fileName, MODE_PRIVATE)){
            outputStream.write(signedState);
            outputStream.flush();
            Log.i(TAG, fileName + " written to disk");
        } catch (IOException e) {
            Log.e(TAG, "failed to write " + fileName + " to disk:", e);
        }
    }

    public static byte[] readBytes(Context context, String fileName) {
        byte[] ba = {};
        try (FileInputStream inputStream = context.openFileInput(fileName)){
            ba = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            Log.w(TAG, "failed to get the signature from the file, ok if first run!");
        }
        return ba;
    }

    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    public static void deleteFile(String name) {
        File f = new File("/data/local/tmp" + name);
        if (f.delete()) {
            System.out.println("deleted the file: " + f.getName());
        } else {
            System.out.println("failed to delete the file.");
        }
    }
}
