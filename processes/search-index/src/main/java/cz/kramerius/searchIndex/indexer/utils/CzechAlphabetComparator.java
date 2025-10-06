package cz.kramerius.searchIndex.indexer.utils;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class CzechAlphabetComparator implements Comparator<String> {
    private Collator czechCollator = Collator.getInstance(
            new Locale("cs", "CZ"));

    public int compare(String s1, String s2) {
        return czechCollator.compare(s1, s2);
    }
}
