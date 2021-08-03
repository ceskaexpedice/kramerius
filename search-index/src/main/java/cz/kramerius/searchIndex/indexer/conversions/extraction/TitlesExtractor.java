package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.shared.Dom4jUtils;
import cz.kramerius.shared.Title;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;

public class TitlesExtractor {

    public Title extractPrimaryTitle(Element modsEl, String model) {
        switch (model) {
            case "page": {
                Title fromTitleInfo = extractTitleFromTitleInfos(modsEl);
                if (fromTitleInfo != null) {
                    return fromTitleInfo;
                } else {
                    return extractTitleFromPartPageNumber(modsEl);
                }
            }
            default: {
                return extractTitleFromTitleInfos(modsEl);
            }
        }
    }

    public List<Title> extractAllTitles(Element modsEl, String model) {
        switch (model) {
            case "page": {
                List<Node> titleInfoAllEls = Dom4jUtils.buildXpath("mods/titleInfo").selectNodes(modsEl);
                List<Title> titles = new ArrayList<>();
                for (Node titleInfoEl : titleInfoAllEls) {
                    Title title = extractTitleFromTitleInfo(titleInfoEl);
                    if (title != null) {
                        titles.add(title);
                    }
                }
                Title fromPageNumber = extractTitleFromPartPageNumber(modsEl);
                if (fromPageNumber != null) {
                    titles.add(fromPageNumber);
                }
                return titles;
            }
            default: {
                List<Node> titleInfoAllEls = Dom4jUtils.buildXpath("mods/titleInfo").selectNodes(modsEl);
                List<Title> titles = new ArrayList<>();
                for (Node titleInfoEl : titleInfoAllEls) {
                    Title title = extractTitleFromTitleInfo(titleInfoEl);
                    if (title != null) {
                        titles.add(title);
                    }
                }
                return titles;
            }
        }
    }

    private Title extractTitleFromPartPageNumber(Element modsEl) {
        //pozor, DMF (alespon DMF Monografie 1.3.2+ umoznuje pouze verzi 'page number'
        Element pageNumber = (Element) Dom4jUtils.buildXpath("mods/part/detail[@type='pageNumber']/number|mods/part/detail[@type='page number']/number").selectSingleNode(modsEl);
        String value = toStringOrNull(pageNumber);
        if (value != null) {
            return new Title(value);
        } else {
            return null;
        }
    }

    public Title extractTitleFromTitleInfos(Element modsEl) {
        List<Node> titleInfoNoTypeEls = Dom4jUtils.buildXpath("mods/titleInfo[not(@type)]").selectNodes(modsEl);
        List<Node> titleInfoTypeUniformEls = Dom4jUtils.buildXpath("mods/titleInfo[@type='uniform']").selectNodes(modsEl);
        List<Node> titleInfoAllEls = Dom4jUtils.buildXpath("mods/titleInfo").selectNodes(modsEl);
        if (!titleInfoNoTypeEls.isEmpty()) {
            Title noTypeEls = extractTitleFromTitleInfo(titleInfoNoTypeEls.get(0));
            if (noTypeEls != null && !noTypeEls.toString().isEmpty()) {
                return noTypeEls;
            }
        }
        if (!titleInfoTypeUniformEls.isEmpty()) {
            Title fromTypeUniform = extractTitleFromTitleInfo(titleInfoTypeUniformEls.get(0));
            if (fromTypeUniform != null && !fromTypeUniform.toString().isEmpty()) {
                return fromTypeUniform;
            }
        }
        if (!titleInfoAllEls.isEmpty()) {
            Title any = extractTitleFromTitleInfo(titleInfoAllEls.get(0));
            if (any != null && !any.toString().isEmpty()) {
                return any;
            }
        }
        return null;
    }

    private Title extractTitleFromTitleInfo(Node titleInfoEl) {
        String nonSort = toStringOrNullNoTrimming(Dom4jUtils.buildXpath("nonSort").selectSingleNode(titleInfoEl));
        String title = buildTitle(titleInfoEl);
        if (title == null || title.isEmpty()) {
            return null;
        } else {
            return new Title(nonSort, title);
        }
    }

    private String buildTitle(Node titleInfoEl) {
        String title = toStringOrNull(Dom4jUtils.buildXpath("title").selectSingleNode(titleInfoEl));
        String subTitle = toStringOrNull(Dom4jUtils.buildXpath("subTitle").selectSingleNode(titleInfoEl));
        String partNumber = toStringOrNull(Dom4jUtils.buildXpath("partNumber").selectSingleNode(titleInfoEl));
        String partName = toStringOrNull(Dom4jUtils.buildXpath("partName").selectSingleNode(titleInfoEl));
        //4
        if (title != null && subTitle != null && partNumber != null && partName != null) {
            return String.format("%s: %s. %s. %s", title, subTitle, partNumber, partName);
        }
        //3
        if (subTitle != null && partNumber != null && partName != null) {
            return String.format("%s. %s. %s", subTitle, partNumber, partName);
        }
        if (title != null && partNumber != null && partName != null) {
            return String.format("%s. %s. %s", title, partNumber, partName);
        }
        if (title != null && subTitle != null && partName != null) {
            return String.format("%s: %s. %s", title, subTitle, partName);
        }
        if (title != null && subTitle != null && partNumber != null) {
            return String.format("%s: %s. %s", title, subTitle, partNumber);
        }
        //2
        if (title != null && subTitle != null) {
            return String.format("%s: %s", title, subTitle);
        }
        if (title != null && partNumber != null) {
            return String.format("%s. %s", title, partNumber);
        }
        if (title != null && partName != null) {
            return String.format("%s. %s", title, partName);
        }
        if (subTitle != null && partNumber != null) {
            return String.format("%s. %s", subTitle, partNumber);
        }
        if (subTitle != null && partName != null) {
            return String.format("%s. %s", subTitle, partName);
        }
        if (partNumber != null && partName != null) {
            return String.format("%s. %s", partNumber, partName);
        }
        //1
        if (title != null) {
            return String.format("%s", title);
        }
        if (subTitle != null) {
            return String.format("%s", subTitle);
        }
        if (partNumber != null) {
            return String.format("%s", partNumber);
        }
        if (partName != null) {
            return String.format("%s", partName);
        }
        return null;
    }

    private String toStringOrNull(Node node) {
        return ExtractorUtils.toStringOrNull(node);
    }

    private String toStringOrNullNoTrimming(Node node) {
        return ExtractorUtils.toStringOrNull(node, false);
    }

}
