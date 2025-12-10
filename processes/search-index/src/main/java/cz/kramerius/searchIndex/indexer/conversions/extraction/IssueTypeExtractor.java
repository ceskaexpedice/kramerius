package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.shared.Dom4jUtils;
import org.dom4j.Element;

public class IssueTypeExtractor {

    public static Type extractFromModsEl(Element modsEl) {
        Type result;
        //podle DMF Periodika 1.8 (https://www.ndk.cz/standardy-digitalizace/DMFperiodika_18_final.pdf strana 42)
        result = extractFromGenre(modsEl);
        if (result != null) {
            return result;
        }
        //specifický zápis používaný v MZK pro Lidové noviny
        //http://www.digitalniknihovna.cz/mzk/uuid/uuid:bdc405b0-e5f9-11dc-bfb2-000d606f5dc6
        result = extractFromPhysicalDescriptionNote(modsEl);
        if (result != null) {
            return result;
        }
        return null;
    }


    private static Type extractFromGenre(Element modsEl) {
        String type = ExtractorUtils.toStringOrNull(Dom4jUtils.buildXpath("mods/genre[text()='issue']/@type").selectSingleNode(modsEl));
        if (type != null) {
            type = type.trim();
            if (!type.isEmpty()) {
                if (type.matches("normal")) {
                    return new Type(0, type);
                } else if (type.matches("morning")) {
                    return new Type(1, type);
                } else if (type.matches("afternoon")) {
                    return new Type(2, type);
                } else if (type.matches("evening")) {
                    return new Type(3, type);
                } else if (type.matches("corrected")) {
                    return new Type(4, type);
                } else if (type.matches("supplement")) {
                    return new Type(5, type);
                } else if (type.matches("sequence_\\d+")) {
                    String[] tokens = type.split("_");
                    Integer intValue = Integer.valueOf(tokens[1]);
                    return new Type(10 + intValue, type);
                }
            }
        }
        return null;
    }

    private static Type extractFromPhysicalDescriptionNote(Element modsEl) {
        String note = ExtractorUtils.toStringOrNull(Dom4jUtils.buildXpath("mods/physicalDescription/note").selectSingleNode(modsEl));
        if (note != null) {
            note = note.trim();
            if (!note.isEmpty()) {
                if (note.matches(".*ranní vydání.*")) {
                    return new Type(1, "morning");
                } else if (note.matches(".*odpolední vydání.*")) {
                    return new Type(2, "afternoon");
                } else if (note.matches(".*polední vydání.*")) { //pozor, 'polední' je substring 'odpolední
                    return new Type(2, "afternoon");
                }
            }
        }
        return null;
    }

    public static class Type {

        public final Integer sort;
        public final String code;

        public Type(Integer sort, String code) {
            this.sort = sort;
            this.code = code;
        }

        @Override
        public String toString() {
            return "Type{" +
                    "sort=" + sort +
                    ", code='" + code + '\'' +
                    '}';
        }
    }
}
