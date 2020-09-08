package cz.kramerius.searchIndex.indexer.conversions.extraction;

import org.dom4j.Node;

public class ExtractorUtils {

    public static String toStringOrNull(Node node) {
        return toStringOrNull(node, true);
    }

    public static String toStringOrNull(Node node, boolean trim) {
        if (node != null) {
            String value = node.getStringValue();
            if (value != null) {
                //replace multiple white spaces with single space
                value = value.replaceAll("\\s+"," ");
                if (trim) {
                    value = value.trim();
                }
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    public static String withFirstLetterInUpperCase(String string) {
        if (string == null) {
            return null;
        } else if (string.isEmpty()) {
            return string;
        } else {
            return Character.toUpperCase(string.charAt(0)) + string.substring(1);
        }
    }
}
