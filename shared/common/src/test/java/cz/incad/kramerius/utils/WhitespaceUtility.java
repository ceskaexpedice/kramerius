package cz.incad.kramerius.utils;

public class WhitespaceUtility {

    public static String replace(String input) {
        char previous = ' ';
        StringBuilder builder = new StringBuilder();
        char[] chrs = input.toCharArray();
        for (char c : chrs) {
            if (c != ' ' && Character.isWhitespace(c)) {
                if (previous != ' ') {
                    builder.append(' ');
                }
                previous = ' ';
            } else {
                builder.append(c);
                previous = c;
            }
        }
        return builder.toString();
    }

    public static String remove(String input ) {
        StringBuilder builder = new StringBuilder();
        input.chars().forEach((ch) -> {
            char tch = (char) ch;
            if (!Character.isWhitespace(tch)) {
                builder.append(tch);
            }
        });
        return builder.toString();
    }

}
