package com.qbizm.kramerius.imptool.poc.convertor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.qbizm.kramerius.imp.jaxb.Contributor;
import com.qbizm.kramerius.imp.jaxb.ContributorName;
import com.qbizm.kramerius.imp.jaxb.Creator;
import com.qbizm.kramerius.imp.jaxb.CreatorName;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import com.qbizm.kramerius.imp.jaxb.Monograph;
import com.qbizm.kramerius.imp.jaxb.MonographBibliographicRecord;
import com.qbizm.kramerius.imp.jaxb.MonographComponentPart;
import com.qbizm.kramerius.imp.jaxb.MonographComponentPartRepresentation;
import com.qbizm.kramerius.imp.jaxb.MonographPage;
import com.qbizm.kramerius.imp.jaxb.MonographUnit;
import com.qbizm.kramerius.imp.jaxb.MonographUnitRepresentation;
import com.qbizm.kramerius.imp.jaxb.PageIndex;
import com.qbizm.kramerius.imp.jaxb.PageRepresentation;
import com.qbizm.kramerius.imp.jaxb.PartInImage;
import com.qbizm.kramerius.imp.jaxb.PartInText;
import com.qbizm.kramerius.imp.jaxb.Publisher;
import com.qbizm.kramerius.imp.jaxb.TechnicalDescription;
import com.qbizm.kramerius.imp.jaxb.UniqueIdentifier;
import com.qbizm.kramerius.imp.jaxb.UniqueIdentifierURNType;
import com.qbizm.kramerius.imptool.poc.valueobj.ConvertorConfig;
import com.qbizm.kramerius.imptool.poc.valueobj.DublinCore;
import com.qbizm.kramerius.imptool.poc.valueobj.ImageMetaData;
import com.qbizm.kramerius.imptool.poc.valueobj.ImageRepresentation;
import com.qbizm.kramerius.imptool.poc.valueobj.RelsExt;
import com.qbizm.kramerius.imptool.poc.valueobj.ServiceException;

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

  public MonographConvertor(ConvertorConfig config) throws ServiceException {
    super(config);
  }

  /**
   * @param uid
   * @return
   * @throws ServiceException
   */
  private String pid(UniqueIdentifier uid) throws ServiceException {
    String pid;
    if (uid == null
      || uid.getUniqueIdentifierURNType() == null
      || !Pattern.matches(PID_PATTERN, PID_PREFIX
        + first(uid.getUniqueIdentifierURNType().getContent()))) {
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
   * Konvertuje monografii a vsechny podobjekty do sady foxml souboru
   * 
   * @param mono
   * @throws ServiceException
   */
  public void convert(Monograph mono) throws ServiceException {
    MonographBibliographicRecord biblio = mono.getMonographBibliographicRecord();
    String title = first(biblio.getTitle().getMainTitle().getContent());
    if (mono.getUniqueIdentifier() == null) {
      mono.setUniqueIdentifier(new UniqueIdentifier());
    }    
    String pid = pid(mono.getUniqueIdentifier());

    // neplatny vstupni objekt
    if (mono.getMonographBibliographicRecord().getSeries() != null
      && mono.getMonographBibliographicRecord().getSeries().size() > 1) {
      throw new IllegalArgumentException(
        "Illegal multiple /Monograph/MonographBibliographicRecord/Series occurence!");
    }

    RelsExt re = new RelsExt(pid, MODEL_MONOGRAPH);

    for (MonographUnit unit : mono.getMonographUnit()) {
      this.convertUnit(unit);
      re.addRelation(RelsExt.HAS_UNIT, pid(unit.getUniqueIdentifier()));
    }

    Map<String, String> pageIdMap = new TreeMap<String, String>();
    for (MonographPage page : mono.getMonographPage()) {
      this.convertPage(page);

      String ppid = pid(page.getUniqueIdentifier());
      re.addRelation(RelsExt.HAS_PAGE, ppid);
      if (page.getIndex() != null) {
        pageIdMap.put(page.getIndex(), ppid);
      } else {
        log.warn(WARN_PAGE_INDEX);
      }
    }

    for (MonographComponentPart part : mono.getMonographComponentPart()) {
      this.convertPart(part, pageIdMap);
      re.addRelation(RelsExt.HAS_INT_COMP_PART, pid(part.getUniqueIdentifier()));
    }

    DigitalObject foxmlMono = this.createDigitalObject(
      mono,
      pid,
      title,
      biblio.getCreator(),
      biblio.getPublisher(),
      biblio.getContributor(),
      re,
      XSL_MODS_MONOGRAPH,
      null);

    this.marshalDigitalObject(foxmlMono);
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
  private void convertPage(MonographPage page) throws ServiceException {
    String title = first(page.getPageNumber().get(0).getContent());
    // String title = page.getIndex();
    if (page.getUniqueIdentifier() == null) {
      page.setUniqueIdentifier(new UniqueIdentifier());
    }
    String pid = pid(page.getUniqueIdentifier());

    List<ImageRepresentation> files = new ArrayList<ImageRepresentation>(2);
    for (PageRepresentation r : page.getPageRepresentation()) {
      if (r.getPageImage() != null) {
        files.add(this.createImageRepresentation(
          r.getPageImage().getHref(),
          r.getTechnicalDescription(),
          r.getUniqueIdentifier()));
      }
      if (r.getPageText() != null) {
        files.add(this.createImageRepresentation(
          r.getPageText().getHref(),
          r.getTechnicalDescription(),
          r.getUniqueIdentifier()));
      }
    }

    RelsExt re = new RelsExt(pid, MODEL_PAGE);

    DigitalObject foxmlPage = this.createDigitalObject(
      page,
      pid,
      title,
      null,
      null,
      null,
      re,
      XSL_MODS_MONOGRAPH_PAGE,
      files.toArray(new ImageRepresentation[files.size()]));

    this.marshalDigitalObject(foxmlPage);
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
  private void convertUnit(MonographUnit unit) throws ServiceException {
    String title = first(unit.getTitle().getMainTitle().getContent());
    if (unit.getUniqueIdentifier() == null) {
      unit.setUniqueIdentifier(new UniqueIdentifier());
    }
    String pid = pid(unit.getUniqueIdentifier());

    List<ImageRepresentation> files = new ArrayList<ImageRepresentation>(2);
    for (MonographUnitRepresentation r : unit.getMonographUnitRepresentation()) {
      if (r.getUniqueIdentifier() != null) {
        log.warn(WARN_MUR_EMPTY_UID + ": pid=" + pid);
      }
      TechnicalDescription td = unit.getTechnicalDescription() != null ? unit.getTechnicalDescription()
        : r.getTechnicalDescription();

      if (unit.getTechnicalDescription() != null && r.getTechnicalDescription() != null) {
        log.warn("Duplicate TechnicalDescription: pid=" + pid);
      }
      if (r.getUnitImage() != null) {
        files.add(this.createImageRepresentation(r.getUnitImage().getHref(), td, null));
      }
      if (r.getUnitText() != null) {
        files.add(this.createImageRepresentation(r.getUnitText().getHref(), td, null));
      }
    }

    RelsExt re = new RelsExt(pid, MODEL_MONOGRAPH_UNIT);

    Map<String, String> pageIdMap = new TreeMap<String, String>();
    for (MonographPage page : unit.getMonographPage()) {
      this.convertPage(page);

      String ppid = pid(page.getUniqueIdentifier());
      re.addRelation(RelsExt.HAS_PAGE, ppid);
      if (page.getIndex() != null) {
        pageIdMap.put(page.getIndex(), ppid);
      } else {
        log.warn("Page index missing! Data inconsistency warning!!");
      }
    }

    for (MonographComponentPart part : unit.getMonographComponentPart()) {
      this.convertPart(part, pageIdMap);
      re.addRelation(RelsExt.HAS_INT_COMP_PART, pid(part.getUniqueIdentifier()));
    }

    DigitalObject foxmlUnit = this.createDigitalObject(
      unit,
      pid,
      title,
      unit.getCreator(),
      unit.getPublisher(),
      unit.getContributor(),
      re,
      XSL_MODS_MONOGRAPH_UNIT,
      files.toArray(new ImageRepresentation[files.size()]));

    this.marshalDigitalObject(foxmlUnit);
  }

  /**
   * Konvertuje MonographComponentPart do foxml
   * 
   * @param part
   * @throws ServiceException
   */
  private void convertPart(MonographComponentPart part, Map<String, String> pageIdMap)
    throws ServiceException {
    if (part.getUniqueIdentifier() == null) {
      part.setUniqueIdentifier(new UniqueIdentifier());
    }    
    String pid = pid(part.getUniqueIdentifier());
    String title = first(part.getPageNumber().getContent());

    ImageRepresentation[] binaryObjects = this.getComponentPartBinaryObjects(part.getMonographComponentPartRepresentation());

    RelsExt re = new RelsExt(pid, MODEL_INTERNAL_PART);

    List<PageIndex> pageIndex = part.getPages().getPageIndex();
    if (pageIndex != null && !part.getPages().getPageIndex().isEmpty()) {
      for (PageIndex pi : pageIndex) {
        Integer piFrom = Integer.valueOf(pi.getFrom());
        Integer piTo = Integer.valueOf(pi.getTo());
        this.processPageIndex(re, piFrom, piTo, pageIdMap);
      }
    }

    DigitalObject foxmlPart = this.createDigitalObject(
      part,
      pid,
      title,
      part.getCreator(),
      null,
      part.getContributor(),
      re,
      XSL_MODS_MONOGRAPH_PART,
      binaryObjects);

    this.marshalDigitalObject(foxmlPart);
  }

  private ImageRepresentation[] getComponentPartBinaryObjects(
    MonographComponentPartRepresentation representation) {
    if (representation != null) {
      ImageRepresentation image = null;
      PartInImage pii = representation.getPartInImage();
      if (pii != null) {
        image = this.createImageRepresentation(
          pii.getHref(),
          representation.getTechnicalDescription(),
          representation.getUniqueIdentifier());
      }
      ImageRepresentation text = null;
      PartInText pit = representation.getPartInText();
      if (pit != null) {
        text = this.createImageRepresentation(
          pit.getHref(),
          null,
          representation.getUniqueIdentifier());
      }

      return new ImageRepresentation[] { image, text};
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
  private DublinCore createMonographDublinCore(
    String title,
    List<Creator> creator,
    List<Publisher> publisher,
    List<Contributor> contributor) {

    DublinCore dc = new DublinCore();
    dc.setTitle(title);

    dc.setCreator(new ArrayList<String>());
    if (creator != null) {
      for (Creator c : creator) {
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
   * @param file
   * @return
   * @throws ServiceException
   */
  private DigitalObject createDigitalObject(
    Object sourceObject,
    String pid,
    String title,
    List<Creator> creator,
    List<Publisher> publisher,
    List<Contributor> contributor,
    RelsExt re,
    String xslFile,
    ImageRepresentation[] files) throws ServiceException {

    if (log.isInfoEnabled()) {
      log.info(sourceObject.getClass().getSimpleName() + ": title=" + title + "; pid=" + pid);
    }

    DigitalObject foxmlObject = new DigitalObject();

    this.setCommonProperties(foxmlObject, pid, title);

    DublinCore dc = this.createMonographDublinCore(title, creator, publisher, contributor);

    this.setCommonStreams(foxmlObject, sourceObject, dc, re, xslFile, files);

    return foxmlObject;
  }

  private ImageRepresentation createImageRepresentation(
    String filename,
    TechnicalDescription td,
    UniqueIdentifier ui) {
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
