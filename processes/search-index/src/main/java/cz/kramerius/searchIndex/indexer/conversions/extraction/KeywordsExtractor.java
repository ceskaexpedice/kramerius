package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.shared.Dom4jUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;

public class KeywordsExtractor {

    public List<String> extractKeywords(Element modsEl, String model) {
        List<String> result = new ArrayList<>();
        List<Node> topicEls = Dom4jUtils.buildXpath("mods/subject/topic").selectNodes(modsEl);
        for (Node topicEl : topicEls) {
            String keyword = toStringOrNull(topicEl);
            if (keyword != null) {
                result.add(keyword);
            }
        }
        return result;
    }

    private String toStringOrNull(Node node) {
        return ExtractorUtils.toStringOrNull(node);
    }
}