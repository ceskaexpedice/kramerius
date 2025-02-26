package cz.incad.kramerius.statistics.accesslogs;

import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaceContext;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaces;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractStatisticsAccessLog implements StatisticsAccessLog {

    public static final Logger LOGGER = Logger.getLogger(AbstractStatisticsAccessLog.class.getName());

    public static final String ISBN_MODS_KEY = "isbn";
    public static final String ISSN_MODS_KEY = "issn";
    public static final String CCNB_MODS_KEY = "ccnb";


    protected static final String[] MODS_DATE_XPATHS = {
            "//mods:part/mods:date/text()",
            "//mods:originInfo/mods:dateIssued/text()",
            "//mods:originInfo/mods:dateIssued[@encoding='marc']/text()",
            "//mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()"
    };
    protected static final List<XPathExpression> MODS_DATE_XPATH_EXPRS = new ArrayList<XPathExpression>() {{
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new RepositoryNamespaceContext());
        for (String strExpr : MODS_DATE_XPATHS) {
            try {
                add(xpath.compile(strExpr));
            } catch (XPathExpressionException e) {
                LOGGER.log(Level.SEVERE, "Can't compile XPath expression \"" + strExpr + "\"!", e);
            }
        }
    }};


    static Map<String, ReportedAction> ACTIONS = new HashMap<>();
    static {
        AbstractStatisticsAccessLog.ACTIONS.put("img", ReportedAction.READ);
        AbstractStatisticsAccessLog.ACTIONS.put("pdf", ReportedAction.PDF);
        AbstractStatisticsAccessLog.ACTIONS.put("print", ReportedAction.PRINT);
        AbstractStatisticsAccessLog.ACTIONS.put("zoomify", ReportedAction.READ);
    }

    protected ThreadLocal<ReportedAction> reportedAction = new ThreadLocal<ReportedAction>();


}
