package cz.kramerius.searchIndex.indexer.conversions;

import java.util.*;

/**
 * Normalizes strings for sorting in solr (specialized fields *.sort)
 * The requirements are:
 * - ignore diacritics ('s'=='š') for Czech, German and Slovak languages
 * - 'ch' goes after 'h', i.e. sorting is designed primarily for czech speakers ('Mucha Alfons' < 'mucho dinero')
 * - ignore case (not abc..zAbc..Z. but more like aAbBcC..zZ)
 * - non-alphabetical characters (in original string) should be ignored
 */
public class SortingNormalizer {

    private static final Set<Character> charactersToBeIgnored = initIgnoredChars();
    private static final Map<Character, String> charactersTransformations = initTransformations();

    private static Set<Character> initIgnoredChars() {
        Set<Character> result = new HashSet<>();
        String ignored = ".,!-\"'()[]:";
        for (int i = 0; i < ignored.length(); i++) {
            result.add(ignored.charAt(i));
        }
        return result;
    }

    private static Map<Character, String> initTransformations() {
        Map<Character, String> t = new HashMap<>();
        //latin lower case (just toLowerCase)
        t.put('a', "A");
        t.put('b', "B");
        t.put('c', "C");
        t.put('d', "D");
        t.put('e', "E");
        t.put('f', "F");
        t.put('g', "G");
        t.put('h', "H");
        t.put('i', "I");
        t.put('j', "J");
        t.put('k', "K");
        t.put('l', "L");
        t.put('m', "M");
        t.put('n', "N");
        t.put('o', "O");
        t.put('p', "P");
        t.put('q', "Q");
        t.put('r', "R");
        t.put('s', "S");
        t.put('t', "T");
        t.put('u', "U");
        t.put('v', "V");
        t.put('w', "W");
        t.put('x', "X");
        t.put('y', "Y");
        t.put('z', "Z");
        //latin upper case (no change)
        t.put('A', "A");
        t.put('B', "B");
        t.put('C', "C");
        t.put('D', "D");
        t.put('E', "E");
        t.put('F', "F");
        t.put('G', "G");
        t.put('H', "H");
        t.put('I', "I");
        t.put('J', "J");
        t.put('K', "K");
        t.put('L', "L");
        t.put('M', "M");
        t.put('N', "N");
        t.put('O', "O");
        t.put('P', "P");
        t.put('Q', "Q");
        t.put('R', "R");
        t.put('S', "S");
        t.put('T', "T");
        t.put('U', "U");
        t.put('V', "V");
        t.put('W', "W");
        t.put('X', "X");
        t.put('Y', "Y");
        t.put('Z', "Z");
        //czech lower case
        t.put('á', "A|");
        t.put('č', "C|");
        t.put('ď', "D|");
        t.put('é', "E|");
        t.put('ě', "E|");
        t.put('í', "I|");
        t.put('ň', "N|");
        t.put('ó', "O|");
        t.put('ř', "R|");
        t.put('š', "S|");
        t.put('ť', "T|");
        t.put('ú', "U|");
        t.put('ů', "U|");
        t.put('ý', "Y|");
        t.put('ž', "Z|");
        //czech upper case
        t.put('Á', "A|");
        t.put('Č', "C|");
        t.put('Ď', "D|");
        t.put('É', "E|");
        t.put('Ě', "E|");
        t.put('Í', "I|");
        t.put('Ň', "N|");
        t.put('Ó', "O|");
        t.put('Ř', "R|");
        t.put('Š', "S|");
        t.put('Ť', "T|");
        t.put('Ú', "U|");
        t.put('Ů', "U|");
        t.put('Ý', "Y|");
        t.put('Ž', "Z|");
        //slovak upper case
        t.put('Ä', "A|");
        t.put('Ĺ', "L|");
        t.put('Ľ', "L|");
        t.put('Ô', "O|");
        t.put('Ŕ', "R|");
        //slovak lower case
        t.put('ä', "A|");
        t.put('ĺ', "L|");
        t.put('ľ', "L|");
        t.put('ô', "O|");
        t.put('ŕ', "R|");
        //german lower case
        t.put('ä', "A|");
        t.put('ö', "O|");
        t.put('ß', "S|");
        t.put('ü', "U|");
        //german uper case
        t.put('Ä', "A|");
        t.put('Ö', "O|");
        t.put('ẞ', "S|");
        t.put('Ü', "U|");

        return t;
    }

    public String normalize(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < value.length(); i++) {
                Character c = Character.valueOf(value.charAt(i));
                if (charactersToBeIgnored.contains(c)) {
                    //nothing, ignore the character
                } else if (charactersTransformations.keySet().contains(c)) {
                    builder.append(charactersTransformations.get(c));
                } else {
                    //keep unknown character as-is
                    builder.append(c);
                }
            }
            String normalized = builder.toString();
            //CH (Czech, Slovak)
            normalized = normalized.replaceAll("CH", "H|");
            //DZ, DŽ (Slovak)
            //pozor, tohle rozbije slova jako nadzvukový (cs), odzemok (sk)
            normalized = normalized.replaceAll("DZ", "D|");
            //final trim
            normalized = normalized.trim();
            return normalized;
        }
    }
}
