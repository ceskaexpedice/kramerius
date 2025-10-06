package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.shared.Dom4jUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;

public class LicensesExtractor {

    public List<String> extractLicenses(Element rdfEl, String model) {
        List<String> result = new ArrayList<>();
        List<Node> licenseEls = Dom4jUtils.buildXpath(
                "Description/license" + //toto je spravny zapis, ostatni jsou chybne/stara data
                        "|Description/licenses" +
                        "|Description/licence" +
                        "|Description/licences" +
                        "|Description/dnnt-label" +
                        "|Description/dnnt-labels"
        ).selectNodes(rdfEl);
        for (Node licenseEl : licenseEls) {
            String license = toStringOrNull(licenseEl);
            if (license != null) {
                result.add(license);
            }
        }
        return result;
    }

    private String toStringOrNull(Node node) {
        return ExtractorUtils.toStringOrNull(node);
    }
}
