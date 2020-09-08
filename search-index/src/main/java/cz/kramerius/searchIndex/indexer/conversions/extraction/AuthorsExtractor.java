package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.shared.Dom4jUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;

public class AuthorsExtractor {

    public List<String> extractAuthors(Element modsEl, String model) {
        List<Node> nameEls = Dom4jUtils.buildXpath("mods/name").selectNodes(modsEl);
        List<String> result = new ArrayList<>();
        for (Node nameEl : nameEls) {
            String namePartNoTypeEl = toStringOrNull(Dom4jUtils.buildXpath("namePart[not(@type)]").selectSingleNode(nameEl));
            String namePartTypeFamilyEl = toStringOrNull(Dom4jUtils.buildXpath("namePart[@type='family']").selectSingleNode(nameEl));
            String namePartTypeGivenEl = toStringOrNull(Dom4jUtils.buildXpath("namePart[@type='given']").selectSingleNode(nameEl));
            String namePartTypetermsOfAddressEl = toStringOrNull(Dom4jUtils.buildXpath("namePart[@type='termsOfAddress']").selectSingleNode(nameEl));
            if (namePartNoTypeEl != null) {
                result.add(namePartNoTypeEl);
            } else if (namePartTypeFamilyEl != null && namePartTypeGivenEl != null && namePartTypetermsOfAddressEl != null) {
                result.add(namePartTypeFamilyEl + ", " + namePartTypeGivenEl + ", " + namePartTypetermsOfAddressEl);
            } else if (namePartTypeFamilyEl != null && namePartTypeGivenEl != null) {
                result.add(namePartTypeFamilyEl + ", " + namePartTypeGivenEl);
            } else if (namePartTypeFamilyEl != null) {
                result.add(namePartTypeFamilyEl);
            } else if (namePartTypeGivenEl != null) {
                result.add(namePartTypeGivenEl);
            }
        }
        /*
        List<Node> authorNameNodes = Dom4jUtils.buildXpath("mods:mods/mods:name/mods:namePart[not(@type)]").selectNodes(modsEl);
        for (Node authorNameNode : authorNameNodes) {
            solrInput.addField("dc.creator", toStringOrNull(authorNameNode));
        }*/
        return result;
    }

    private String toStringOrNull(Node node) {
        return ExtractorUtils.toStringOrNull(node);
    }

}
