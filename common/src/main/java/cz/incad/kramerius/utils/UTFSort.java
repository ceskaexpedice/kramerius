package cz.incad.kramerius.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Used to replace punctuation marks, special characters, and accented characters
 * so that the resulting text can be easily sorted in Solr.
 *
 * @author alberto
 * @author Aleksei Ermak
 */
public class UTFSort {

    Map<Integer, String> replacementMap = new HashMap<>();

    /**
     * Loads special characters and their replacements from file, parses them and store to use later.
     */
    public UTFSort() throws IOException {
        InputStream is = UTFSort.class.getResourceAsStream("unicode_map.st");
        List<String> allLines = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.toList());
        for (String line : allLines) {
            int firstSpaceIdx = line.indexOf(" ");
            String keyString = line.substring(0, firstSpaceIdx);
            int keyInt = Integer.decode("#"+keyString);
            String[] replacementSymbols = line.substring(firstSpaceIdx + 1).split(" ");
            String replacement = Arrays.stream(replacementSymbols)
                    .filter(sym -> !sym.equals("0000"))
                    .map(this::decode)
                    .collect(Collectors.joining());
            replacementMap.put(keyInt, replacement);
        }
        is.close();
    }

    /**
     * Replaces special characters in incoming string.
     * @param originalStr original raw string
     * @return            string without any punctuation marks, special or accented characters
     */
    public String translate(String originalStr) {
        StringBuffer sb = new StringBuffer();
        originalStr.codePoints().forEach((code)->{
            String replacementStr=replacementMap.get(code);
            if (replacementStr == null){
                sb.appendCodePoint(code);
            }else{
                sb.append(replacementStr);
            }
        });
        String translatedStr = sb.toString();
        return translatedStr.replace("CH", "H|");
    }

    private String decode(String encodedSymbol) {
        return String.valueOf((char) Integer.parseInt(encodedSymbol, 16));
    }

    public void printMap() {
        replacementMap.forEach((symbol, replacement) -> System.out.println(symbol + " -> " + replacement));
    }

    public static void main(String[] args) throws IOException {
        UTFSort u = new UTFSort();
        u.printMap();
        System.out.println(u.translate("která mají řadicí platnost (tj. č,ř,š,ž,Č,Š,Ř,Ž)"));
    }
}
