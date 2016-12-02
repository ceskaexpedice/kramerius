package cz.incad.kramerius.utils;


import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.ibm.icu.text.Collator;


public class NaturalOrderCollator implements Comparator<String>


{

    Collator stringCollator;

    public static void main(String[] args) {
        String[] strings = new String[]{"1-2", "1-02", "1-20", "10-20", "fred", "jane", "pic01",
                "pic2", "pic02", "pic02a", "pic3", "pic4", "pic 4 else", "pic 5", "pic05", "pic 5",
                "pic 5 something", "pic 6", "pic   7", "pic100", "pic100a", "pic120", "pic121",
                "pic02000", "tom", "x2-g8", "x2-y7", "x2-y08", "x8-y8", "anča1chtěla", "čtenář5a", "cizinec", "anča1hleděla", "an-ča1čuměla", "1", "10", "2", "20", "2a", "pic", "pic0100", ",", "#", "#1", "9", "6", "7-8", "[7,8]", "motýl noční", "motýlek"};

        List<String> orig = Arrays.asList(strings);

        System.out.println("Original: " + orig);

        List<String> scrambled = Arrays.asList(strings);
        Collections.shuffle(scrambled);

        System.out.println("Scrambled: " + scrambled);

        Collections.sort(scrambled, new NaturalOrderCollator());
        //Collections.sort(scrambled, Collator.getInstance(new Locale("cs")));

        System.out.println("Sorted: " + scrambled);
    }

    public NaturalOrderCollator(Collator stringCollator) {
        this.stringCollator = stringCollator;
    }

    public NaturalOrderCollator() {
        this.stringCollator = Collator.getInstance(new Locale("cs_CZ"));
    }

    private final boolean isDigit(char ch) {
        return ch >= 48 && ch <= 57;
    }

    /**
     * Length of string is passed in for improved efficiency (only need to calculate it once)
     **/
    private final String getChunk(String s, int slength, int marker) {
        StringBuilder chunk = new StringBuilder();
        char c = s.charAt(marker);
        chunk.append(c);
        marker++;
        if (isDigit(c)) {
            while (marker < slength) {
                c = s.charAt(marker);
                if (!isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        } else {
            while (marker < slength) {
                c = s.charAt(marker);
                if (isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        }
        return chunk.toString();
    }

    public int compare(String s1, String s2) {

        int thisMarker = 0;
        int thatMarker = 0;
        int s1Length = s1.length();
        int s2Length = s2.length();

        while (thisMarker < s1Length && thatMarker < s2Length) {
            String thisChunk = getChunk(s1, s1Length, thisMarker);
            thisMarker += thisChunk.length();

            String thatChunk = getChunk(s2, s2Length, thatMarker);
            thatMarker += thatChunk.length();

            // If both chunks contain numeric characters, sort them numerically
            int result = 0;
            if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0))) {
                int firstInt = Integer.parseInt(thisChunk);
                int secondInt = Integer.parseInt(thatChunk);
                result = firstInt - secondInt;
                if (result != 0) {
                    return result;
                }
            } else {
                result = stringCollator.compare(thisChunk, thatChunk);
            }

            if (result != 0)
                return result;
        }

        return s1Length - s2Length;
    }
}