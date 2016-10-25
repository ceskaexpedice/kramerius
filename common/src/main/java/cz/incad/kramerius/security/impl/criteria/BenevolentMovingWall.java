package cz.incad.kramerius.security.impl.criteria;

import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.impl.criteria.mw.DateLexer;
import cz.incad.kramerius.security.impl.criteria.mw.DatesParser;

public class BenevolentMovingWall extends AbstractCriterium implements
        RightCriterium {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(BenevolentMovingWall.class.getName());

    public static String[] MODS_XPATHS = {
            "//mods:originInfo/mods:dateIssued/text()",
            "//mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()",
            "//mods:part/mods:date/text()" };


    private XPathFactory xpfactory;

    public BenevolentMovingWall() {
        this.xpfactory = XPathFactory.newInstance();
    }

    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {
        int wallFromConf = Integer.parseInt((String) getObjects()[0]);
        try {
            ObjectPidsPath[] pathsToRoot = getEvaluateContext()
                    .getPathsToRoot();
            EvaluatingResult result = null;
            for (ObjectPidsPath pth : pathsToRoot) {
                String[] pids = pth.getPathFromLeafToRoot();
                for (String pid : pids) {

                    if (pid.equals(SpecialObjects.REPOSITORY.getPid()))
                        continue;
                    Document biblioMods = getEvaluateContext()
                            .getFedoraAccess().getBiblioMods(pid);
                    // try all xpaths on mods
                    for (String xp : MODS_XPATHS) {
                        result = resolveInternal(wallFromConf, pid, xp,
                                biblioMods);
                        if (result != null)
                            break;
                    }

                    // rozhodnul
                    if (result != null) return result;
                }
            }

            return result != null ? result : EvaluatingResult.NOT_APPLICABLE;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            return EvaluatingResult.NOT_APPLICABLE;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            return EvaluatingResult.NOT_APPLICABLE;
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            return EvaluatingResult.NOT_APPLICABLE;
        }
    }

    public EvaluatingResult resolveInternal(int wallFromConf, String pid,
            String xpath, Document xmlDoc) throws IOException,
            XPathExpressionException {
        if (pid.equals(SpecialObjects.REPOSITORY.getPid()))
            return EvaluatingResult.NOT_APPLICABLE;
        return evaluateDoc(pid,wallFromConf, xmlDoc, xpath);
    }

    public EvaluatingResult evaluateDoc(String pid,int wallFromConf, Document xmlDoc,
            String xPathExpression) throws XPathExpressionException {
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile(xPathExpression);
        Object date = expr.evaluate(xmlDoc, XPathConstants.NODE);
        if (date != null) {
            String patt = ((Text) date).getData();

            try {
                DatesParser dateParse = new DatesParser(new DateLexer(
                        new StringReader(patt)));
                Date parsed = dateParse.dates();

                Calendar calFromMetadata = Calendar.getInstance();
                calFromMetadata.setTime(parsed);

                Calendar calFromConf = Calendar.getInstance();
                calFromConf.add(Calendar.YEAR, -1 * wallFromConf);

                return calFromMetadata.before(calFromConf) ? EvaluatingResult.TRUE
                        : EvaluatingResult.NOT_APPLICABLE;

            } catch (RecognitionException e) {
                LOGGER.log(Level.WARNING, e.getMessage() +" in object "+pid);
                LOGGER.log(Level.WARNING, "Returning NOT_APPLICABLE");
                return EvaluatingResult.NOT_APPLICABLE;
            } catch (TokenStreamException e) {
                LOGGER.log(Level.WARNING, e.getMessage() +" in object "+pid);
                LOGGER.log(Level.WARNING, "Returning NOT_APPLICABLE");
                return EvaluatingResult.NOT_APPLICABLE;
            }

        }

        return null;
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
        return new SecuredActions[] { SecuredActions.READ };
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