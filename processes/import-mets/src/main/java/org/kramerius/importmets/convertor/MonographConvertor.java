package org.kramerius.importmets.convertor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.kramerius.importmets.valueobj.ConvertorConfig;
import org.kramerius.importmets.valueobj.DublinCore;
import org.kramerius.importmets.valueobj.FileDescriptor;
import org.kramerius.importmets.valueobj.RelsExt;
import org.kramerius.importmets.valueobj.ServiceException;

import com.qbizm.kramerius.imp.jaxb.monograph.Contributor;
import com.qbizm.kramerius.imp.jaxb.monograph.ContributorName;
import com.qbizm.kramerius.imp.jaxb.monograph.Creator;
import com.qbizm.kramerius.imp.jaxb.monograph.CreatorName;
import com.qbizm.kramerius.imp.jaxb.monograph.Language;
import com.qbizm.kramerius.imp.jaxb.monograph.Monograph;
import com.qbizm.kramerius.imp.jaxb.monograph.MonographBibliographicRecord;
import com.qbizm.kramerius.imp.jaxb.monograph.MonographComponentPart;
import com.qbizm.kramerius.imp.jaxb.monograph.MonographComponentPartRepresentation;
import com.qbizm.kramerius.imp.jaxb.monograph.MonographPage;
import com.qbizm.kramerius.imp.jaxb.monograph.MonographUnit;
import com.qbizm.kramerius.imp.jaxb.monograph.MonographUnitRepresentation;
import com.qbizm.kramerius.imp.jaxb.monograph.PageIndex;
import com.qbizm.kramerius.imp.jaxb.monograph.PageRepresentation;
import com.qbizm.kramerius.imp.jaxb.monograph.PartInImage;
import com.qbizm.kramerius.imp.jaxb.monograph.PartInText;
import com.qbizm.kramerius.imp.jaxb.monograph.Publisher;
import com.qbizm.kramerius.imp.jaxb.monograph.Subject;
import com.qbizm.kramerius.imp.jaxb.monograph.TechnicalDescription;
import com.qbizm.kramerius.imp.jaxb.monograph.UniqueIdentifier;
import com.qbizm.kramerius.imp.jaxb.monograph.UniqueIdentifierURNType;

/**
 * Konvertor monografii do foxml
 *
 * @author xholcik
 */

public class MonographConvertor extends BaseConvertor {

    /**
     * XSL transformacni sablony
     */
    private static final String XSL_MODS_MONOGRAPH = "model_monograph_MODS.xsl";

    private static final String XSL_MODS_MONOGRAPH_UNIT = "model_monographUnit-MODS.xsl";

    private static final String XSL_MODS_MONOGRAPH_PAGE = "model_monographPage-MODS.xsl";

    private static final String XSL_MODS_MONOGRAPH_PART = "model_monographComponentPart-MODS.xsl";

    public MonographConvertor(ConvertorConfig config, Unmarshaller unmarshaller) throws ServiceException {
        super(config, unmarshaller);
    }

    /**
     * @param uid
     * @return
     * @throws ServiceException
     */
    private String uuid(UniqueIdentifier uid) throws ServiceException {
        String pid;
        if (uid == null || uid.getUniqueIdentifierURNType() == null || !Pattern.matches(PID_PATTERN, PID_PREFIX + first(uid.getUniqueIdentifierURNType().getContent()))) {
            pid = generateUUID();
            log.info("Assigned new UUID:"+pid);
            if (uid.getUniqueIdentifierURNType() == null) {
                uid.setUniqueIdentifierURNType(new UniqueIdentifierURNType());
            }
            uid.getUniqueIdentifierURNType().getContent().add(pid);
        } else {
            pid = first(uid.getUniqueIdentifierURNType().getContent());
        }

        return pid;
    }

    /**
     * Konvertuje monografii a vsechny podobjekty do sady foxml souboru
     *
     * @param mono
     * @throws ServiceException
     */
    public void  convert(Monograph mono, StringBuffer convertedURI) throws ServiceException {
        MonographBibliographicRecord biblio = mono.getMonographBibliographicRecord();
        String title = first(biblio.getTitle().getMainTitle().getContent());
        if (mono.getUniqueIdentifier() == null) {
            mono.setUniqueIdentifier(new UniqueIdentifier());
        }
        String uuid = uuid(mono.getUniqueIdentifier());
        String pid = pid(uuid);

        String cleanTitle= StringUtils.replaceEach(title, new String[]{"\t", "\n"}, new String[]{" ", " "});
        convertedURI.append(cleanTitle).append("\t").append("pid=").append(pid);
        // neplatny vstupni objekt
        //if (mono.getMonographBibliographicRecord().getSeries() != null && mono.getMonographBibliographicRecord().getSeries().size() > 1) {
        //    throw new IllegalArgumentException("Illegal multiple /Monograph/MonographBibliographicRecord/Series occurence!");
        //}

        RelsExt re = new RelsExt(pid, MODEL_MONOGRAPH);
        boolean visibility = isPublic(uuid, config.isDefaultVisibility(), "m_monograph");
        String contract = getContract(mono.getMonographPage());
        if (contract == null) {
            MonographUnit unit = firstItem(mono.getMonographUnit());
            if (unit != null) {
                contract = getContract(unit.getMonographPage());
            }
        }
        getConfig().setContract(contract);

        for (MonographUnit unit : mono.getMonographUnit()) {
            this.convertUnit(unit, visibility);
            re.addRelation(RelsExt.HAS_UNIT,pid( uuid(unit.getUniqueIdentifier())), false);
        }

        Map<Integer, String> pageIdMap = new TreeMap<Integer, String>();
        for (MonographPage page : mono.getMonographPage()) {
            this.convertPage(page, visibility);

            String ppid = pid(uuid(page.getUniqueIdentifier()));
            re.addRelation(RelsExt.HAS_PAGE, ppid, false);
            fillPageIdMap(pageIdMap, page.getIndex(), ppid);
        }

        for (MonographComponentPart part : mono.getMonographComponentPart()) {
            this.convertPart(part, pageIdMap, visibility);
            re.addRelation(RelsExt.HAS_INT_COMP_PART,pid( uuid(part.getUniqueIdentifier())), false);
        }

        addDonatorRelation(re, biblio.getCreator());

        DublinCore dc = this.createMonographDublinCore(pid, title, biblio.getCreator(), biblio.getPublisher(), biblio.getContributor());

        dc.addQualifiedIdentifier(RelsExt.CONTRACT, contract);
        re.addRelation(RelsExt.CONTRACT, contract, true);

        String ISBN = biblio.getISBN() == null ? null : first(biblio.getISBN().getContent());
        dc.addQualifiedIdentifier(RelsExt.ISBN, ISBN);
        if (ISBN == null || "".equals(ISBN)) {
            dc.addQualifiedIdentifier(RelsExt.EXTID, convertExtId(uuid));
        }
        for (Subject subj : biblio.getSubject()) {
            if (subj.getDDC() != null) {
                for (String ddc : subj.getDDC().getContent()) {
                    dc.addSubject("ddc:" + ddc);
                }
            }
            if (subj.getUDC() != null) {
                for (String udc : subj.getUDC().getContent()) {
                    dc.addSubject("udc:" + udc);
                }
            }
        }
        convertHandle(uuid, dc, re);

        dc.setDescription(biblio.getAnnotation() == null? null:concat(biblio.getAnnotation().getContent()));
        Publisher publ = firstItem(biblio.getPublisher());
        if (publ!= null){
            dc.setDate(publ.getDateOfPublication()==null?null:first(publ.getDateOfPublication().getContent()));
        }

        dc.setType(MODEL_MONOGRAPH);

        Language lang = firstItem(biblio.getLanguage());
        if (lang != null){
            dc.setLanguage(first(lang.getContent()));
        }


        FileDescriptor[] files = new FileDescriptor[1];
        if (mono.getTechnicalDescription() != null) {
          //TODO files[0] = this.createImageRepresentation(null, mono.getTechnicalDescription(), null);
        }

        //TODO DigitalObject foxmlMono = this.createDigitalObject(mono, pid, title, dc, re, XSL_MODS_MONOGRAPH, files, visibility);

      //TODO this.marshalDigitalObject(foxmlMono);

    }

    private String convertExtId(String pid) {
        Connection con = config.getDbConnection();
        if (con != null) {
            Statement st = null;
            ResultSet rs = null;
            try {
                st = con.createStatement();
                rs = st.executeQuery("select id from m_monograph  where ui_uniqueidentifierurntype = \'" + pid + "\'");
                if (rs.next()) {
                    return rs.getString(1);
                }
            } catch (SQLException ex) {
                log.error("Error in reading visibility", ex);
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (st != null) {
                        st.close();
                    }
                    // con.close(); connection will be closed in the Main class
                } catch (SQLException eex) {

                }
            }

        }
        return null;
    }

    private void addDonatorRelation(RelsExt re, List<Creator> creators) {
        if (creators != null) {
            for (Creator creator : creators) {
                if (DONATOR_ID.equals(creator.getCreatorSurname()== null?"":first(creator.getCreatorSurname().getContent()))) {
                    re.addRelation(RelsExt.HAS_DONATOR, DONATOR_PID, false);
                }
            }
        }
    }

    /**
     * Konvertuje stranku monografie do foxml
     *
     * @param page
     * @param monograph
     * @param prefix
     * @param foxmlMono
     * @throws ServiceException
     */
    private void convertPage(MonographPage page, boolean visibility) throws ServiceException {
        String title = first(page.getPageNumber().get(0).getContent());
        // String title = page.getIndex();
        if (page.getUniqueIdentifier() == null) {
            page.setUniqueIdentifier(new UniqueIdentifier());
        }
        String uuid = uuid(page.getUniqueIdentifier());
        String pid = pid(uuid);

        RelsExt re = new RelsExt(pid, MODEL_PAGE);

        List<FileDescriptor> files = new ArrayList<FileDescriptor>(2);
        for (PageRepresentation r : page.getPageRepresentation()) {
            if (r.getPageImage() != null) {
              //TODO files.add(this.createImageRepresentation(r.getPageImage().getHref(), r.getTechnicalDescription(), r.getUniqueIdentifier()));
                re.addRelation(RelsExt.FILE, r.getPageImage().getHref(), true);
            }
            if (r.getPageText() != null) {
              //TODO files.add(this.createImageRepresentation(r.getPageText().getHref(), r.getTechnicalDescription(), r.getUniqueIdentifier()));
            }
        }

        DublinCore dc = this.createMonographDublinCore(pid, title, null, null, null);
        convertHandle(uuid, dc, re);
        dc.setType(MODEL_PAGE);

      //TODO DigitalObject foxmlPage = this.createDigitalObject(page, pid, title, dc, re, XSL_MODS_MONOGRAPH_PAGE, files.toArray(new ImageRepresentation[files.size()]), visibility);

      //TODO this.marshalDigitalObject(foxmlPage);
    }

    /**
     * Konvertuje monograph unit do foxml
     *
     * @param unit
     * @param monograph
     * @param prefix
     * @param foxmlMono
     * @throws ServiceException
     */
    private void convertUnit(MonographUnit unit, boolean parentVisibility) throws ServiceException {
        String title = first((unit.getTitle() == null || unit.getTitle().getMainTitle() == null) ? null : unit.getTitle().getMainTitle().getContent());
        if (unit.getUniqueIdentifier() == null) {
            unit.setUniqueIdentifier(new UniqueIdentifier());
        }
        String uuid = uuid(unit.getUniqueIdentifier());
        String pid = pid(uuid);
        boolean visibility = isPublic(uuid, parentVisibility, "m_monographunit");
        List<FileDescriptor> files = new ArrayList<FileDescriptor>(2);
        for (MonographUnitRepresentation r : unit.getMonographUnitRepresentation()) {
            if (r.getUniqueIdentifier() != null) {
                log.warn(WARN_MUR_EMPTY_UID + ": pid=" + pid);
            }
            TechnicalDescription td = unit.getTechnicalDescription() != null ? unit.getTechnicalDescription() : r.getTechnicalDescription();

            if (unit.getTechnicalDescription() != null && r.getTechnicalDescription() != null) {
                log.warn("Duplicate TechnicalDescription: pid=" + pid);
            }
            if (r.getUnitImage() != null) {
              //TODO files.add(this.createImageRepresentation(r.getUnitImage().getHref(), td, null));
            }
            if (r.getUnitText() != null) {
              //TODO files.add(this.createImageRepresentation(r.getUnitText().getHref(), td, null));
            }
        }

        RelsExt re = new RelsExt(pid, MODEL_MONOGRAPH_UNIT);

        Map<Integer, String> pageIdMap = new TreeMap<Integer, String>();
        for (MonographPage page : unit.getMonographPage()) {
            this.convertPage(page, visibility);

            String ppid = pid(uuid(page.getUniqueIdentifier()));
            re.addRelation(RelsExt.HAS_PAGE, ppid, false);
            fillPageIdMap(pageIdMap, page.getIndex(), ppid);
        }

        for (MonographComponentPart part : unit.getMonographComponentPart()) {
            this.convertPart(part, pageIdMap, visibility);
            re.addRelation(RelsExt.HAS_INT_COMP_PART, pid(uuid(part.getUniqueIdentifier())), false);
        }

        DublinCore dc = this.createMonographDublinCore(pid, title, unit.getCreator(), unit.getPublisher(), unit.getContributor());
        String contract = getContract(unit.getMonographPage());
        dc.addQualifiedIdentifier(RelsExt.CONTRACT, contract);
        re.addRelation(RelsExt.CONTRACT, contract, true);

        convertHandle(uuid, dc, re);
        dc.setType(MODEL_MONOGRAPH_UNIT);
        Language lang = firstItem(unit.getLanguage());
        if (lang != null){
            dc.setLanguage(first(lang.getContent()));
        }
        if (unit.getMonographUnitIdentification() != null && unit.getMonographUnitIdentification().getMonographUnitNumber() != null){
            dc.setDescription(concat(unit.getMonographUnitIdentification().getMonographUnitNumber().getContent()));
        }
        Publisher publ = firstItem(unit.getPublisher());
        if (publ!= null){
            dc.setDate(publ.getDateOfPublication()==null?null:first(publ.getDateOfPublication().getContent()));
        }
      //TODO DigitalObject foxmlUnit = this.createDigitalObject(unit, pid, title, dc, re, XSL_MODS_MONOGRAPH_UNIT, files.toArray(new ImageRepresentation[files.size()]), visibility);

      //TODO this.marshalDigitalObject(foxmlUnit);
    }



    /**
     * Konvertuje MonographComponentPart do foxml
     *
     * @param part
     * @throws ServiceException
     */
    private void convertPart(MonographComponentPart part, Map<Integer, String> pageIdMap, boolean visibility) throws ServiceException {
        if (part.getUniqueIdentifier() == null) {
            part.setUniqueIdentifier(new UniqueIdentifier());
        }
        String uuid = uuid(part.getUniqueIdentifier());
        String pid = pid(uuid);
        //String title = first(part.getPageNumber().getContent());
        String title = first((part.getTitle() == null || part.getTitle().getMainTitle() == null) ? null : part.getTitle().getMainTitle().getContent());
        FileDescriptor[] binaryObjects = this.getComponentPartBinaryObjects(part.getMonographComponentPartRepresentation());

        RelsExt re = new RelsExt(pid, MODEL_INTERNAL_PART);

        List<PageIndex> pageIndex = part.getPages() != null ? part.getPages().getPageIndex() : null;
        if (pageIndex != null && !part.getPages().getPageIndex().isEmpty()) {
            for (PageIndex pi : pageIndex) {
                Integer piFrom = Integer.valueOf(pi.getFrom());
                Integer piTo = Integer.valueOf(pi.getTo());
                this.processPageIndex(re, piFrom, piTo, pageIdMap);
            }
        }

        DublinCore dc = this.createMonographDublinCore(pid, title, part.getCreator(), null, part.getContributor());
        convertHandle(uuid, dc, re);
        dc.setType(MODEL_INTERNAL_PART);
        Language lang = firstItem(part.getLanguage());
        if (lang != null){
            dc.setLanguage(first(lang.getContent()));
        }
      //TODO DigitalObject foxmlPart = this.createDigitalObject(part, pid, title, dc, re, XSL_MODS_MONOGRAPH_PART, binaryObjects, visibility);

      //TODO this.marshalDigitalObject(foxmlPart);
    }

    private FileDescriptor[] getComponentPartBinaryObjects(MonographComponentPartRepresentation representation) {
        if (representation != null) {
            FileDescriptor image = null;
            PartInImage pii = representation.getPartInImage();
            if (pii != null) {
              //TODO image = this.createImageRepresentation(pii.getHref(), representation.getTechnicalDescription(), representation.getUniqueIdentifier());
            }
            FileDescriptor text = null;
            PartInText pit = representation.getPartInText();
            if (pit != null) {
              //TODO  text = this.createImageRepresentation(pit.getHref(), null, representation.getUniqueIdentifier());
            }

            return new FileDescriptor[] { image, text };
        } else {
            return null;
        }
    }

    /**
     * Naplni dublin core data z monographu
     *
     * @param biblio
     * @return
     */
    private DublinCore createMonographDublinCore(String pid, String title, List<Creator> creator, List<Publisher> publisher, List<Contributor> contributor) {

        DublinCore dc = new DublinCore();
        dc.setTitle(title);
        dc.addIdentifier(pid);

        dc.setCreator(new ArrayList<String>());
        if (creator != null) {
            for (Creator c : creator) {
                if (DONATOR_ID.equals(c.getCreatorSurname()== null?"":first(c.getCreatorSurname().getContent())) ) {
                    continue;
                }
                StringBuffer s = new StringBuffer();
                s.append(first(c.getCreatorSurname().getContent()));
                for (CreatorName name : c.getCreatorName()) {
                    s.append(" " + first(name.getContent()));
                }
                dc.getCreator().add(s.toString());
            }
        }

        dc.setPublisher(new ArrayList<String>());
        if (publisher != null) {
            for (Publisher p : publisher) {
                dc.getPublisher().add(first(p.getPublisherName().getContent()));
            }
        }

        dc.setContributor(new ArrayList<String>());
        if (contributor != null) {
            for (Contributor c : contributor) {
                StringBuffer s = new StringBuffer();
                s.append(first(c.getContributorSurname().getContent()));
                for (ContributorName name : c.getContributorName()) {
                    s.append(" " + first(name.getContent()));
                }
                dc.getContributor().add(s.toString());
            }
        }
        return dc;
    }


    private String getContract(List<MonographPage> pages) {
        if (pages != null) {
            for (MonographPage page : pages) {
                List<PageRepresentation> reps = page.getPageRepresentation();
                if (reps != null) {
                    for (PageRepresentation rep : reps) {
                        if (rep.getPageImage() != null) {
                            String filename = removeSigla(rep.getPageImage().getHref());
                            if (filename != null) {
                                int length = config.getContractLength();
                                if (length > filename.length()) {
                                    return filename;
                                } else {
                                    return filename.substring(0, length);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
