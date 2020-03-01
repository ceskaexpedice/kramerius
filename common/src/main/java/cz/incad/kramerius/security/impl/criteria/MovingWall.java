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
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.solr.SolrUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import javax.xml.xpath.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * 
 * Stena, ktera pousti vsechny dokumenty, ktere jsou po datumu uvedenem v konfiguraci
 * 
 * Trida vzdy porovnava pouze datum uvedem v metadatech objektu, ktery je s pravem svazan.
 *      18.2.2018 - pridani zdi pro mesic
 *                - clanky (article) i stranky (page), nemaji cele datum s mesicem jako vytisk (periodicalitem),
 *                  proto datum beru z vytisku
 * Pokud je pravo uvedeno na objetku REPOSITORY, pak zkouma nejvyssi prvek v hierarchii 
 * 
 * (konkretni monografii, konkretni periodikum, atd..)
 */
public class MovingWall extends AbstractCriterium implements RightCriterium {

    //encoding="marc"
    public static String[] MODS_XPATHS={"//mods:originInfo/mods:dateIssued[@encoding='marc']/text()","//mods:originInfo/mods:dateIssued/text()","//mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()","//mods:part/mods:date/text()"};

    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(MovingWall.class.getName());


	private XPathFactory xpfactory;
	
	public MovingWall() {
        this.xpfactory = XPathFactory.newInstance();
	}
	
    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {
        int length = getObjects().length;
        int wallFromConf = Integer.parseInt((String)getObjects()[0]);
        
        // for criterium rights, only one parameter -> value of the wall
        if (length == 1) {
              return evalute(wallFromConf);
        }
        
        String modeFromConf = (String)getObjects()[1];
        String firstModel = (String)getObjects()[2];
        String firstPid = (String)getObjects()[3];
        String fedoraModel = null;
        String parentPid = null;
        Date pidDate = null;
        Date parentDate = null;
        
        try {
            ObjectPidsPath[] pathsToRoot = getEvaluateContext().getPathsToRoot();
            EvaluatingResult result = null;
            for (ObjectPidsPath pth : pathsToRoot) {
                String[] pids = pth.getPathFromLeafToRoot();
                for (String pid : pids) {
                    
                    if (pid.equals(SpecialObjects.REPOSITORY.getPid())) continue;
                    
                    try {
                        Document doc = SolrUtils.getSolrDataInternal(SolrUtils.UUID_QUERY + "\"" + pid + "\"");
                        fedoraModel = SolrUtils.disectFedoraModel(doc);
                        parentPid = SolrUtils.disectParentPid(doc);
                        String datePid = SolrUtils.disectDate(doc);
                        pidDate = parseDate(datePid);
                        
                        Document parentDoc = SolrUtils.getSolrDataInternal(SolrUtils.UUID_QUERY + "\"" + parentPid + "\"");
                        String dateParent = SolrUtils.disectDate(parentDoc);
                        parentDate = parseDate(dateParent);

                        // if article/page/periodicalitem was chosen first and it's public -> periodical/periodicalVolume must be public too
                        if ((firstModel.equals("periodicalitem") || firstModel.equals("article") || firstModel.equals("page")) && (fedoraModel.equals("periodical") || fedoraModel.equals("periodicalvolume"))) {
                            Date currentDate = new Date();
                            Document firstDoc = SolrUtils.getSolrDataInternal(SolrUtils.UUID_QUERY + "\"" + firstPid + "\"");
                            Date firstDatePid = parseDate(SolrUtils.disectDate(firstDoc));
                            return mwCalc(wallFromConf, modeFromConf, fedoraModel, parentDate, firstDatePid, currentDate);
                        }
                        
                        if (fedoraModel != null && fedoraModel.equals("periodicalvolume")) {
                            return mwCalcItem(wallFromConf, modeFromConf, pid);
                        }
                    
                        if (fedoraModel != null && fedoraModel.equals("periodical")) {
                            String pidVolume = getEvaluateContext().getFedoraAccess().getFirstVolumePid(pid);
                            return  mwCalcItem(wallFromConf, modeFromConf, pidVolume);
                        }
                    
                    } catch (ParserConfigurationException ex) {
                        Logger.getLogger(MovingWall.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SAXException ex) {
                        Logger.getLogger(MovingWall.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (RecognitionException ex) {
                        Logger.getLogger(MovingWall.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (TokenStreamException ex) {
                        Logger.getLogger(MovingWall.class.getName()).log(Level.SEVERE, null, ex);
                    }
                   
                    Document biblioMods = getEvaluateContext().getFedoraAccess().getBiblioMods(pid);
                    // try all xpaths on mods
                    for (String xp : MODS_XPATHS) {
                        result = resolveInternal(wallFromConf,modeFromConf,pid,fedoraModel,parentPid,parentDate,xp,biblioMods, this.xpfactory);
                        if (result !=null) break;
                    }

                    if (result == null && fedoraModel != null) {
                        Date currentDate = new Date();
                        result = mwCalc(wallFromConf, modeFromConf, fedoraModel, parentDate, pidDate, currentDate);
                    } 
                    // TRUE or FALSE -> rozhodnul, nevratil NOT_APPLICABLE
                    if (result != null && (result.equals(EvaluatingResult.TRUE) ||  result.equals(EvaluatingResult.FALSE))) return result; 
                }
            }
            return result != null ? result :EvaluatingResult.NOT_APPLICABLE;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return EvaluatingResult.NOT_APPLICABLE;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return EvaluatingResult.NOT_APPLICABLE;
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return EvaluatingResult.NOT_APPLICABLE;
        }
    }

    
    public EvaluatingResult evalute(int wallFromConf) throws RightCriteriumException {
        try {
            ObjectPidsPath[] pathsToRoot = getEvaluateContext().getPathsToRoot();
            EvaluatingResult result = null;
            for (ObjectPidsPath pth : pathsToRoot) {
                String[] pids = pth.getPathFromLeafToRoot();
                for (String pid : pids) {
                    
                    if (pid.equals(SpecialObjects.REPOSITORY.getPid())) continue;
                    Document biblioMods = getEvaluateContext().getFedoraAccess().getBiblioMods(pid);
                    // try all xpaths on mods
                    for (String xp : MODS_XPATHS) {
                        result = resolveInternal(wallFromConf, null, pid, null, null, null, xp, biblioMods, this.xpfactory);
                        if (result !=null) break;
                    }
                    // TRUE or FALSE -> rozhodnul, nevratil NOT_APPLICABLE
                    if (result != null && (result.equals(EvaluatingResult.TRUE) ||  result.equals(EvaluatingResult.FALSE))) return result; 
                }
            }
            return result != null ? result :EvaluatingResult.NOT_APPLICABLE;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return EvaluatingResult.NOT_APPLICABLE;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return EvaluatingResult.NOT_APPLICABLE;
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return EvaluatingResult.NOT_APPLICABLE;
        }   
    }
    
    public static EvaluatingResult resolveInternal(int wallFromConf, String modeFromConf, String pid, String fedoraModel, String parentPid, Date parentDate, String xpath, Document xmlDoc,XPathFactory xpfactory) throws IOException, XPathExpressionException {
        if (pid.equals(SpecialObjects.REPOSITORY.getPid())) return EvaluatingResult.NOT_APPLICABLE;
        return evaluateDoc(wallFromConf, modeFromConf, fedoraModel, parentDate, xmlDoc, xpath, xpfactory);
    }



    public static EvaluatingResult evaluateDoc(int wallFromConf, String modeFromConf, String fedoraModel, Date parentDate, Document xmlDoc, String xPathExpression,XPathFactory xpfactory) throws XPathExpressionException {
        Object date = findDateString(xmlDoc, xPathExpression, xpfactory);
        if (date != null) {
            String patt = ((Text) date).getData();
            try {
                Date parsed = tryToParseDates(patt);
                Date currentDate = new Date();
                if (parsed != null) {
                    return mwCalc(wallFromConf, modeFromConf, fedoraModel, parentDate, parsed, currentDate);
                } else {
                    return EvaluatingResult.NOT_APPLICABLE;
                }
            } catch (RecognitionException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                LOGGER.log(Level.SEVERE,"Returning NOT_APPLICABLE");
                return EvaluatingResult.NOT_APPLICABLE;
            } catch (TokenStreamException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                LOGGER.log(Level.SEVERE,"Returning NOT_APPLICABLE");
                return EvaluatingResult.NOT_APPLICABLE;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                LOGGER.log(Level.SEVERE,"Returning NOT_APPLICABLE");
                return EvaluatingResult.NOT_APPLICABLE;
            }
            
        }
        return null;
    }
    
    public static Date parseDate(Document xmlDoc, String xPathExpression,XPathFactory xpfactory) throws XPathExpressionException, RecognitionException, IOException, TokenStreamException {
        Object date = findDateString(xmlDoc, xPathExpression, xpfactory);
        Date parsed = null;
        if (date != null) {
            String patt = ((Text) date).getData();
            parsed = tryToParseDates(patt);
        }
        return parsed;
    }
    
    
    public static Date parseDate(String date) throws XPathExpressionException, RecognitionException, IOException, TokenStreamException {
        List<String> patterns = Arrays.asList("dd.MM.yyyy", "MM.yyyy", "yyyy");
        if (date == null)
            return null;
        Date parsed = customizedDates(date, patterns);
        return parsed;
    }


    public static Object findDateString(Document xmlDoc, String xPathExpression, XPathFactory xpfactory)
            throws XPathExpressionException {
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile(xPathExpression);
        Object date = expr.evaluate(xmlDoc, XPathConstants.NODE);
        return date;
    }
   
    public EvaluatingResult mwCalcItem(int wallFromConf, String modeFromConf, String pidVolume) throws IOException, RecognitionException, TokenStreamException {
        try {
            Document doc = SolrUtils.getSolrDataInternal(SolrUtils.UUID_QUERY + "\"" + pidVolume + "\"");
            Date dateVolume = parseDate(SolrUtils.disectDate(doc));
            String pidItem = getEvaluateContext().getFedoraAccess().getFirstItemPid(pidVolume);
            Document itemDoc = SolrUtils.getSolrDataInternal(SolrUtils.UUID_QUERY + "\"" + pidItem + "\"");
            Date dateItem = parseDate(SolrUtils.disectDate(itemDoc));
            Date currentDate = new Date();
            return mwCalc(wallFromConf, modeFromConf, "periodicalitem", dateVolume, dateItem, currentDate);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(MovingWall.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(MovingWall.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(MovingWall.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return EvaluatingResult.FALSE;
    }
    /**
     * Computing moving wall
     * @param wallFromConf Moving wall set by user
     * @param modeFromConf Moving wall value applies for year or month
     * @param fedoraModel Fedora model of original object
     * @param parentDate Date of parent of original object
     * @param parsed Parsed date from metadata
     * @param currentDate Current date
     * @return
     */
    
    static EvaluatingResult mwCalc(int wallFromConf, String modeFromConf, String fedoraModel,
            Date parentDate, Date parsed, Date currentDate) {

        if (parsed == null) {
            parsed = parentDate;
        }
        Calendar calFromMetadata = Calendar.getInstance();
        calFromMetadata.setTime(parsed);

        Calendar currentCal = Calendar.getInstance();
        currentCal.setTime(currentDate);

        // Pocita se na cele roky. Odvolavam se na komentar p. Zabicky  
        // https://github.com/ceskaexpedice/kramerius/issues/38 se pocita zed na cele roky
        
        int yearFromMetadata = calFromMetadata.get(Calendar.YEAR);
        int currentYear = currentCal.get(Calendar.YEAR);
        
        int monthFromMetadata = calFromMetadata.get(Calendar.MONTH);
        int currentMonth = currentCal.get(Calendar.MONTH);
        
        if (modeFromConf == null) {
            modeFromConf = "year";
        }
        
        if (modeFromConf.equals("month") && fedoraModel != null && (fedoraModel.equals("article") || fedoraModel.equals("page"))) {
           calFromMetadata.setTime(parentDate);
           yearFromMetadata = calFromMetadata.get(Calendar.YEAR);
           monthFromMetadata = calFromMetadata.get(Calendar.MONTH);
        }
        if (modeFromConf.equals("year")) {
            if ((currentYear - yearFromMetadata) >= wallFromConf) {
                return EvaluatingResult.TRUE;
            } else {
                return EvaluatingResult.FALSE;
            }
        }
        if (modeFromConf.equals("month")) {
            if (12*(currentYear - yearFromMetadata) + (currentMonth - monthFromMetadata) >= wallFromConf) {
                return EvaluatingResult.TRUE;
            } else {
                return EvaluatingResult.FALSE;
            }
        }
        return EvaluatingResult.FALSE;
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
