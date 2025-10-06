package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.shared.Dom4jUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;

public class LanguagesExtractor {

    public List<String> extractLanguages(Element modsEl, String model) {
        List<String> result = new ArrayList<>();
        List<Node> languageEls = Dom4jUtils.buildXpath("mods/language[not(@objectPart='translation')]").selectNodes(modsEl);
        for (Node languageEl : languageEls) {
            List<Node> languageTermAllEls = Dom4jUtils.buildXpath("languageTerm").selectNodes(languageEl);
            List<Node> languageTermIso6392bEls = Dom4jUtils.buildXpath("languageTerm[@authority='iso639-2b']").selectNodes(languageEl);
            if (!languageTermIso6392bEls.isEmpty()) {
                String language = toStringOrNull(languageTermIso6392bEls.get(0));
                if (language != null) {
                    result.add(language);
                }
            } else if (!languageTermAllEls.isEmpty()) {
                String language = toStringOrNull(languageTermAllEls.get(0));
                if (language != null) {
                    result.add(language);
                }
            }
        }
        return result;
    }

    private String toStringOrNull(Node node) {
        return ExtractorUtils.toStringOrNull(node);
    }

}
