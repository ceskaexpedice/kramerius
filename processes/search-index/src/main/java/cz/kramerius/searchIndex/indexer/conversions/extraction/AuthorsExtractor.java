package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.shared.AuthorInfo;
import cz.kramerius.shared.Dom4jUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class AuthorsExtractor {

    public List<AuthorInfo> extractPrimaryAuthors(Element modsEl, String model) {
        List<Node> nameEls = Dom4jUtils.buildXpath("mods/name[@usage='primary']").selectNodes(modsEl);
        return extractAuthors(nameEls, model);
    }

    public List<AuthorInfo> extractNonPrimaryAuthors(Element modsEl, String model) {
        List<Node> nameEls = Dom4jUtils.buildXpath("mods/name[not(@usage='primary')]").selectNodes(modsEl);
        return extractAuthors(nameEls, model);
    }

    private List<AuthorInfo> extractAuthors(List<Node> nameEls, String model) {
        List<AuthorInfo> result = new ArrayList<>();
        for (Node nameEl : nameEls) {
            String namePartNoTypeEl = toStringOrNull(Dom4jUtils.buildXpath("namePart[not(@type)]").selectSingleNode(nameEl));
            String namePartTypeFamilyEl = toStringOrNull(Dom4jUtils.buildXpath("namePart[@type='family']").selectSingleNode(nameEl));
            String namePartTypeGivenEl = toStringOrNull(Dom4jUtils.buildXpath("namePart[@type='given']").selectSingleNode(nameEl));
            String namePartTypetermsOfAddressEl = toStringOrNull(Dom4jUtils.buildXpath("namePart[@type='termsOfAddress']").selectSingleNode(nameEl));

            List<Node> nodes = Dom4jUtils.buildXpath("role/roleTerm").selectNodes(nameEl);
            List<String> roles = nodes.stream().map(Node::getText).toList();
            String nameIdentifier = toStringOrNull(Dom4jUtils.buildXpath("nameIdentifier").selectSingleNode(nameEl));

            if (namePartNoTypeEl != null) {
                result.add(extractAuthor(namePartNoTypeEl, nameEl,nameIdentifier,roles));
            } else if (namePartTypeFamilyEl != null && namePartTypeGivenEl != null && namePartTypetermsOfAddressEl != null) {
                result.add(extractAuthor(namePartTypeFamilyEl + ", " + namePartTypeGivenEl + ", " + namePartTypetermsOfAddressEl, nameEl, nameIdentifier,roles));
            } else if (namePartTypeFamilyEl != null && namePartTypeGivenEl != null) {
                result.add(extractAuthor(namePartTypeFamilyEl + ", " + namePartTypeGivenEl, nameEl,nameIdentifier,roles));
            } else if (namePartTypeFamilyEl != null) {
                result.add(extractAuthor(namePartTypeFamilyEl, nameEl,nameIdentifier,roles));
            } else if (namePartTypeGivenEl != null) {
                result.add(extractAuthor(namePartTypeGivenEl, nameEl,nameIdentifier,roles));
            }
        }
        return result;
    }

    private AuthorInfo extractAuthor(String extractedName, Node modsNameEl,    String autIdentifier, List<String> roleTermElms) {
        String date = toStringOrNull(Dom4jUtils.buildXpath("namePart[@type='date']").selectSingleNode(modsNameEl));
        String[] roles = roleTermElms.toArray(new String[roleTermElms.size()]);
        return new AuthorInfo(extractedName, date,  autIdentifier, roles);
    }

    private String toStringOrNull(Node node) {
        return ExtractorUtils.toStringOrNull(node);
    }

}
