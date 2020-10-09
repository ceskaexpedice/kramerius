package cz.kramerius.searchIndex.indexer.conversions;

/**
 * Normalizes strings for sorting in solr (specialized fields *.sort)
 * The requirements are:
 * - ignore diacritics ('s'=='š') for Czech, German and Slovak languages
 * - 'ch' goes after 'h', i.e. sorting is designed primarily for czech speakers ('Mucha Alfons' < 'mucho dinero')
 * - ignore case (not abc..zAbc..Z. but more like aAbBcC..zZ)
 * - non-alphabetical characters (in original string) should be ignored
 */
public class SortingNormalizer {

    public String normalize(String value) {
        String result = value.toUpperCase()
                .replaceAll("Á", "A|")
                .replaceAll("Č", "C|")
                .replaceAll("Ď", "D|")
                .replaceAll("É", "E|")
                .replaceAll("Ě", "E|")
                .replaceAll("CH", "H|")
                .replaceAll("Í", "I|")
                .replaceAll("Ň", "N|")
                .replaceAll("Ó", "O|")
                .replaceAll("Ř", "R|")
                .replaceAll("Š", "S|")
                .replaceAll("Ť", "T|")
                .replaceAll("Ú", "U|")
                .replaceAll("Ů", "U|")
                .replaceAll("Ý", "Y|")
                .replaceAll("Ž", "Z|");
        if (result.startsWith("[")) {
            result = result.substring(1);
        }
        return result;
    }
}
