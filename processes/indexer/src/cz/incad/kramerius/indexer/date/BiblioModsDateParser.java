package cz.incad.kramerius.indexer.date;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.FedoraNamespaceContext;

import cz.incad.kramerius.security.impl.criteria.mw.DateLexer;
import cz.incad.kramerius.security.impl.criteria.mw.DatesParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import javax.xml.xpath.*;

import java.io.StringReader;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * This class is used to parse the years of document publication from BIBLIO MODS data stream.
 * It uses precompiled regular and XPath expressions to retrieve date nodes and parse them later.
 * Parsed years are returned in a special auxiliary structure. Before that, the class caches them
 * by document uuid.
 *
 * @author Aleksei Ermak
 * @see    DateQuintet
 */
public class BiblioModsDateParser {

    /* Textual representation of publication date
     from BIBLIO MODS data stream without any changes */
    private String dateStr;

    /* Beginning year of publication */
    private String yearBegin;

    /* End year of publication */
    private String yearEnd;

    /* Structure to cache years for certain uuid */
    private HashMap<String, DateQuintet> dateCache;

    /* XPath expressions for BIBLIO MODS date nodes extraction */
    private final String prefix = "//mods:mods/";
    private List<XPathExpression> modsDateXPathExps;
    private final List<String> modsDateXPathStrs = Arrays.asList(
            prefix + "mods:part/mods:date/text()",
            prefix + "mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()",
            prefix + "mods:originInfo/mods:dateIssued/text()"
    );

    /* Regular expressions for parsing years from text */
    private List<Pattern> yearRegexPatterns;
    private final List<String> yearRegexStrs = Arrays.asList(
            "(?<![0-9])[0-9]{3}(?![0-9])",   // 800, 999
            "(?<![0-9])[0-9]{4}(?![0-9])",   // 1941, 1945
            "(?<![0-9])[0-9]{3}-(?![0-9])",  // 194-, 199-
            "(?<![0-9])[0-9]{2}--(?![0-9])", // 18--, 19--
            "(?<![0-9])[\\^]{4}(?![0-9])"    // ^^^^
    );

    private static final Logger logger = Logger.getLogger(BiblioModsDateParser.class.getName());


    public BiblioModsDateParser() {
        dateCache = new HashMap<>();
        compileModsDateXPaths();
        compileYearRegexPatterns();
    }

    /**
     * Compiles XPath expressions to retrieve date nodes from BIBLIO MODS data stream later.
     */
    private void compileModsDateXPaths() {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        modsDateXPathExps = new ArrayList<>();
        for (String dateXPathStr : modsDateXPathStrs) {
            try {
                modsDateXPathExps.add(xpath.compile(dateXPathStr));
            } catch (XPathExpressionException e) {
                logger.warning("Can't compile XPath expressions to retrieve BIBLIO MODS date nodes!");
                logger.warning(e.getMessage());
            }
        }
    }

    /**
     * Compiles regular expressions to parse years from BIBLIO MODS date nodes content later.
     */
    private void compileYearRegexPatterns() {
        yearRegexPatterns = new ArrayList<>();
        for (String yearRegexStr : yearRegexStrs) {
            yearRegexPatterns.add(Pattern.compile(yearRegexStr));
        }
    }

    /**
     * Returns years for uuid if they were cached, otherwise returns null.
     *
     * @param  uuid uuid to check
     * @return      dates for uuid or null
     * @see    DateQuintet
     */
    public DateQuintet checkInCache(String uuid) {
        return dateCache.getOrDefault(uuid, null);
    }

    /**
     * Retrieves date nodes from BIBLIO MODS data stream by precompiled XPath expressions,
     * then parses the textual contents of the nodes by precompiled regular expressions,
     * stores parsed years to the parser attributes. At the end creates quintet structure
     * filled by that years, stores this structure to the date cache using document uuid, and returns it.
     * Returns null if BIBLIO MODS has no date nodes.
     *
     * @param  biblioMods BIBLIO MODS data stream
     * @param  uuid       uuid to save parsed dates to cache
     * @return            years for uuid or null
     * @throws XPathExpressionException
     * @see    DateQuintet
     */
    public DateQuintet extractYearsFromBiblioMods(Document biblioMods, String uuid)
            throws XPathExpressionException {

        clearActualDates();

        List<Node> dateNodes = getDateNodes(biblioMods);
        if (dateNodes.isEmpty()) {
            return null;  // BIBLIO MODS has no dates
        }

        // parse all date nodes in MODS, save dates to object attributes
        for (Node dateNode : dateNodes) {
            distributeDateFromNode(dateNode);
        }

        // parse dates in string format, setup date quintet
        DateQuintet dates = prepareDateQuintet();

        // save prepared quartet to the date cache
        dateCache.put(uuid, dates);

        return dates;
    }

    /**
     * Retrieves nodes from org.w3c.dom.Document by different precompiled XPath expressions
     * and returns list of that nodes.
     *
     * @param  doc XML document to retrieve nodes from it
     * @return     list of retrieved nodes
     * @throws XPathExpressionException
     */
    private List<Node> getDateNodes(Document doc) throws XPathExpressionException {
        List<Node> resultNodeList = new ArrayList<>();
        for (XPathExpression dateExp : modsDateXPathExps) {
            NodeList nodes = (NodeList) dateExp.evaluate(doc, XPathConstants.NODESET);
            if (nodes.getLength() > 0) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    resultNodeList.add(nodes.item(i));
                }
            }
        }
        return resultNodeList;
    }

    /**
     * Stores the textual content of given node to the parser attributes.
     * Uses the node attributes (point='start' or point='end') to decide what date it is.
     * If node has no attribute saves date to parser attribute that must be parsed later.
     *
     * @param node node containing date in textual format
     */
    private void distributeDateFromNode(Node node) {
        NamedNodeMap attributes = node.getParentNode().getAttributes();
        String nodeTextContent = node.getTextContent();

        // set only dateStr if 'point' attribute is not found
        if (attributes == null || attributes.getLength() == 0 ||
                attributes.getNamedItem("point") == null) {
            dateStr = nodeTextContent;
        }
        // otherwise get yearBegin or yearEnd
        else {
            Node point = attributes.getNamedItem("point");
            if ("start".equals(point.getNodeValue())) {
                yearBegin = nodeTextContent;
            } else {
                yearEnd = nodeTextContent;
            }
        }
    }

    /**
     * Parses extracted date in order to get rid of extra characters
     * and get the year of publication in usual numeric representation.
     * If the years of the beginning or end of publication are empty,
     * fills them with the general year of publication.
     * Returns quintet structure containing parsed dates.
     *
     * @return quintet structure containing parsed dates
     * @see    DateQuintet
     */
    private DateQuintet prepareDateQuintet() {
        String year = parseYearFromDateStr();
        Date date = parseDateOrSetDefault(year);
        if (yearBegin.isEmpty()) {
            yearBegin = year;
        }
        if (yearEnd.isEmpty()) {
            yearEnd = year;
        }
        return new DateQuintet(date, dateStr, yearBegin, yearEnd, year);
    }

    /**
     * Tries to parse original string containing date of publication.
     * If can't parse returns date specified by general year of publication.
     *
     * @param   defaultYear year to parse if original date in string can't be parsed
     * @return              date of publication or null
     */
    private Date parseDateOrSetDefault(String defaultYear) {
        Date publicationDate = parseDateFromStr(dateStr);
        if (publicationDate == null)
            publicationDate = parseDateFromStr(defaultYear);
        return publicationDate;
    }

    /**
     * Gets all possible years of publication and return general year of publication.
     * If there is more years, chooses minimal and maximal and set them as beginning and end years
     * of publication. In this case general year is the end year of publication.
     *
     * @return general year of publication
     */
    private String parseYearFromDateStr() {
        String result = "";

        // apply date patterns and get all possible years from dateStr
        List<String> yearsStr = getAllMatchedYears();
        if (yearsStr.size() > 1) {
            // several years have been found -> setup begin and end dates
            List<Integer> yearsInt = yearsStr.stream().map(Integer::valueOf).collect(Collectors.toList());
            yearBegin = String.valueOf(Collections.min(yearsInt));
            yearEnd = String.valueOf(Collections.max(yearsInt));
            result = yearEnd;
        } else if (!yearsStr.isEmpty()) {
            result = yearsStr.get(0);
        }

        return result;
    }

    /**
     * Parses extracted date in textual representation by different precompiled regular expressions.
     * In parsed years replaces characters denoting an uncertain publication date.
     *
     * @return list of all possible years of publication without any extra character
     */
    private List<String> getAllMatchedYears() {
        List<String> years = new ArrayList<>();
        for (Pattern pattern : yearRegexPatterns) {
            Matcher matcher = pattern.matcher(dateStr);
            while (matcher.find()) {
                for (int i = 0, groupCount = matcher.groupCount(); i <= groupCount; i++) {
                    years.add(replaceNonDigit(matcher.group(i)));
                }
            }
        }
        return years;
    }

    /**
     * Replaces non-digit characters denoting an uncertain publication date from string.
     *
     * @param  str string to replace characters in it
     * @return     string without characters denoting an uncertain publication date
     */
    private String replaceNonDigit(String str) {
        str = str.replaceAll("\\^", "9"); // ^^^^ -> 9999
        str = str.replaceAll("-", "0");   // 19-- -> 1900
        return str;
    }

    /**
     * Clears the parser year attributes.
     * Initializes attributes if they have not been initialized.
     */
    private void clearActualDates() {
        dateStr = "";
        yearBegin = "";
        yearEnd = "";
    }

    /**
     * Parses date from string.
     *
     * @param  str string to parse
     * @return     date or null
     */
    private Date parseDateFromStr(String str) {
        try {
            DatesParser p = new DatesParser(new DateLexer(new StringReader(str)));
            return p.dates();
        } catch (NullPointerException | RecognitionException | TokenStreamException e) {
            return null;
        }
    }
}
