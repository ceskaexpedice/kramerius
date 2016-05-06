package cz.incad.kramerius.auth.utils;

import java.util.Random;

public class GeneratePasswordUtils {

    static char[] CHARS = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9' ,'_',',','-','/',')','(','{','}','?','.','<','>'};

    
    public static String generatePswd() {
        StringBuffer generated = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < GeneratePasswordUtils.PASSWORD_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARS.length);
            generated.append(CHARS[randomIndex]);
        }
        return generated.toString();
    }

    public static final int PASSWORD_LENGTH = 12;

    
}