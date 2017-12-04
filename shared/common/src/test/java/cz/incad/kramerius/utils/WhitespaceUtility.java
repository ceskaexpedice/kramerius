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
    public static void compare(String input1, String input2) {
        int min = Math.min(input1.length(), input2.length());
        for (int i = 0; i < min; i++) {
            System.out.println(input1.charAt(i)+" <=> "+input2.charAt(i)+" ("+(input1.charAt(i) == input2.charAt(i))+")");
        }
    }
}
