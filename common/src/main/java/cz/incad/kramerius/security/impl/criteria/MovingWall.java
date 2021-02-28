/*
 * Copyright (C) 2010 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.security.impl.criteria;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.criteria.mw.DateLexer;
import cz.incad.kramerius.security.impl.criteria.mw.DatesParser;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * 
 * Stena, ktera pousti vsechny dokumenty, ktere jsou po datumu uvedenem v konfiguraci
 * 
 * Trida vzdy porovnava pouze datum uvedem v metadatech objektu, ktery je s pravem svazan.  
 * Pokud je pravo uvedeno na objetku REPOSITORY, pak zkouma nejvyssi prvek v hierarchii 
 * 
 * (konkretni monografii, konkretni periodikum, atd..)
 */
public class MovingWall extends AbstractCriterium implements RightCriterium {

    //encoding="marc"
    public static String[] MODS_XPATHS={
            "//mods:originInfo/mods:dateIssued[@encoding='marc']/text()",
            "//mods:originInfo/mods:dateIssued/text()",
            "//mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()",
            "//mods:part/mods:date/text()",
            "//mods:date/text()"
    };

    
    static transient java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(MovingWall.class.getName());


    private XPathFactory xpfactory;

    public MovingWall() {
        this.xpfactory = XPathFactory.newInstance();
    }

    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        int wallFromConf = Integer.parseInt((String)getObjects()[0]);
        try {
            ObjectPidsPath[] pathsToRoot = getEvaluateContext().getPathsToRoot();
            EvaluatingResultState result = null;
            for (ObjectPidsPath pth : pathsToRoot) {
                String[] pids = pth.getPathFromLeafToRoot();
                for (String pid : pids) {
                    
                    if (pid.equals(SpecialObjects.REPOSITORY.getPid())) continue;
                    Document biblioMods = getEvaluateContext().getFedoraAccess().getBiblioMods(pid);
                    try {
                        XMLUtils.print(biblioMods, System.out);
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    }
                    // try all xpaths on mods
                    for (String xp : MODS_XPATHS) {
                        result = resolveInternal(wallFromConf,pid,xp,biblioMods, this.xpfactory);
                        if (result !=null) break;
                    }
                    // TRUE or FALSE -> rozhodnul, nevratil NOT_APPLICABLE
                    if (result != null && (result.equals(EvaluatingResultState.TRUE) ||  result.equals(EvaluatingResultState.FALSE))) return result;
                }
            }
            return result != null ? result :EvaluatingResultState.NOT_APPLICABLE;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return EvaluatingResultState.NOT_APPLICABLE;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return EvaluatingResultState.NOT_APPLICABLE;
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return EvaluatingResultState.NOT_APPLICABLE;
        }
    }


    @Override
    public EvaluatingResultState mockEvaluate(DataMockExpectation dataMockExpectation) throws RightCriteriumException {
        switch (dataMockExpectation) {
            case EXPECT_DATA_VAUE_EXISTS: return EvaluatingResultState.TRUE;
            case EXPECT_DATA_VALUE_DOESNTEXIST: return EvaluatingResultState.NOT_APPLICABLE;
        }
        return EvaluatingResultState.NOT_APPLICABLE;
    }

    public static EvaluatingResultState resolveInternal(int wallFromConf, String pid, String xpath, Document xmlDoc, XPathFactory xpfactory) throws IOException, XPathExpressionException {
        if (pid.equals(SpecialObjects.REPOSITORY.getPid())) return EvaluatingResultState.NOT_APPLICABLE;
        return evaluateDoc(wallFromConf, xmlDoc, xpath, xpfactory);
    }



    public static EvaluatingResultState evaluateDoc(int wallFromConf, Document xmlDoc, String xPathExpression, XPathFactory xpfactory) throws XPathExpressionException {
        Object date = findDateString(xmlDoc, xPathExpression, xpfactory);
        if (date != null) {
            String patt = ((Text) date).getData();

            try {
                Date parsed = tryToParseDates(patt);
                Date currentDate = new Date();
                if (parsed != null) {
                    return mwCalc(wallFromConf, parsed, currentDate);
                } else {
                    return EvaluatingResultState.NOT_APPLICABLE;
                }
            } catch (RecognitionException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                LOGGER.log(Level.SEVERE,"Returning NOT_APPLICABLE");
                return EvaluatingResultState.NOT_APPLICABLE;
            } catch (TokenStreamException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                LOGGER.log(Level.SEVERE,"Returning NOT_APPLICABLE");
                return EvaluatingResultState.NOT_APPLICABLE;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                LOGGER.log(Level.SEVERE,"Returning NOT_APPLICABLE");
                return EvaluatingResultState.NOT_APPLICABLE;
            }
            
        }

        
        return null;
    }

    public static Object findDateString(Document xmlDoc, String xPathExpression, XPathFactory xpfactory)
            throws XPathExpressionException {
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile(xPathExpression);
        Object date = expr.evaluate(xmlDoc, XPathConstants.NODE);
        return date;
    }

    
    /**
     * Computing moving wall
     * @param wallFromConf Moving wall set by user
     * @param parsed Parsed date from metadata
     * @param currentDate Current date
     * @return
     */
    static EvaluatingResultState mwCalc(int wallFromConf, Date parsed,
                                        Date currentDate) {
        Calendar calFromMetadata = Calendar.getInstance();
        calFromMetadata.setTime(parsed);

        Calendar currentCal = Calendar.getInstance();
        currentCal.setTime(currentDate);
        
        // Pocita se na cele roky. Odvolavam se na komentar p. Zabicky  
        // https://github.com/ceskaexpedice/kramerius/issues/38 se pocita zed na cele roky
        
        int yearFromMetadata = calFromMetadata.get(Calendar.YEAR);
        int currentYear = currentCal.get(Calendar.YEAR);
        if ((currentYear - yearFromMetadata) >= wallFromConf) {
            return EvaluatingResultState.TRUE;
        } else {
            return EvaluatingResultState.FALSE;
        }
    }

    public static Date tryToParseDates(String patt)
            throws RecognitionException, TokenStreamException, IOException {
        try {
            return ndkDates(patt);
        } catch (Exception e) {
            try {
                // normalize text and test again; remove all characters under 127 
                return ndkDates(normalizedString(patt));
            } catch (Exception e1) {
                // try to parse custom 
                List<String> patterns = readCustomizedPatterns();
                return customizedDates(patt, patterns);
            }
        }
    }

    /**
     * Remove NONASCII character and try parse again
     * @param patt
     * @return
     */
    public static String normalizedString(String patt) {
        StringBuilder builder = new StringBuilder();
        char[] charArray = patt.toCharArray();
        for (char c : charArray) {
            int val = c;
            if (val < 127) {
                builder.append(c);
            }
        }
        return builder.toString();
    }
    
    public static List<String> readCustomizedPatterns() throws IOException {
        List<String> retvals = new ArrayList<String>();
        BufferedReader buffReader = null;
        try {
            String patternFile = KConfiguration.getInstance().getConfiguration().getString("mw.patterns.file", "${sys:user.home}/.kramerius4/mw.patterns");
            File file = new File(patternFile);
            if (file.exists()) {
                FileReader freader = new FileReader(file);

                buffReader = new BufferedReader(freader);
                String line = null;
                while((line = buffReader.readLine()) != null ) {
                    retvals.add(line);
                }
            }
        } finally {
            IOUtils.tryClose(buffReader);
        }
        return retvals;
        
    }
    
    /**
     * Parse customized dates
     * @param patt Read date from data
     * @return Parsed date
     * @throws IOException
     */
    public static Date customizedDates(String patt, List<String> patterns) throws IOException {
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdateFormat = new SimpleDateFormat(pattern);
                Date parsed = sdateFormat.parse(patt);
                // parsed -> return
                return parsed;
            } catch (ParseException e) {
                // continue
                LOGGER.fine("failed to parse date "+patt);
            }
        }
        return null;
    }
    
    /**
     * NDK dates - NDK specifications
     * @param patt REad date from data
     * @return Parsed date
     * @throws RecognitionException
     * @throws TokenStreamException
     */
    public static Date ndkDates(String patt) throws RecognitionException,
            TokenStreamException {
        DatesParser dateParse = new DatesParser(new DateLexer(new StringReader(patt)));
        Date parsed = dateParse.dates();
        return parsed;
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
        return new SecuredActions[] {SecuredActions.READ};
    }

    @Override
    public boolean validateParams(Object[] vals) {
        if (vals.length == 1) {
            try {
                Integer.parseInt((String) vals[0]);
                return true;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
                return false;
            }
        } else return false;
    }
    
    
    
}
