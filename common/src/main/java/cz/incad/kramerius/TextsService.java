package cz.incad.kramerius;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Administrator
 */
public class TextsService {

    public static String getText(String name, String language) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        StringBuffer result = new StringBuffer();
        String lang = language;
        if (lang == null || lang.length() == 0) {
            lang = "cs";
        }
        String textsDir = Constants.WORKING_DIR + File.separator + "texts";

        String nameOfTextFile = textsDir + File.separator + name + "_" + lang;
        File file = new File(nameOfTextFile);
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }
        return result.toString();
    }

    public static void writeText(String name, String language, String text) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        if (text == null) {
            System.out.println("invalid text");
        } else {
            String lang = language;
            if (lang == null || lang.length() == 0) {
                lang = "cs";
            }

            String textsDir = Constants.WORKING_DIR + File.separator + "texts";
            File dir = new File(textsDir);
            if (!dir.exists()) {
                System.out.println("vytvorime " + textsDir);
                dir.mkdirs();
            }

            String nameOfTextFile = textsDir + File.separator + name + "_" + lang;
            File file = new File(nameOfTextFile);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            out.write(text);
            out.close();

        }
    }
}
