package cz.incad.kramerius.security.impl.criteria;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import cz.incad.kramerius.security.*;
import org.ceskaexpedice.akubra.core.repository.KnownDatastreams;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaceContext;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.impl.criteria.mw.DateLexer;
import cz.incad.kramerius.security.impl.criteria.mw.DatesParser;


public class BenevolentMovingWall extends AbstractCriterium implements RightCriterium {

    private static final Logger LOGGER = Logger.getLogger(BenevolentMovingWall.class.getName());

    private static final String[] MODS_DATE_XPATHS = {
            "//mods:part/mods:date/text()",
            "//mods:originInfo/mods:dateIssued/text()",
            "//mods:originInfo/mods:dateIssued[@encoding='marc']/text()",
            "//mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()"
    };

    private static final List<XPathExpression> MODS_DATE_XPATH_EXPRS = new ArrayList<XPathExpression>() {{
        XPathFactory xpFactory = XPathFactory.newInstance();
        XPath xpath = xpFactory.newXPath();
        xpath.setNamespaceContext(new RepositoryNamespaceContext());
        for (String strExpr : MODS_DATE_XPATHS) {
            try {
                add(xpath.compile(strExpr));
            } catch (XPathExpressionException e) {
                LOGGER.log(Level.SEVERE, "Can't compile XPath expression \"" + strExpr + "\"!", e);
            }
        }
    }};

    @Override
    public EvaluatingResultState evalute(Right right) throws RightCriteriumException {
        int configWall = Integer.parseInt((String) getObjects()[0]);
        ObjectPidsPath[] pathsToRoot = getEvaluateContext().getPathsToRoot();
        for (ObjectPidsPath pth : pathsToRoot) {
            String[] pids = pth.getPathFromLeafToRoot();
            for (String pid : pids) {
                if (pid.equals(SpecialObjects.REPOSITORY.getPid()))
                    continue;
                EvaluatingResultState result = evaluateByModsDate(pid, configWall);
                if (result != null)
                    return result;
            }
        }
        return EvaluatingResultState.NOT_APPLICABLE;
    }

    private EvaluatingResultState evaluateByModsDate(String pid, int configWall) {
        try {
            InputStream inputStream = getEvaluateContext().getAkubraRepository().getDatastreamContent(pid, KnownDatastreams.BIBLIO_MODS);
            Document biblioMods = DomUtils.streamToDocument(inputStream);
            for (XPathExpression expr : MODS_DATE_XPATH_EXPRS) {
                Date modsDate = getDate(expr, biblioMods);
                EvaluatingResultState result = evaluate(modsDate, configWall);
                if (result != null)
                    return result;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Can't get BIBLIO_MODS datastream of " + pid, e);
        }
        return null;
    }

    private Date getDate(XPathExpression expr, Document bibilioMods)
            throws XPathExpressionException, TokenStreamException, RecognitionException {
        Object dateObj = expr.evaluate(bibilioMods, XPathConstants.NODE);
        if (dateObj != null) {
            String dateStr = ((Text) dateObj).getData();
            DatesParser dateParse = new DatesParser(new DateLexer(new StringReader(dateStr)));
            return dateParse.dates();
        }
        return null;
    }

    private EvaluatingResultState evaluate(Date date, int configWall) {
        if (date == null)
            return null;
        Calendar calFromMetadata = Calendar.getInstance();
        calFromMetadata.setTime(date);
        Calendar calFromConf = Calendar.getInstance();
        calFromConf.add(Calendar.YEAR, -1 * configWall);
        return calFromMetadata.before(calFromConf) ?
                EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
    }

    @Override
    public EvaluatingResultState mockEvaluate(Right right, DataMockExpectation dataMockExpectation) throws RightCriteriumException {
        switch (dataMockExpectation) {
            case EXPECT_DATA_VAUE_EXISTS: return EvaluatingResultState.TRUE;
            case EXPECT_DATA_VALUE_DOESNTEXIST: return EvaluatingResultState.NOT_APPLICABLE;
        }
        return EvaluatingResultState.NOT_APPLICABLE;
    }

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.NORMAL;
    }

    @Override
    public boolean isParamsNecessary() {
        return true;
    }

    @Override
    public SecuredActions[] getApplicableActions() {
        return new SecuredActions[] { SecuredActions.A_READ };
    }

    @Override
    public boolean validateParams(Object[] vals) {
        if (vals.length == 1) {
            try {
                Integer.parseInt((String) vals[0]);
                return true;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return false;
            }
        } else
            return false;
    }
}
