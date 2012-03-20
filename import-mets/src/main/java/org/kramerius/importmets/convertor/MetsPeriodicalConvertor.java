package org.kramerius.importmets.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kramerius.dc.ElementType;
import org.kramerius.dc.OaiDcType;
import org.kramerius.importmets.utils.XMLTools;
import org.kramerius.importmets.valueobj.ConvertorConfig;
import org.kramerius.importmets.valueobj.DublinCore;
import org.kramerius.importmets.valueobj.Foxml;
import org.kramerius.importmets.valueobj.ImageMetaData;
import org.kramerius.importmets.valueobj.ImageRepresentation;
import org.kramerius.importmets.valueobj.RelsExt;
import org.kramerius.importmets.valueobj.ServiceException;
import org.kramerius.mets.AreaType;
import org.kramerius.mets.DivType;
import org.kramerius.mets.DivType.Fptr;
import org.kramerius.mets.FileType.FLocat;
import org.kramerius.mets.FileType;
import org.kramerius.mets.MdSecType;
import org.kramerius.mets.Mets;
import org.kramerius.mets.MetsType.FileSec;
import org.kramerius.mets.MetsType.FileSec.FileGrp;
import org.kramerius.mets.StructMapType;
import org.kramerius.mods.DateBaseDefinition;
import org.kramerius.mods.DetailDefinition;
import org.kramerius.mods.IdentifierDefinition;
import org.kramerius.mods.ModsDefinition;
import org.kramerius.mods.ObjectFactory;
import org.kramerius.mods.PartDefinition;
import org.kramerius.mods.TitleInfoDefinition;
import org.kramerius.mods.XsString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import com.qbizm.kramerius.imp.jaxb.periodical.Contributor;
import com.qbizm.kramerius.imp.jaxb.periodical.ContributorName;
import com.qbizm.kramerius.imp.jaxb.periodical.CoreBibliographicDescriptionPeriodical;
import com.qbizm.kramerius.imp.jaxb.periodical.Creator;
import com.qbizm.kramerius.imp.jaxb.periodical.CreatorName;
import com.qbizm.kramerius.imp.jaxb.periodical.ItemRepresentation;
import com.qbizm.kramerius.imp.jaxb.periodical.Language;
import com.qbizm.kramerius.imp.jaxb.periodical.MainTitle;
import com.qbizm.kramerius.imp.jaxb.periodical.PageIndex;
import com.qbizm.kramerius.imp.jaxb.periodical.PageRepresentation;
import com.qbizm.kramerius.imp.jaxb.periodical.Periodical;
import com.qbizm.kramerius.imp.jaxb.periodical.PeriodicalInternalComponentPart;
import com.qbizm.kramerius.imp.jaxb.periodical.PeriodicalItem;
import com.qbizm.kramerius.imp.jaxb.periodical.PeriodicalPage;
import com.qbizm.kramerius.imp.jaxb.periodical.PeriodicalVolume;
import com.qbizm.kramerius.imp.jaxb.periodical.Publisher;
import com.qbizm.kramerius.imp.jaxb.periodical.Subject;
import com.qbizm.kramerius.imp.jaxb.periodical.TechnicalDescription;
import com.qbizm.kramerius.imp.jaxb.periodical.UniqueIdentifier;
import com.qbizm.kramerius.imp.jaxb.periodical.UniqueIdentifierURNType;

/**
 * Konvertor periodika do sady foxml digitalnich objektu
 *
 * @author xholcik
 */
public class MetsPeriodicalConvertor extends BaseConvertor {

    /**
     * XSL transformacni sablony
     */
    private static final String XSL_MODS_PERIODICAL = "model_periodical_MODS.xsl";

    private static final String XSL_MODS_PERIODICAL_PART = "model_periodicalInternalComponentPart-MODS.xsl";

    private static final String XSL_MODS_PERIODICAL_ITEM = "model_periodicalItem_MODS.xsl";

    private static final String XSL_MODS_PERIODICAL_PAGE = "model_periodicalPage-MODS.xsl";

    private static final String XSL_MODS_PERIODICAL_VOLUME = "model_periodicalVolume_MODS.xsl";


    private static final Logger log = Logger.getLogger(MetsPeriodicalConvertor.class);

    public MetsPeriodicalConvertor(ConvertorConfig config, Unmarshaller unmarshaller) throws ServiceException {
        super(config, unmarshaller);
    }

    /**
     * Pomocna metoda pro ziskani pid objektu
     *
     * @param uid
     * @return
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

    private Foxml periodical = new Foxml();
    private Foxml volume = new Foxml();
    private Foxml issue = new Foxml();
    private Map<String, Foxml> pages = new HashMap<String, Foxml>();
    private List<Foxml> internalParts = new ArrayList<Foxml>();

    public void  convert(Mets mets, StringBuffer convertedURI) throws ServiceException {
        try {
            log.info("LABEL:"+mets.getLabel1()+" TYPE:"+mets.getTYPE());

            policyID = config.isDefaultVisibility() ? POLICY_PUBLIC : POLICY_PRIVATE;
            loadModsAndDcMap(mets);
            loadFileMap(mets);
            processStructMap(mets);

            exportFoxml(periodical);
            exportFoxml(volume);
            exportFoxml(issue);
            for (Foxml page: pages.values()){
                exportFoxml(page);
            }
            for (Foxml part: internalParts){
                exportFoxml(part);
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

   /**
     * Načte všechny MODS a DC záznamy a uloží do map podle jejich ID
     * @param mets
     */
    private void loadModsAndDcMap(Mets mets) throws JAXBException{
        int modsCounter = 0;
        int dcCounter = 0;
        for(MdSecType md : mets.getDmdSec()){
            String id = md.getID();
            Element me = ((Element)md.getMdWrap().getXmlData().getAny().get(0));
            String type = md.getMdWrap().getMDTYPE();
            if("MODS".equalsIgnoreCase(type)){
                ModsDefinition mods = (ModsDefinition)((JAXBElement<?>)unmarshallerMODS.unmarshal(me)).getValue();
                if (modsMap.put(id, mods)!=null){
                    log.warn("Duplicate MODS record: "+id);
                }else{
                    modsCounter++;
                }
            }else if ("DC".equalsIgnoreCase(type)){
                OaiDcType dc = (OaiDcType)((JAXBElement<?>)unmarshallerDC.unmarshal(me)).getValue();
                if (dcMap.put(id, dc)!=null){
                    log.warn("Duplicate DC record: "+id);
                }else{
                    dcCounter++;
                }
            }else{
                log.warn("Unsupported metadata type: "+type+" for "+id);
            }
        }
        log.info("Loaded "+modsCounter+ " MODS records and "+dcCounter+" DC records.");
        if (dcCounter!=modsCounter){
            log.warn("Different MODS ("+modsCounter+") and DC ("+dcCounter+") records count.");
        }
    }


    private void loadFileMap(Mets mets) throws JAXBException{
        FileSec fsec =  mets.getFileSec();
        for (FileGrp fGrp : fsec.getFileGrp()){
            for ( FileType file : fGrp.getFile()){
                String id = file.getID();
                FLocat fl = firstItem(file.getFLocat());
                String name = fl.getHref();
                name = name.replace("masterCopy/MC", "userCopy/UC");
                fileMap.put(id,name);
            }
        }
    }


    private void processStructMap (Mets mets)throws ServiceException{
        DivType titleDiv = null;
        DivType issueDiv = null;
        for (StructMapType sm: mets.getStructMap()){
            if ("PHYSICAL".equals(sm.getTYPE())){
                titleDiv = processPeriodical(sm);
            } else if ("LOGICAL".equals(sm.getTYPE())){
                issueDiv = processIssue(sm);
            } else {
                log.warn("Unsupported StructMap type: "+sm.getTYPE()+" for "+sm.getID());
            }
        }
        processPages(titleDiv);
        processInternalParts(issueDiv);
    }


    private DivType processPeriodical(StructMapType sm){
        DivType titleDiv = sm.getDiv();
        MdSecType modsIdObj = (MdSecType)firstItem(titleDiv.getDMDID());
        String modsId = modsIdObj.getID();
        String dcId = modsId.replaceFirst("MODS", "DC");

        ModsDefinition mods = modsMap.get(modsId);
        OaiDcType dc = dcMap.get(dcId);
        String uuid = getUUIDfromMods(mods);
        if (uuid == null){
            uuid = generateUUID();
        }
        String pid = pid(uuid);
        String title = getTitlefromMods( mods);
        RelsExt re = new RelsExt(pid, MODEL_PERIODICAL);

        re.addRelation(RelsExt.POLICY, policyID, true);

        dc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createType(createDcElementType(MODEL_PERIODICAL)));
        dc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createRights(createDcElementType(policyID)));



        periodical.setPid(pid);
        periodical.setTitle(title);
        periodical.setDc(dc);
        periodical.setMods(mods);
        periodical.setRe(re);

        volume.setPid(pid(generateUUID()));
        getVolumefromMods( mods);
        volume.setTitle(volumeTitle);
        ModsDefinition volumeMods = modsObjectFactory.createModsDefinition();
        volumeMods.getModsGroup().add(volumePart);
        volume.setMods(volumeMods);
        OaiDcType volumeDc = dcObjectFactory.createOaiDcType();
        volumeDc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createIdentifier(createDcElementType(volume.getPid())));
        volumeDc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createTitle(createDcElementType(volume.getTitle())));
        volumeDc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createType(createDcElementType(MODEL_PERIODICAL_VOLUME)));
        volumeDc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createRights(createDcElementType(policyID)));

        volume.setDc(volumeDc);
        volume.setRe(new RelsExt(volume.getPid(), MODEL_PERIODICAL_VOLUME));
        volume.getRe().addRelation(RelsExt.POLICY, policyID, true);
        periodical.getRe().addRelation(RelsExt.HAS_VOLUME, volume.getPid(),false);

        return titleDiv;

    }

    private void processPages(DivType titleDiv){
        for (DivType pageDiv:titleDiv.getDiv()){
            String type = pageDiv.getTYPE();
            BigInteger order = pageDiv.getORDER();
            String pageTitle = pageDiv.getORDERLABEL();

            Foxml page = new Foxml();
            page.setPid(pid(generateUUID()));
            page.setTitle(pageTitle);
            //create MODS for page
            ModsDefinition pageMods = modsObjectFactory.createModsDefinition();
            PartDefinition pagePart = modsObjectFactory.createPartDefinition();
            pagePart.setType(type);
            //add part for page Number
            DetailDefinition titleDetail = modsObjectFactory.createDetailDefinition();
            titleDetail.setType("pageNumber");
            XsString titleString = modsObjectFactory.createXsString();
            titleString.setValue(pageTitle);
            JAXBElement<XsString> titleElement = modsObjectFactory.createNumber(titleString);
            titleDetail.getNumberOrCaptionOrTitle().add(titleElement);
            pagePart.getDetailOrExtentOrDate().add(titleDetail);
            //add part for page Index
            DetailDefinition orderDetail = modsObjectFactory.createDetailDefinition();
            orderDetail.setType("pageIndex");
            XsString orderString = modsObjectFactory.createXsString();
            orderString.setValue(order.toString());
            JAXBElement<XsString> orderElement = modsObjectFactory.createNumber(orderString);
            orderDetail.getNumberOrCaptionOrTitle().add(orderElement);
            pagePart.getDetailOrExtentOrDate().add(orderDetail);
            //add mods to page foxml
            pageMods.getModsGroup().add(pagePart);
            page.setMods(pageMods);
            //create DC for page
            OaiDcType pageDc = dcObjectFactory.createOaiDcType();
            pageDc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createIdentifier(createDcElementType(page.getPid())));
            pageDc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createTitle(createDcElementType(page.getTitle())));
            pageDc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createType(createDcElementType(MODEL_PAGE)));
            pageDc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createRights(createDcElementType(policyID)));
            page.setDc(pageDc);

            page.setRe(new RelsExt(page.getPid(), MODEL_PAGE));
            page.getRe().addRelation(RelsExt.POLICY, policyID, true);
            issue.getRe().addRelation(RelsExt.HAS_PAGE, page.getPid(),false);

            String pageId = null;
            for (Fptr fptr : pageDiv.getFptr()){
                for (Object obj: fptr.getPar().getAreaOrSeq()){
                    if (obj instanceof AreaType){
                        AreaType area = (AreaType)obj;
                        FileType fileId = (FileType)area.getFILEID();
                        if (pageId == null){
                            pageId = removeFileTypePrefix (fileId.getID());
                        }
                        String fileName = fileMap.get(fileId.getID());
                        page.addFiles(new ImageRepresentation(fileName, getFileType(fileId.getID())));
                    }
                }
            }

            pages.put(pageId, page);
        }
    }



    private String volumeTitle = "";
    private PartDefinition volumePart = null;

    /**
     * Fill in the convertor private fields volumeTitle and volumeDate from title mods
     * @param mods
     */
    private void getVolumefromMods(ModsDefinition mods){
        for(Object mg: mods.getModsGroup()){
            if (mg instanceof PartDefinition){
                PartDefinition pd = (PartDefinition)mg;
                if("volume".equalsIgnoreCase(pd.getType())){
                    volumePart = pd;
                    for (Object el:pd.getDetailOrExtentOrDate()){
                        if (el instanceof DetailDefinition){
                            DetailDefinition dd = (DetailDefinition)el;
                            if("volume".equalsIgnoreCase(dd.getType())){
                                for (JAXBElement<XsString> obj:dd.getNumberOrCaptionOrTitle()){
                                    volumeTitle = obj.getValue().getValue();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private DivType processIssue(StructMapType sm){

        DivType issueDiv = sm.getDiv();
        MdSecType modsIdObj = (MdSecType)firstItem(issueDiv.getDMDID());
        String modsId = modsIdObj.getID();
        String dcId = modsId.replaceFirst("MODS", "DC");

        ModsDefinition mods = modsMap.get(modsId);
        OaiDcType dc = dcMap.get(dcId);
        String uuid = getUUIDfromMods(mods);
        if (uuid == null){
            uuid = generateUUID();
        }
        String pid = pid(uuid);
        String title = getTitlefromMods( mods);
        RelsExt re = new RelsExt(pid, MODEL_PERIODICAL_ITEM);

        re.addRelation(RelsExt.POLICY, policyID, true);

        dc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createType(createDcElementType(MODEL_PERIODICAL_ITEM)));
        dc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createRights(createDcElementType(policyID)));


        issue.setPid(pid);
        issue.setTitle(title);
        issue.setDc(dc);
        issue.setMods(mods);
        issue.setRe(re);

        volume.getRe().addRelation(RelsExt.HAS_ITEM, issue.getPid(),false);

        return issueDiv;
    }


    String lastFileId = "";
    private void processInternalParts(DivType issueDiv){
        for (DivType partDiv : issueDiv.getDiv()){
            MdSecType modsIdObj = (MdSecType)firstItem(partDiv.getDMDID());
            String modsId = modsIdObj.getID();
            String dcId = modsId.replaceFirst("MODS", "DC");

            ModsDefinition mods = modsMap.get(modsId);
            if (mods == null){
                throw new ServiceException("Cannot find article mods: "+modsId);
            }
            OaiDcType dc = dcMap.get(dcId);
            if (dc == null){
                throw new ServiceException("Cannot find article dc: "+dcId);
            }
            String uuid = getUUIDfromMods(mods);
            if (uuid == null){
                uuid = generateUUID();
            }
            String pid = pid(uuid);
            String title = getTitlefromMods( mods);
            RelsExt re = new RelsExt(pid, MODEL_ARTICLE);

            re.addRelation(RelsExt.POLICY, policyID, true);

            dc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createType(createDcElementType(MODEL_ARTICLE)));
            dc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createRights(createDcElementType(policyID)));

            Foxml article = new Foxml();
            article.setPid(pid);
            article.setTitle(title);
            article.setDc(dc);
            article.setMods(mods);
            article.setRe(re);

            issue.getRe().addRelation(RelsExt.HAS_INT_COMP_PART, article.getPid(),false);

            lastFileId = "";
            extractFileIDFromDiv(partDiv, article);
            internalParts.add(article);

        }
    }

    protected void extractFileIDFromDiv(DivType partDiv, Foxml article) {
        for (DivType altoDiv : partDiv.getDiv()){
            String fileId = null;
            Fptr block = firstItem(altoDiv.getFptr());
            if (block != null){
                AreaType area = block.getArea();
                if (area != null){
                    FileType file = (FileType)block.getArea().getFILEID();
                    if (file != null){
                        fileId = removeFileTypePrefix ( file.getID() );
                    }else{
                        throw new ServiceException ("NULL FILEID in "+block.getID());
                    }
                }else{
                    throw new ServiceException ("NULL AREA in "+block.getID());
                }
            }else{
                extractFileIDFromDiv(altoDiv,article);
            }
            if (fileId != null && !lastFileId.equalsIgnoreCase(fileId)){
                Foxml page = pages.get(fileId);
                if (page == null){
                    throw new ServiceException("Cannot find page for Alto: "+altoDiv.getID()+ " (fileId ="+fileId+", lastFileId ="+lastFileId+")");
                }
                String pagePid = page.getPid();
                article.getRe().addRelation(RelsExt.IS_ON_PAGE, pagePid, false);
                lastFileId = fileId;
            }
        }
    }


    /**
     * Prevede periodikum a vsechny navazane objekty do sady foxml souboru
     *
     * @param peri
     * @throws ServiceException
     */
    public void  convert(Periodical peri, StringBuffer convertedURI) throws ServiceException {
        CoreBibliographicDescriptionPeriodical biblio = peri.getCoreBibliographicDescriptionPeriodical();
        if (biblio == null) {
            biblio = new CoreBibliographicDescriptionPeriodical();
        }
        String title = getMainTitle(biblio);
        if (peri.getUniqueIdentifier() == null) {
            peri.setUniqueIdentifier(new UniqueIdentifier());
        }
        String uuid = uuid(peri.getUniqueIdentifier());
        String pid = pid (uuid);

        RelsExt re = new RelsExt(pid, MODEL_PERIODICAL);
        boolean visibility = isPublic(uuid, config.isDefaultVisibility(), "p_periodical");

        String volumeuuid= null;
        for (PeriodicalVolume volume : peri.getPeriodicalVolume()) {
            volumeuuid = this.convertVolume(volume, visibility);
            re.addRelation(RelsExt.HAS_VOLUME, pid(uuid(volume.getUniqueIdentifier())),false);
        }
        String cleanTitle= StringUtils.replaceEach(title, new String[]{"\t", "\n"}, new String[]{" ", " "});
        if (volumeuuid== null){
            convertedURI.append(cleanTitle).append("\t").append("pid=").append(pid);
        } else{
            convertedURI.append(cleanTitle).append("\t").append("pid=").append(volumeuuid);
        }


        DublinCore dc = createPeriodicalDublinCore(pid, title, biblio);

        convertHandle(uuid, dc, re);

        String ISSN = peri.getISSN()==null?null:first(peri.getISSN().getContent());
        dc.addQualifiedIdentifier(RelsExt.ISSN, ISSN);

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

        dc.setDescription(biblio.getAnnotation() == null? null:concat(biblio.getAnnotation().getContent()));
        Publisher publ = firstItem(biblio.getPublisher());
        if (publ!= null){
            dc.setDate(publ.getDateOfPublication()==null?null:first(publ.getDateOfPublication().getContent()));
        }

        dc.setType(MODEL_PERIODICAL);

        Language lang = firstItem(biblio.getLanguage());
        if (lang != null){
            dc.setLanguage(first(lang.getContent()));
        }

        ImageRepresentation[] files = new ImageRepresentation[1];
        if (peri.getTechnicalDescription() != null) {
            //TODOfiles[0] = this.createImageRepresentation(null, peri.getTechnicalDescription(), null);
        }

        //TODO DigitalObject foxmlPeri = this.createDigitalObject(peri, pid, title, dc, re, XSL_MODS_PERIODICAL, files, visibility);

      //TODO this.marshalDigitalObject(foxmlPeri);

    }



    /**
     * Prevede PeriodicalVolume do foxml
     *
     * @param volume
     * @param prefix
     * @throws ServiceException
     */
    private String convertVolume(PeriodicalVolume volume, boolean parentVisibility) throws ServiceException {
        CoreBibliographicDescriptionPeriodical biblio = volume.getCoreBibliographicDescriptionPeriodical();
        if (biblio == null) {
            biblio = new CoreBibliographicDescriptionPeriodical();
        }
        String title = "";
        if (volume.getPeriodicalVolumeIdentification() != null && volume.getPeriodicalVolumeIdentification().getPeriodicalVolumeNumber() != null) {
            title = first(volume.getPeriodicalVolumeIdentification().getPeriodicalVolumeNumber().getContent());
        }
        if (volume.getUniqueIdentifier() == null) {
            volume.setUniqueIdentifier(new UniqueIdentifier());
        }
        String uuid = uuid(volume.getUniqueIdentifier());
        String pid = pid (uuid);

        RelsExt re = new RelsExt(pid, MODEL_PERIODICAL_VOLUME);

        boolean visibility = isPublic(uuid, parentVisibility, "p_periodicalvolume");

        String contract = getContract(volume.getPeriodicalPage());
        if (contract == null){
            PeriodicalItem item = firstItem(volume.getPeriodicalItem());
            if (item !=  null){
                contract = getContract(item.getPeriodicalPage());
            }
        }
        getConfig().setContract(contract);

        for (PeriodicalItem item : volume.getPeriodicalItem()) {
            this.convertItem(item, visibility);
            re.addRelation(RelsExt.HAS_ITEM, pid(uuid(item.getUniqueIdentifier())),false);
        }

        Map<Integer, String> pageIdMap = new TreeMap<Integer, String>();
        for (PeriodicalPage page : volume.getPeriodicalPage()) {
            this.convertPage(page, visibility);

            String ppid = pid(uuid(page.getUniqueIdentifier()));
            re.addRelation(RelsExt.HAS_PAGE, ppid,false);
            fillPageIdMap(pageIdMap, page.getIndex(), ppid);
        }

        for (PeriodicalInternalComponentPart part : volume.getPeriodicalInternalComponentPart()) {
            this.convertInternalPart(part, pageIdMap, visibility);
            re.addRelation(RelsExt.HAS_INT_COMP_PART, pid(uuid(part.getUniqueIdentifier())),false);
        }

        DublinCore dc = createPeriodicalDublinCore(pid, title, biblio);

        dc.addQualifiedIdentifier(RelsExt.CONTRACT, contract);
        re.addRelation(RelsExt.CONTRACT, contract, true);

        convertHandle(uuid, dc, re);

        dc.setDescription(biblio.getAnnotation() == null? null:concat(biblio.getAnnotation().getContent()));

        if (volume.getPeriodicalVolumeIdentification() != null && volume.getPeriodicalVolumeIdentification().getPeriodicalVolumeDate() != null) {
             dc.setDate(first(volume.getPeriodicalVolumeIdentification().getPeriodicalVolumeDate().getContent()));
        }


        dc.setType(MODEL_PERIODICAL_VOLUME);

        ImageRepresentation[] files = new ImageRepresentation[1];
        if (volume.getTechnicalDescription() != null) {
           //TODO files[0] = this.createImageRepresentation(null, volume.getTechnicalDescription(), null);
        }

      //TODO  DigitalObject foxmlVolume = this.createDigitalObject(volume, pid, title, dc, re, XSL_MODS_PERIODICAL_VOLUME, files, visibility);

      //TODO this.marshalDigitalObject(foxmlVolume);
        return pid;
    }

    /**
     * Prevede PeriodicalInternalComponentPart do foxml
     *
     * @param part
     * @param prefix
     * @throws ServiceException
     */
    private void convertInternalPart(PeriodicalInternalComponentPart part, Map<Integer, String> pageIdMap, boolean visibility) throws ServiceException {
        CoreBibliographicDescriptionPeriodical biblio = part.getCoreBibliographicDescriptionPeriodical();
        if (biblio == null) {
            biblio = new CoreBibliographicDescriptionPeriodical();
        }
        String title = getMainTitle(biblio);
        if (part.getUniqueIdentifier() == null) {
            part.setUniqueIdentifier(new UniqueIdentifier());
        }
        String uuid = uuid(part.getUniqueIdentifier());
        String pid = pid(uuid);

        RelsExt re = new RelsExt(pid, MODEL_INTERNAL_PART);

        List<PageIndex> pageIndex = part.getPages() != null ? part.getPages().getPageIndex() : null;
        if (pageIndex != null && !part.getPages().getPageIndex().isEmpty()) {
            for (PageIndex pi : pageIndex) {
                Integer piFrom = Integer.valueOf(pi.getFrom());
                Integer piTo = Integer.valueOf(pi.getTo());
                this.processPageIndex(re, piFrom, piTo, pageIdMap);
            }
        }

        DublinCore dc = createPeriodicalDublinCore(pid, title, biblio);

        convertHandle(uuid, dc, re);
        dc.setType(MODEL_INTERNAL_PART);

        dc.setDescription(biblio.getAnnotation() == null? null:concat(biblio.getAnnotation().getContent()));
        Publisher publ = firstItem(biblio.getPublisher());
        if (publ!= null){
            dc.setDate(publ.getDateOfPublication()==null?null:first(publ.getDateOfPublication().getContent()));
        }


        Language lang = firstItem(biblio.getLanguage());
        if (lang != null){
            dc.setLanguage(first(lang.getContent()));
        }

      //TODO DigitalObject foxmlPart = this.createDigitalObject(part, pid, title, dc, re, XSL_MODS_PERIODICAL_PART, null, visibility);

      //TODO this.marshalDigitalObject(foxmlPart);
    }

    /**
     * Prevede PeriodicalItem do foxml
     *
     * @param item
     * @param prefix
     * @throws ServiceException
     */
    private void convertItem(PeriodicalItem item, boolean parentVisibility) throws ServiceException {
        CoreBibliographicDescriptionPeriodical biblio = item.getCoreBibliographicDescriptionPeriodical();
        if (biblio == null) {
            biblio = new CoreBibliographicDescriptionPeriodical();
        }
        String title = "";
        if (item.getPeriodicalItemIdentification() != null && item.getPeriodicalItemIdentification().getPeriodicalItemNumber() != null) {
            title = first(item.getPeriodicalItemIdentification().getPeriodicalItemNumber().getContent());
        }
        if (item.getUniqueIdentifier() == null) {
            item.setUniqueIdentifier(new UniqueIdentifier());
        }
        String uuid = uuid(item.getUniqueIdentifier());
        String pid = pid (uuid);

        List<ImageRepresentation> files = new ArrayList<ImageRepresentation>(2);
        if (item.getItemRepresentation() != null) {
            ItemRepresentation r = item.getItemRepresentation();
            if (item.getItemRepresentation().getItemImage() != null) {
                //TODO files.add(this.createImageRepresentation(r.getItemImage().getHref(), r.getTechnicalDescription(), item.getItemRepresentation().getUniqueIdentifier()));
            }
            if (item.getItemRepresentation().getItemText() != null) {
              //TODO files.add(this.createImageRepresentation(r.getItemText().getHref(), null, item.getItemRepresentation().getUniqueIdentifier()));
            }
        }

        RelsExt re = new RelsExt(pid, MODEL_PERIODICAL_ITEM);
        boolean visibility = isPublic(uuid, parentVisibility, "p_periodicalitem");

        Map<Integer, String> pageIdMap = new TreeMap<Integer, String>();
        for (PeriodicalPage page : item.getPeriodicalPage()) {
            this.convertPage(page,visibility);

            String ppid = pid(uuid(page.getUniqueIdentifier()));
            re.addRelation(RelsExt.HAS_PAGE, ppid,false);
            fillPageIdMap(pageIdMap, page.getIndex(), ppid);
        }

        for (PeriodicalInternalComponentPart part : item.getPeriodicalInternalComponentPart()) {
            this.convertInternalPart(part, pageIdMap, visibility);
            re.addRelation(RelsExt.HAS_INT_COMP_PART, pid(uuid(part.getUniqueIdentifier())),false);
        }

        DublinCore dc = createPeriodicalDublinCore(pid, title, biblio);
        String contract = getContract(item.getPeriodicalPage());
        dc.addQualifiedIdentifier(RelsExt.CONTRACT, contract);
        re.addRelation(RelsExt.CONTRACT, contract, true);

        convertHandle(uuid, dc, re);

        dc.setType(MODEL_PERIODICAL_ITEM);

        dc.setDescription(biblio.getAnnotation() == null? null:concat(biblio.getAnnotation().getContent()));

        if (item.getPeriodicalItemIdentification() != null && item.getPeriodicalItemIdentification().getPeriodicalItemDate() != null) {
            dc.setDate(first(item.getPeriodicalItemIdentification().getPeriodicalItemDate().getContent()));
        }

      //TODO DigitalObject foxmlItem = this.createDigitalObject(item, pid, title, dc, re, XSL_MODS_PERIODICAL_ITEM, files.toArray(new ImageRepresentation[files.size()]), visibility);

      //TODO this.marshalDigitalObject(foxmlItem);
    }

    /**
     * Prevede stranku periodika do foxml
     *
     * @param page
     * @throws ServiceException
     */
    private void convertPage(PeriodicalPage page, boolean visibility) throws ServiceException {
        String title = first(page.getPageNumber().get(0).getContent());
        // String title = page.getIndex();
        if (page.getUniqueIdentifier() == null) {
            page.setUniqueIdentifier(new UniqueIdentifier());
        }
        String uuid = uuid(page.getUniqueIdentifier());
        String pid = pid (uuid);

        RelsExt re = new RelsExt(pid, MODEL_PAGE);

        List<ImageRepresentation> files = new ArrayList<ImageRepresentation>(2);
        for (PageRepresentation r : page.getPageRepresentation()) {
            if (r.getPageImage() != null) {
              //TODO files.add(this.createImageRepresentation(r.getPageImage().getHref(), r.getTechnicalDescription(), r.getUniqueIdentifier()));
                re.addRelation(RelsExt.FILE, r.getPageImage().getHref(), true);
            }
            if (r.getPageText() != null) {
              //TODO files.add(this.createImageRepresentation(r.getPageText().getHref(), null, r.getUniqueIdentifier()));
            }
        }



        DublinCore dc = this.createPeriodicalDublinCore(pid, title, null);

        convertHandle(uuid, dc, re);

        dc.setType(MODEL_PAGE);

      //TODO DigitalObject foxmlPage = this.createDigitalObject(page, pid, title, dc, re, XSL_MODS_PERIODICAL_PAGE, files.toArray(new ImageRepresentation[files.size()]), visibility);

      //TODO this.marshalDigitalObject(foxmlPage);
    }

    /**
     * Vytvori a naplni DublinCore data pro DC datastream
     *
     * @param biblio
     * @return
     */
    private DublinCore createPeriodicalDublinCore(String pid, String title, CoreBibliographicDescriptionPeriodical biblio) {
        if (biblio == null) {
            biblio = new CoreBibliographicDescriptionPeriodical();
        }
        DublinCore dc = new DublinCore();

        dc.setTitle(title);
        dc.addIdentifier(pid);

        dc.setCreator(new ArrayList<String>());
        if (biblio.getCreator() != null) {
            for (Creator c : biblio.getCreator()) {
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
        if (biblio.getPublisher() != null) {
            for (Publisher p : biblio.getPublisher() ) {
                dc.getPublisher().add(first(p.getPublisherName().getContent()));
            }
        }

        dc.setContributor(new ArrayList<String>());
        if (biblio.getContributor()  != null) {
            for (Contributor c : biblio.getContributor()) {
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





    private String getContract(List<PeriodicalPage> pages) {
        if (pages != null) {
            for (PeriodicalPage page : pages) {
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
