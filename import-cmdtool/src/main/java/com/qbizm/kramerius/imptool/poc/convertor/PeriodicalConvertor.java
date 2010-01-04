package com.qbizm.kramerius.imptool.poc.convertor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import com.qbizm.kramerius.imp.jaxb.periodical.Contributor;
import com.qbizm.kramerius.imp.jaxb.periodical.ContributorName;
import com.qbizm.kramerius.imp.jaxb.periodical.CoreBibliographicDescriptionPeriodical;
import com.qbizm.kramerius.imp.jaxb.periodical.Creator;
import com.qbizm.kramerius.imp.jaxb.periodical.CreatorName;
import com.qbizm.kramerius.imp.jaxb.periodical.ItemRepresentation;
import com.qbizm.kramerius.imp.jaxb.periodical.MainTitle;
import com.qbizm.kramerius.imp.jaxb.periodical.PageIndex;
import com.qbizm.kramerius.imp.jaxb.periodical.PageRepresentation;
import com.qbizm.kramerius.imp.jaxb.periodical.Periodical;
import com.qbizm.kramerius.imp.jaxb.periodical.PeriodicalInternalComponentPart;
import com.qbizm.kramerius.imp.jaxb.periodical.PeriodicalItem;
import com.qbizm.kramerius.imp.jaxb.periodical.PeriodicalPage;
import com.qbizm.kramerius.imp.jaxb.periodical.PeriodicalVolume;
import com.qbizm.kramerius.imp.jaxb.periodical.Publisher;
import com.qbizm.kramerius.imp.jaxb.periodical.TechnicalDescription;
import com.qbizm.kramerius.imp.jaxb.periodical.UniqueIdentifier;
import com.qbizm.kramerius.imp.jaxb.periodical.UniqueIdentifierURNType;
import com.qbizm.kramerius.imptool.poc.valueobj.ConvertorConfig;
import com.qbizm.kramerius.imptool.poc.valueobj.DublinCore;
import com.qbizm.kramerius.imptool.poc.valueobj.ImageMetaData;
import com.qbizm.kramerius.imptool.poc.valueobj.ImageRepresentation;
import com.qbizm.kramerius.imptool.poc.valueobj.RelsExt;
import com.qbizm.kramerius.imptool.poc.valueobj.ServiceException;

/**
 * Konvertor periodika do sady foxml digitalnich objektu
 * 
 * @author xholcik
 */
public class PeriodicalConvertor extends BaseConvertor {

    /**
     * XSL transformacni sablony
     */
    private static final String XSL_MODS_PERIODICAL = "model_periodical_MODS.xsl";

    private static final String XSL_MODS_PERIODICAL_PART = "model_periodicalInternalComponentPart-MODS.xsl";

    private static final String XSL_MODS_PERIODICAL_ITEM = "model_periodicalItem_MODS.xsl";

    private static final String XSL_MODS_PERIODICAL_PAGE = "model_periodicalPage-MODS.xsl";

    private static final String XSL_MODS_PERIODICAL_VOLUME = "model_periodicalVolume_MODS.xsl";

    public PeriodicalConvertor(ConvertorConfig config) throws ServiceException {
        super(config);
    }

    /**
     * Pomocna metoda pro ziskani pid objektu
     * 
     * @param uid
     * @return
     */
    private String pid(UniqueIdentifier uid) throws ServiceException {
        String pid;
        if (uid == null || uid.getUniqueIdentifierURNType() == null || !Pattern.matches(PID_PATTERN, PID_PREFIX + first(uid.getUniqueIdentifierURNType().getContent()))) {
            pid = generateUUID();
            if (uid.getUniqueIdentifierURNType() == null) {
                uid.setUniqueIdentifierURNType(new UniqueIdentifierURNType());
            }
            uid.getUniqueIdentifierURNType().getContent().add(pid);
        } else {
            pid = first(uid.getUniqueIdentifierURNType().getContent());
        }

        return PID_PREFIX + pid;
    }

    /**
     * Obskurni metoda pro ziskani nazvu z obskurniho objektu
     * 
     * @param biblio
     * @return
     */
    private String getMainTitle(CoreBibliographicDescriptionPeriodical biblio) {
        if (biblio != null && biblio.getTitle() != null) {
            for (Object o : biblio.getTitle().getMainTitleAndSubTitleAndParallelTitle()) {
                if (o instanceof MainTitle) {
                    return first(((MainTitle) o).getContent());
                }
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Prevede periodikum a vsechny navazane objekty do sady foxml souboru
     * 
     * @param peri
     * @throws ServiceException
     */
    public void convert(Periodical peri) throws ServiceException {
        CoreBibliographicDescriptionPeriodical biblio = peri.getCoreBibliographicDescriptionPeriodical();
        String title = getMainTitle(biblio);
        if (peri.getUniqueIdentifier() == null) {
            peri.setUniqueIdentifier(new UniqueIdentifier());
        }
        String pid = pid(peri.getUniqueIdentifier());

        RelsExt re = new RelsExt(pid, MODEL_PERIODICAL);

        for (PeriodicalVolume volume : peri.getPeriodicalVolume()) {
            this.convertVolume(volume);
            re.addRelation(RelsExt.HAS_VOLUME, pid(volume.getUniqueIdentifier()));
        }

        DigitalObject foxmlPeri = this.createDigitalObject(peri, pid, title, biblio, re, XSL_MODS_PERIODICAL, null);

        this.marshalDigitalObject(foxmlPeri);
    }

    /**
     * Prevede PeriodicalVolume do foxml
     * 
     * @param volume
     * @param prefix
     * @throws ServiceException
     */
    private void convertVolume(PeriodicalVolume volume) throws ServiceException {
        CoreBibliographicDescriptionPeriodical biblio = volume.getCoreBibliographicDescriptionPeriodical();
        String title = "";
        if (volume.getPeriodicalVolumeIdentification() != null && volume.getPeriodicalVolumeIdentification().getPeriodicalVolumeNumber() != null) {
            title = first(volume.getPeriodicalVolumeIdentification().getPeriodicalVolumeNumber().getContent());
        }
        if (volume.getUniqueIdentifier() == null) {
            volume.setUniqueIdentifier(new UniqueIdentifier());
        }
        String pid = pid(volume.getUniqueIdentifier());

        RelsExt re = new RelsExt(pid, MODEL_PERIODICAL_VOLUME);

        for (PeriodicalItem item : volume.getPeriodicalItem()) {
            this.convertItem(item);
            re.addRelation(RelsExt.HAS_ITEM, pid(item.getUniqueIdentifier()));
        }

        Map<String, String> pageIdMap = new TreeMap<String, String>();
        for (PeriodicalPage page : volume.getPeriodicalPage()) {
            this.convertPage(page);

            String ppid = pid(page.getUniqueIdentifier());
            re.addRelation(RelsExt.HAS_PAGE, ppid);
            if (page.getIndex() != null) {
                pageIdMap.put(page.getIndex(), ppid);
            } else {
                log.warn(WARN_PAGE_INDEX);
            }
        }

        for (PeriodicalInternalComponentPart part : volume.getPeriodicalInternalComponentPart()) {
            this.convertInternalPart(part, pageIdMap);
            re.addRelation(RelsExt.HAS_INT_COMP_PART, pid(part.getUniqueIdentifier()));
        }

        DigitalObject foxmlVolume = this.createDigitalObject(volume, pid, title, biblio, re, XSL_MODS_PERIODICAL_VOLUME, null);

        this.marshalDigitalObject(foxmlVolume);
    }

    /**
     * Prevede PeriodicalInternalComponentPart do foxml
     * 
     * @param part
     * @param prefix
     * @throws ServiceException
     */
    private void convertInternalPart(PeriodicalInternalComponentPart part, Map<String, String> pageIdMap) throws ServiceException {
        CoreBibliographicDescriptionPeriodical biblio = part.getCoreBibliographicDescriptionPeriodical();
        String title = getMainTitle(biblio);
        if (part.getUniqueIdentifier() == null) {
            part.setUniqueIdentifier(new UniqueIdentifier());
        }
        String pid = pid(part.getUniqueIdentifier());

        RelsExt re = new RelsExt(pid, MODEL_INTERNAL_PART);

        List<PageIndex> pageIndex = part.getPages().getPageIndex();
        if (pageIndex != null && !part.getPages().getPageIndex().isEmpty()) {
            for (PageIndex pi : pageIndex) {
                Integer piFrom = Integer.valueOf(pi.getFrom());
                Integer piTo = Integer.valueOf(pi.getTo());
                this.processPageIndex(re, piFrom, piTo, pageIdMap);
            }
        }

        DigitalObject foxmlPart = this.createDigitalObject(part, pid, title, biblio, re, XSL_MODS_PERIODICAL_PART, null);

        this.marshalDigitalObject(foxmlPart);
    }

    /**
     * Prevede PeriodicalItem do foxml
     * 
     * @param item
     * @param prefix
     * @throws ServiceException
     */
    private void convertItem(PeriodicalItem item) throws ServiceException {
        CoreBibliographicDescriptionPeriodical biblio = item.getCoreBibliographicDescriptionPeriodical();
        String title = getMainTitle(biblio);
        if (item.getUniqueIdentifier() == null) {
            item.setUniqueIdentifier(new UniqueIdentifier());
        }
        String pid = pid(item.getUniqueIdentifier());

        List<ImageRepresentation> files = new ArrayList<ImageRepresentation>(2);
        if (item.getItemRepresentation() != null) {
            ItemRepresentation r = item.getItemRepresentation();
            if (item.getItemRepresentation().getItemImage() != null) {
                files.add(this.createImageRepresentation(r.getItemImage().getHref(), r.getTechnicalDescription(), item.getItemRepresentation().getUniqueIdentifier()));
            }
            if (item.getItemRepresentation().getItemText() != null) {
                files.add(this.createImageRepresentation(r.getItemText().getHref(), null, item.getItemRepresentation().getUniqueIdentifier()));
            }
        }

        RelsExt re = new RelsExt(pid, MODEL_PERIODICAL_ITEM);

        Map<String, String> pageIdMap = new TreeMap<String, String>();
        for (PeriodicalPage page : item.getPeriodicalPage()) {
            this.convertPage(page);

            String ppid = pid(page.getUniqueIdentifier());
            re.addRelation(RelsExt.HAS_PAGE, pid(page.getUniqueIdentifier()));
            if (page.getIndex() != null) {
                pageIdMap.put(page.getIndex(), ppid);
            } else {
                log.warn(WARN_PAGE_INDEX);
            }
        }

        for (PeriodicalInternalComponentPart part : item.getPeriodicalInternalComponentPart()) {
            this.convertInternalPart(part, pageIdMap);
            re.addRelation(RelsExt.HAS_INT_COMP_PART, pid(part.getUniqueIdentifier()));
        }

        DigitalObject foxmlItem = this.createDigitalObject(item, pid, title, biblio, re, XSL_MODS_PERIODICAL_ITEM, files.toArray(new ImageRepresentation[files.size()]));

        this.marshalDigitalObject(foxmlItem);
    }

    /**
     * Prevede stranku periodika do foxml
     * 
     * @param page
     * @throws ServiceException
     */
    private void convertPage(PeriodicalPage page) throws ServiceException {
        String title = first(page.getPageNumber().get(0).getContent());
        // String title = page.getIndex();
        if (page.getUniqueIdentifier() == null) {
            page.setUniqueIdentifier(new UniqueIdentifier());
        }
        String pid = pid(page.getUniqueIdentifier());

        List<ImageRepresentation> files = new ArrayList<ImageRepresentation>(2);
        for (PageRepresentation r : page.getPageRepresentation()) {
            if (r.getPageImage() != null) {
                files.add(this.createImageRepresentation(r.getPageImage().getHref(), r.getTechnicalDescription(), r.getUniqueIdentifier()));
            }
            if (r.getPageText() != null) {
                files.add(this.createImageRepresentation(r.getPageText().getHref(), null, r.getUniqueIdentifier()));
            }
        }

        RelsExt re = new RelsExt(pid, MODEL_PAGE);

        DigitalObject foxmlPage = this.createDigitalObject(page, pid, title, null, null, null, re, XSL_MODS_PERIODICAL_PAGE, files.toArray(new ImageRepresentation[files.size()]));

        this.marshalDigitalObject(foxmlPage);
    }

    /**
     * Vytvori a naplni DublinCore data pro DC datastream
     * 
     * @param biblio
     * @return
     */
    private DublinCore createPeriodicalDublinCore(String title, List<Creator> creator, List<Publisher> publisher, List<Contributor> contributor) {
        DublinCore dc = new DublinCore();

        dc.setTitle(title);

        dc.setCreator(new ArrayList<String>());
        if (creator != null) {
            for (Creator c : creator) {
                StringBuffer s = new StringBuffer();
                s.append(c.getCreatorSurname());
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

    /**
     * Provede spolecne operace nad objekty periodik - zjednoduseni nekterych
     * parametru pres CoreBibliographicDescriptionPeriodical
     * 
     * @param sourceObject
     * @param pid
     * @param title
     * @param biblio
     * @param re
     * @param xslFile
     * @param files
     * @param foxmlObject
     * @throws ServiceException
     */
    private DigitalObject createDigitalObject(Object sourceObject, String pid, String title, CoreBibliographicDescriptionPeriodical biblio, RelsExt re, String xslFile,
            ImageRepresentation[] files) throws ServiceException {

        if (biblio == null) {
            biblio = new CoreBibliographicDescriptionPeriodical();
        }

        return this.createDigitalObject(sourceObject, pid, title, biblio.getCreator(), biblio.getPublisher(), biblio.getContributor(), re, xslFile, files);
    }

    /**
     * Vytvori digitalni objekt dle zadanych parametru vcetne datastreamu
     * 
     * @param sourceObject
     * @param pid
     * @param title
     * @param creator
     * @param publisher
     * @param contributor
     * @param re
     * @param xslFile
     * @param files
     * @param foxmlObject
     * @throws ServiceException
     */
    private DigitalObject createDigitalObject(Object sourceObject, String pid, String title, List<Creator> creator, List<Publisher> publisher, List<Contributor> contributor,
            RelsExt re, String xslFile, ImageRepresentation[] files) throws ServiceException {

        if (log.isInfoEnabled()) {
            log.info(sourceObject.getClass().getSimpleName() + ": title=" + title + "; pid=" + pid);
        }

        DigitalObject foxmlObject = new DigitalObject();

        this.setCommonProperties(foxmlObject, pid, title);

        DublinCore dc = this.createPeriodicalDublinCore(title, creator, publisher, contributor);

        this.setCommonStreams(foxmlObject, sourceObject, dc, re, xslFile, files);

        return foxmlObject;
    }

    private ImageRepresentation createImageRepresentation(String filename, TechnicalDescription td, UniqueIdentifier ui) {
        ImageRepresentation ir = new ImageRepresentation();
        ir.setFilename(filename);

        ImageMetaData ad = new ImageMetaData();
        ir.setImageMetaData(ad);

        if (td != null) {
            ad.setScanningDevice(first(td.getScanningDevice().getContent()));
            ad.setScanningParameters(first(td.getScanningParameters().getContent()));
            ad.setOtherImagingInformation(first(td.getOtherImagingInformation().getContent()));
        }

        if (ui != null) {
            if (ui.getUniqueIdentifierURNType() != null) {
                ad.setUrn(first(ui.getUniqueIdentifierURNType().getContent()));
            }
            if (ui.getUniqueIdentifierSICIType() != null) {
                ad.setSici(first(ui.getUniqueIdentifierSICIType().getContent()));
            }
        }

        return ir;
    }

}
