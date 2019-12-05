package cz.incad.kramerius.security.impl.criteria;

import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

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

/**
 * Kriterium
 * 
 * @author pavels
 */
public class Window extends AbstractCriterium implements RightCriterium {

    public static String[] MODS_XPATHS = {
            "//mods:originInfo/mods:dateIssued/text()",
            "//mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()",
            "//mods:part/mods:date/text()" };

    public static final Logger LOGGER = Logger
            .getLogger(Window.class.getName());

    private XPathFactory xpfactory;

    public Window() {
        this.xpfactory = XPathFactory.newInstance();
    }

    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {
        if (getObjects().length >= 2) {
            int firstVal = Integer.parseInt((String) getObjects()[0]);
            int secondVal = Integer.parseInt((String) getObjects()[1]);
            try {
                ObjectPidsPath[] pathsToRoot = getEvaluateContext()
                        .getPathsToRoot();
                EvaluatingResult result = null;
                for (ObjectPidsPath pth : pathsToRoot) {
                    String[] pids = pth.getPathFromLeafToRoot();
                    for (String pid : pids) {

                        // try all xpaths on mods
                        if (pid.equals(SpecialObjects.REPOSITORY.getPid()))
                            continue;
                        Document biblioMods = getEvaluateContext()
                                .getFedoraAccess().getBiblioMods(pid);
                        for (String xp : MODS_XPATHS) {
                            result = resolveInternal(firstVal, secondVal, pid,
                                    xp, biblioMods);
                            if (result != null)
                                break;
                        }

                        // TRUE or FALSE -> rozhodnul, nevratil NOT_APPLICABLE
                        if (result != null
                                && (result.equals(EvaluatingResult.TRUE) || result
                                        .equals(EvaluatingResult.FALSE)))
                            return result;
                    }
                }

                return result != null ? result
                        : EvaluatingResult.NOT_APPLICABLE;
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

        } else {
            LOGGER.severe("expecting two arguments <first_year>;<second_year>");
        }

        return null;
    }

    public EvaluatingResult resolveInternal(int firstYear, int secondYear,
            String pid, String xpath, Document xmlDoc) throws IOException,
            XPathExpressionException {
        if (pid.equals(SpecialObjects.REPOSITORY.getPid()))
            return EvaluatingResult.NOT_APPLICABLE;
        return evaluateDoc(firstYear, secondYear, xmlDoc, xpath);
    }

    public EvaluatingResult evaluateDoc(int firstYear, int secondYear,
            Document xmlDoc, String xPathExpression)
            throws XPathExpressionException {
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

                boolean result = firstYear < calFromMetadata.get(Calendar.YEAR)
                        && calFromMetadata.get(Calendar.YEAR) < secondYear;
                LOGGER.info("" + firstYear + " < "
                        + calFromMetadata.get(Calendar.YEAR) + " < "
                        + secondYear + " result = " + result);
                return result ? EvaluatingResult.TRUE : EvaluatingResult.FALSE;

            } catch (RecognitionException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                LOGGER.log(Level.SEVERE, "Returning NOT_APPLICABLE");
                return EvaluatingResult.NOT_APPLICABLE;
            } catch (TokenStreamException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                LOGGER.log(Level.SEVERE, "Returning NOT_APPLICABLE");
                return EvaluatingResult.NOT_APPLICABLE;
            }

        } else
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
        if (vals.length == 2) {
            try {
                Integer.parseInt((String) vals[0]);
                Integer.parseInt((String) vals[1]);
                return true;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return false;
            }
        } else
            return false;
    }

}
