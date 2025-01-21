package org.kramerius.importmets.convertor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;

import org.apache.log4j.Logger;
import org.kramerius.alto.Alto;
import org.kramerius.alto.Alto.Layout.Page;
import org.kramerius.alto.BlockType;
import org.kramerius.alto.StringType;
import org.kramerius.alto.TextBlockType;
import org.kramerius.alto.TextBlockType.TextLine;
import org.kramerius.dc.ElementType;
import org.kramerius.dc.OaiDcType;
import org.kramerius.importmets.valueobj.*;
import org.kramerius.mets.*;
import org.kramerius.mets.DivType.Fptr;
import org.kramerius.mets.FileType.FLocat;
import org.kramerius.mets.MetsType.FileSec;
import org.kramerius.mets.MetsType.FileSec.FileGrp;
import org.kramerius.mets.MetsType.StructLink;
import org.kramerius.mets.StructLinkType.SmLink;
import org.kramerius.mods.*;
import org.kramerius.srwdc.DcCollectionType;
import org.kramerius.srwdc.SrwDcType;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Konvertor periodika do sady foxml digitalnich objektu
 *
 * @author xholcik
 */
public class MetsPeriodicalConvertor extends BaseConvertor {


    private static final Logger log = Logger.getLogger(MetsPeriodicalConvertor.class);

    public MetsPeriodicalConvertor(ConvertorConfig config, Unmarshaller unmarshaller) throws ServiceException {
        super(config, unmarshaller);
    }


    public void convert(Mets mets, StringBuffer convertedURI)
            throws ServiceException {
        try {
            policyID = config.isPolicyPublic() ? POLICY_PUBLIC : POLICY_PRIVATE;
            loadModsAndDcMap(mets);
            loadFileMap(mets);
            processStructMap(mets);
            for (Foxml obj : objects.values()) {
                exportFoxml(obj);
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Načte všechny MODS a DC záznamy a uloží do map podle jejich ID
     *
     * @param mets
     */
    private void loadModsAndDcMap(Mets mets) throws JAXBException {
        int modsCounter = 0;
        int dcCounter = 0;
        for (MdSecType md : mets.getDmdSec()) {
            String id = md.getID();
            Element me = ((Element) md.getMdWrap().getXmlData().getAny().get(0));
            String type = md.getMdWrap().getMDTYPE();
            if ("MODS".equalsIgnoreCase(type)) {
                Object elementValue = ((JAXBElement<?>) unmarshallerMODS.unmarshal(me)).getValue();
                ModsDefinition mods = null;
                if (elementValue instanceof ModsDefinition) {
                    mods = (ModsDefinition) elementValue;
                } else if (elementValue instanceof ModsCollectionDefinition) {
                    mods = firstItem(((ModsCollectionDefinition) elementValue).getMods());
                } else {
                    log.warn("Unsupported MODS element: " + elementValue.getClass());
                }
                if (modsMap.put(id, mods) != null) {
                    log.warn("Duplicate MODS record: " + id);
                } else {
                    modsCounter++;
                }
                if (specialGenre.equals(Genre.NONE)) {//if cartographic or sheetmusic was already detected, do not overwrite it
                    specialGenre = getGenrefromMods(mods);//otherwise check, if it is set or not
                }
            } else if ("DC".equalsIgnoreCase(type)) {
                Object elementValue = null;
                try {
                    elementValue = ((JAXBElement<?>) unmarshallerDC.unmarshal(me)).getValue();
                } catch (UnmarshalException ue) {
                    elementValue = ((JAXBElement<?>) unmarshallerSRWDC.unmarshal(me)).getValue();
                }
                OaiDcType dc = null;
                if (elementValue instanceof OaiDcType) {
                    dc = (OaiDcType) elementValue;
                } else if (elementValue instanceof DcCollectionType) {
                    dc = morphSRWDCtoOAIDC(firstItem(((DcCollectionType) elementValue).getDc()));
                } else {
                    log.warn("Unsupported DC element: " + elementValue.getClass());
                }

                if (dcMap.put(id, dc) != null) {
                    log.warn("Duplicate DC record: " + id);
                } else {
                    dcCounter++;
                }
            } else {
                log.warn("Unsupported metadata type: " + type + " for " + id);
            }
        }
        log.info("Loaded " + modsCounter + " MODS records and " + dcCounter
                + " DC records.");
        if (dcCounter != modsCounter) {
            log.warn("Different MODS (" + modsCounter + ") and DC ("
                    + dcCounter + ") records count.");
        }
    }

    private OaiDcType morphSRWDCtoOAIDC(SrwDcType srwdc) {

        org.kramerius.dc.ObjectFactory of = new org.kramerius.dc.ObjectFactory();
        OaiDcType dc = of.createOaiDcType();
        for (JAXBElement<org.kramerius.srwdc.ElementType> srwdcelem : srwdc.getTitleOrCreatorOrSubject()) {
            org.kramerius.dc.ElementType elem = of.createElementType();
            elem.setLang(srwdcelem.getValue().getLang());
            elem.setValue(srwdcelem.getValue().getValue());
            JAXBElement<ElementType> dcjaxb = new JAXBElement<ElementType>(srwdcelem.getName(), org.kramerius.dc.ElementType.class, null, elem);
            dc.getTitleOrCreatorOrSubject().add(dcjaxb);
        }
        return dc;
    }

    private void loadFileMap(Mets mets) throws JAXBException {
        int filecounter = 0;
        FileSec fsec = mets.getFileSec();
        for (FileGrp fGrp : fsec.getFileGrp()) {
            String grpId = fGrp.getID();
            StreamFileType groupType = getFileType(grpId);
            for (FileType file : fGrp.getFile()) {
                String id = file.getID();
                String declaredMimetype = file.getMIMETYPE();

                groupType = makeSureOCRGRP(grpId, declaredMimetype, groupType);

                FLocat fl = firstItem(file.getFLocat());
                String name = fl.getHref().replace("\\", "/");
                fileMap.put(id, new FileDescriptor(name, groupType));
                filecounter++;
            }
        }
        log.info("Loaded files: " + filecounter);
    }

    private void processStructMap(Mets mets) throws ServiceException {
        for (StructMapType sm : mets.getStructMap()) {
            if ("PHYSICAL".equalsIgnoreCase(sm.getTYPE())) {
                processPages(sm);
            } else if ("LOGICAL".equalsIgnoreCase(sm.getTYPE())) {
                singleVolumeMonograph = false;
                processDiv(null, null, sm.getDiv());
                // eMonograph has only one structMap without TYPE
            } else if (sm.getTYPE() == null) {
                processElectronicDiv(null, sm.getDiv());
                return;
            } else {
                log.warn("Unsupported StructMap type: " + sm.getTYPE()
                        + " for " + sm.getID());
            }
        }
        processStructLink(mets.getStructLink());

    }

    private Map<String, String> filePageMap = new HashMap<String, String>();
    private Multimap<String, FileDescriptor> audioFilesMap = ArrayListMultimap.create();

    private void collectAudioFiles(DivType pageDiv) {
        if (pageDiv.getDiv() != null) {
            pageDiv.getDiv().forEach(this::collectAudioFiles);
        }
        for (Fptr fptr : pageDiv.getFptr()) {
            FileType fileId = (FileType) fptr.getFILEID();
            FileDescriptor fileDesc = fileMap.get(fileId.getID());
            if (fileDesc == null) {
                throw new ServiceException("Invalid audiofile pointer:" + fileId.getID());
            }
            audioFilesMap.put(pageDiv.getID(), fileDesc);
        }
    }

    private void processPages(StructMapType sm) {
        DivType issueDiv = sm.getDiv();
        for (DivType pageDiv : issueDiv.getDiv()) {
            String type = pageDiv.getTYPE();
            if (type != null && (type.equalsIgnoreCase("sound") || type.equalsIgnoreCase("soundpart"))) {
                collectAudioFiles(pageDiv);
                continue;
            }
            BigInteger order = pageDiv.getORDER();
            String pageTitle = pageDiv.getORDERLABEL();

            Foxml page = new Foxml();
            page.setPid(pid(generateUUID()));
            page.setTitle(pageTitle);
            String pageDivID = pageDiv.getID();
            String modsId = "";
            if (pageDivID.startsWith("DIV_PAGE")) {
                modsId = pageDiv.getID().replaceFirst("DIV", "MODSMD");
            } else {
                modsId = pageDiv.getID().replaceFirst("DIV_P", "MODSMD");
            }


            ModsDefinition pageMods = modsMap.get(modsId);
            if (pageMods == null) {
                log.info("CREATING NEW MODS for page " + page.getTitle());

                // create MODS for page
                pageMods = modsObjectFactory.createModsDefinition();
                PartDefinition pagePart = modsObjectFactory.createPartDefinition();
                pagePart.setType(type);
                // add part for page Number
                DetailDefinition titleDetail = modsObjectFactory.createDetailDefinition();
                titleDetail.setType("pageNumber");
                XsString titleString = modsObjectFactory.createXsString();
                titleString.setValue(pageTitle);
                JAXBElement<XsString> titleElement = modsObjectFactory.createNumber(titleString);
                titleDetail.getNumberOrCaptionOrTitle().add(titleElement);
                pagePart.getDetailOrExtentOrDate().add(titleDetail);
                // add part for page Index
                DetailDefinition orderDetail = modsObjectFactory.createDetailDefinition();
                orderDetail.setType("pageIndex");
                XsString orderString = modsObjectFactory.createXsString();
                orderString.setValue(order != null ? order.toString() : "");
                JAXBElement<XsString> orderElement = modsObjectFactory.createNumber(orderString);
                orderDetail.getNumberOrCaptionOrTitle().add(orderElement);
                pagePart.getDetailOrExtentOrDate().add(orderDetail);
                // add mods to page foxml
                pageMods.getModsGroup().add(pagePart);
                page.setMods(pageMods);
                // create DC for page
                OaiDcType pageDc = createDC(page.getPid(), page.getTitle());
                setDCModelAndPolicy(pageDc, MODEL_PAGE, policyID);
                page.setDc(pageDc);
            } else {  // use MODS and DC from METS dmdSec
                log.info("USING EXISTING MODS for page " + page.getTitle());
                String uuid = getUUIDfromMods(pageMods);
                if (uuid == null) {
                    uuid = generateUUID();
                }
                String pid = pid(uuid);
                page.setPid(pid);
                page.setMods(pageMods);
                String dcId = modsId.replaceFirst("MODS", "DC");
                OaiDcType dc = dcMap.get(dcId);
                if (dc == null) {
                    log.warn("DublinCore part missing for MODS " + modsId);
                    dc = createDC(pid, page.getTitle());
                }
                setDCModelAndPolicy(dc, MODEL_PAGE, policyID);
                page.setDc(dc);
            }

            page.setRe(new RelsExt(page.getPid(), MODEL_PAGE));
            page.getRe().addRelation(RelsExt.POLICY, policyID, true);

            for (Fptr fptr : pageDiv.getFptr()) {
                FileType fileId = (FileType) fptr.getFILEID();
                FileDescriptor fileDesc = fileMap.get(fileId.getID());
                if (fileDesc == null) {
                    throw new ServiceException("Invalid file pointer:" + fileId.getID());
                }
                if (KConfiguration.getInstance().getConfiguration().getBoolean("convert.userCopy", true)) {
                    if (StreamFileType.MASTER_IMAGE.equals(fileDesc.getFileType())) {
                        continue;
                    }
                } else {
                    if (StreamFileType.USER_IMAGE.equals(fileDesc.getFileType())) {
                        continue;
                    }
                }
                page.addFiles(fileDesc);
                filePageMap.put(fileId.getID(), page.getPid());//map file ID to page uuid - for collecting alto references from struct map in method collectAlto
            }

            String pageId = pageDiv.getID();
            objects.put(pageId, page);
        }
    }

    private OaiDcType createDC(String pid, String title) {
        OaiDcType dc = dcObjectFactory.createOaiDcType();
        dc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createIdentifier(createDcElementType(pid)));
        dc.getTitleOrCreatorOrSubject().add(dcObjectFactory.createTitle(createDcElementType(title)));
        return dc;
    }

    private void setDCModelAndPolicy(OaiDcType dc, String model, String policy) {
        List<JAXBElement<ElementType>> dclist = dc.getTitleOrCreatorOrSubject();
        boolean containsTypeElement = false;//check if DC already contains some type element
        for (JAXBElement<ElementType> el : dclist) {
            if ("type".equalsIgnoreCase(el.getName().getLocalPart())) {
                if (el.getValue().getValue().startsWith("model:")) {
                    el.getValue().setValue(model);
                    containsTypeElement = true;
                }
            }
        }
        if (!containsTypeElement) {
            dclist.add(dcObjectFactory.createType(createDcElementType(model)));
        }
        dclist.add(dcObjectFactory.createRights(createDcElementType(policy)));
    }

    private boolean singleVolumeMonograph = false;

    private String soundCollectionId = null;  //root sound collection id for sound recordings, used in processStructLink for adding missing page relations

    private Foxml processDiv(Foxml parent, String parentModel, DivType div) {
        String divType = div.getTYPE();
        if ("SOUNDCOLLECTION".equalsIgnoreCase(divType)) {
            soundCollectionId = div.getID();
        }
        if ("PAGE".equalsIgnoreCase(divType))
            return null;//divs for PAGES are processed from physical map and structlinks
        MdSecType modsIdObj = (MdSecType) firstItem(div.getDMDID());
        //if ("PICTURE".equalsIgnoreCase(divType)) return null;//divs for PICTURE are not supported in K4
        if ("MONOGRAPH".equalsIgnoreCase(divType) && modsIdObj == null) {//special hack to ignore extra div for single volume monograph
            singleVolumeMonograph = true;
            List<DivType> volumeDivs = div.getDiv();
            if (volumeDivs == null) return null;
            if (volumeDivs.size() == 1) {//process volume as top level
                processDiv(null, null, volumeDivs.get(0));
                return null;
            }
            if (volumeDivs.size() > 1) {//if monograph div contains more subdivs, first is supposed to be the volume, the rest are supplements that will be nested in the volume.
                Foxml volume = processDiv(null, null, volumeDivs.get(0));
                for (int i = 1; i < volumeDivs.size(); i++) {
                    processDiv(volume, null, volumeDivs.get(i));
                }
            }
            return null;
        }

        if (modsIdObj == null) {
            collectAlto(parent, div);
            return null;//we consider only div with associated metadata (DMDID)
        }

        String model = mapModel(divType, false);


        String modsId = modsIdObj.getID();
        String dcId = modsId.replaceFirst("MODS", "DC");

        ModsDefinition mods = modsMap.get(modsId);
        if (mods == null) {
            throw new ServiceException("Cannot find mods: " + modsId);
        }


        String uuid = getUUIDfromMods(mods);
        if (uuid == null) {
            uuid = generateUUID();
        }
        String pid = pid(uuid);
        String title = getTitlefromMods(mods);
        RelsExt re = new RelsExt(pid, model);

        re.addRelation(RelsExt.POLICY, policyID, true);

        OaiDcType dc = dcMap.get(dcId);
        if (dc == null) {
            log.warn("DublinCore part missing for MODS " + modsId);
            dc = createDC(pid, title);
        }
        setDCModelAndPolicy(dc, model, policyID);

        Foxml foxml = new Foxml();
        foxml.setPid(pid);
        foxml.setTitle(title);
        foxml.setDc(dc);
        foxml.setMods(mods);
        foxml.setRe(re);
        if (parent != null) {
            String parentRelation = mapParentRelation(model);
//            if (RelsExt.CONTAINS_TRACK.equalsIgnoreCase(parentRelation)&& MODEL_SOUND_RECORDING.equalsIgnoreCase(parentModel)){
//                parent.getRe().addRelation(RelsExt.HAS_TRACK, pid, false);
//            } else {
            parent.getRe().addRelation(parentRelation, pid, false);
//            }
        }
        String divID = div.getID();
        objects.put(divID, foxml);

        for (DivType partDiv : div.getDiv()) {
            processDiv(foxml, model, partDiv);
        }
        return foxml;
    }

    private boolean singleVolumeEMonograph = true;

    private Foxml processElectronicDiv(Foxml parent, DivType div) {
        String divType = div.getTYPE();
        MdSecType modsIdObj = (MdSecType) firstItem(div.getDMDID());

        if ("TITLE".equalsIgnoreCase(divType) && modsIdObj != null) {
            singleVolumeEMonograph = false;
        }

        if ("DOCUMENT".equalsIgnoreCase(divType)) {
            processElectronicDiv(parent, div.getDiv().get(0));
            return null;
        }

        if ("FILE".equalsIgnoreCase(divType) && modsIdObj == null) {
            for (Fptr fptr : div.getFptr()) {
                FileType fileId = (FileType) fptr.getFILEID();
                FileDescriptor fileDesc = fileMap.get(fileId.getID());
                parent.addFiles(fileDesc);
            }
            return null;
        }


        String model = mapModel(divType, true);
        String modsId = modsIdObj.getID();
        String dcId = modsId.replaceFirst("MODS", "DC");

        ModsDefinition mods = modsMap.get(modsId);
        if (mods == null) {
            throw new ServiceException("Cannot find mods: " + modsId);
        }


        String uuid = getUUIDfromMods(mods);
        if (uuid == null) {
            uuid = generateUUID();
        }
        String pid = pid(uuid);
        String title = getTitlefromMods(mods);
        RelsExt re = new RelsExt(pid, model);

        re.addRelation(RelsExt.POLICY, policyID, true);

        OaiDcType dc = dcMap.get(dcId);
        if (dc == null) {
            log.warn("DublinCore part missing for MODS " + modsId);
            dc = createDC(pid, title);
        }
        setDCModelAndPolicy(dc, model, policyID);

        Foxml foxml = new Foxml();
        foxml.setPid(pid);
        foxml.setTitle(title);
        foxml.setDc(dc);

        foxml.setMods(mods);
        foxml.setRe(re);
        if (parent != null) {
            String parentRelation = mapParentRelation(model);
            parent.getRe().addRelation(parentRelation, pid, false);
        }
        String divID = div.getID();
        objects.put(divID, foxml);

        for (DivType partDiv : div.getDiv()) {
            processElectronicDiv(foxml, partDiv);
        }
        return foxml;
    }

    private Genre specialGenre = Genre.NONE;

    private String mapModel(String divType, boolean isElectronic) {
        if ("PERIODICAL_TITLE".equalsIgnoreCase(divType)) {
            return MODEL_PERIODICAL;
        } else if ("PERIODICAL_VOLUME".equalsIgnoreCase(divType)) {
            return MODEL_PERIODICAL_VOLUME;
        } else if ("ISSUE".equalsIgnoreCase(divType)) {
            return MODEL_PERIODICAL_ITEM;
        } else if ("ARTICLE".equalsIgnoreCase(divType)) {
            return MODEL_ARTICLE;
        } else if ("SUPPLEMENT".equalsIgnoreCase(divType) //DMF Zvuk-Gramofonové_desky 0.4; DMF Zvuk-Fonografické_válečky 0.2;
                || "SUPPL".equalsIgnoreCase(divType) //DMF Zvuk-Gramofonové_desky 0.3; DMF Zvuk-Fonografické_válečky 0.1;
        ) {
            return MODEL_SUPPLEMENT;
        } else if ("PICTURE".equalsIgnoreCase(divType)) {
            return MODEL_PICTURE;
        } else if ("VOLUME".equalsIgnoreCase(divType) && !isElectronic) {
            if (singleVolumeMonograph) {
                return checkSpecialGenreOrMonograph();
            } else {
                return MODEL_MONOGRAPH_UNIT;
            }
        } else if ("CHAPTER".equalsIgnoreCase(divType)) {
            return MODEL_INTERNAL_PART;
        } else if ("MONOGRAPH".equalsIgnoreCase(divType)) {
            return checkSpecialGenreOrMonograph();
            //emonography
        } else if ("TITLE".equalsIgnoreCase(divType) && isElectronic) {
            return MODEL_MONOGRAPH;
        } else if ("VOLUME".equalsIgnoreCase(divType) && isElectronic) {
            if (singleVolumeEMonograph) {
                return MODEL_MONOGRAPH;
            } else {
                return MODEL_MONOGRAPH_UNIT;
            }
        } else if ("SOUNDCOLLECTION".equalsIgnoreCase(divType)) {
            return MODEL_SOUND_RECORDING;
        } else if ("SOUNDRECORDING".equalsIgnoreCase(divType)) {
            return MODEL_SOUND_UNIT;
        } else if ("SOUNDPART".equalsIgnoreCase(divType)) {
            return MODEL_TRACK;
        }
        throw new ServiceException("Unsupported div type in logical structure: " + divType);
    }

    /**
     * If special genre (cartographic or sheetmusic) was detected in MODS, return MODEL MAP or SHEETMUSIC, otherwise return default model MONOGRAPH
     */
    private String checkSpecialGenreOrMonograph() {
        if (specialGenre.equals(Genre.CARTOGRAPHIC)) {
            return MODEL_MAP;
        } else if (specialGenre.equals(Genre.SHEETMUSIC)) {
            return MODEL_SHEETMUSIC;
        }
        return MODEL_MONOGRAPH;
    }

    private String mapParentRelation(String model) {
        if (MODEL_PERIODICAL_VOLUME.equalsIgnoreCase(model)) {
            return RelsExt.HAS_VOLUME;
        } else if (MODEL_PERIODICAL_ITEM.equalsIgnoreCase(model)) {
            return RelsExt.HAS_ITEM;
        } else if (MODEL_ARTICLE.equalsIgnoreCase(model)) {
            return RelsExt.HAS_INT_COMP_PART;
        } else if (MODEL_SUPPLEMENT.equalsIgnoreCase(model)) {
            return RelsExt.HAS_INT_COMP_PART;
        } else if (MODEL_PICTURE.equalsIgnoreCase(model)) {
            return RelsExt.HAS_INT_COMP_PART;
        } else if (MODEL_INTERNAL_PART.equalsIgnoreCase(model)) {
            return RelsExt.HAS_INT_COMP_PART;
        } else if (MODEL_MONOGRAPH_UNIT.equalsIgnoreCase(model)) {
            return RelsExt.HAS_UNIT;
        } else if (MODEL_SOUND_UNIT.equalsIgnoreCase(model)) {
            return RelsExt.HAS_SOUND_UNIT;
        } else if (MODEL_TRACK.equalsIgnoreCase(model)) {
            return RelsExt.CONTAINS_TRACK;
        }
        throw new ServiceException("Unsupported model mapping in logical structure: " + model);
    }


    private Map<String, Alto> altoMap = new HashMap<String, Alto>();

    private void collectAlto(Foxml parent, DivType div) {
        for (DivType subdiv : div.getDiv()) {
            collectAlto(parent, subdiv);
        }
        for (Fptr fptr : div.getFptr()) {
            AreaType area = fptr.getArea();
            if (area != null) {
                Object fileid = area.getFILEID();
                String begin = area.getBEGIN();
                if (fileid instanceof FileType) {
                    FileType altofile = (FileType) fileid;
                    String id = altofile.getID();
                    String altoStream = filePageMap.get(id) + "/ALTO";
                    parent.appendStruct("<part type=\"" + div.getTYPE() + "\" order=\"" + div.getORDER() + "\" alto=\"" + altoStream + "\" begin=\"" + begin + "\" />\n");
                    //System.out.print("<part type=\""+div.getTYPE()+"\" order=\""+div.getORDER()+"\" alto=\""+altoStream+"\" begin=\""+begin+"\" />\n");
                    Alto alto = getAlto(id);
                    for (Page page : alto.getLayout().getPage()) {
                        for (BlockType block : page.getPrintSpace().getTextBlockOrIllustrationOrGraphicalElement()) {
                            if (begin.equals(block.getID())) {
                                if (block instanceof TextBlockType) {
                                    TextBlockType textBlock = (TextBlockType) block;
                                    for (TextLine line : textBlock.getTextLine()) {
                                        for (Object st : line.getStringAndSP()) {
                                            if (st instanceof StringType) {
                                                StringType stt = (StringType) st;
                                                parent.appendOcr(stt.getCONTENT());
                                                //System.out.print(stt.getCONTENT());
                                            } else if (st instanceof TextBlockType.TextLine.SP) {
                                                parent.appendOcr(" ");
                                                //System.out.print(" ");
                                            }
                                        }
                                        parent.appendOcr("\n");
                                        //System.out.print("\n");
                                    }
                                    parent.appendOcr("\n");
                                    //System.out.print("\n");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Alto getAlto(String id) {
        Alto retval = altoMap.get(id);
        if (retval == null) {
            String fileLoc = fileMap.get(id).getFilename();
            if (fileLoc != null) {
                try {
                    retval = (Alto) unmarshallerALTO.unmarshal(new File(config.getImportFolder() + System.getProperty("file.separator") + fileLoc));
                    altoMap.put(id, retval);
                } catch (JAXBException e) {
                    throw new ServiceException(e);
                }
            }
        }
        return retval;
    }

    protected void processStructLink(StructLink structLink) {
        boolean pagesFirst = KConfiguration.getInstance().getConfiguration().getBoolean("convert.pagesFirst", true);
        for (Object o : structLink.getSmLinkOrSmLinkGrp()) {
            if (o instanceof SmLink) {
                SmLink smLink = (SmLink) o;
                String from = smLink.getFrom();
                String to = smLink.getTo();
                if (from == null || to == null) continue;
                Foxml target = objects.get(to);
                if (target == null && !(to.startsWith("DIV_STOPA") || to.startsWith("DIV_AUDIO"))) {
                    log.warn("Invalid structLink from: " + from + " to: " + to);
                    continue;
                }
                Foxml part = objects.get(from);
                if (part == null) {
                    log.warn("Invalid structLink from: " + from + " to: " + to);
                    continue;
                }
                if (from.startsWith("ISSUE") || from.startsWith("VOLUME") || from.startsWith("SUPPLEMENT")) {
                    if (isReprePage(target.getMods())) {
                        if (part.getFiles() == null || part.getFiles().isEmpty()) {
                            for (FileDescriptor targetFile : target.getFiles()) {
                                if (StreamFileType.USER_IMAGE.equals(targetFile.getFileType()) || StreamFileType.MASTER_IMAGE.equals(targetFile.getFileType())) {
                                    part.addFiles(targetFile);
                                }
                            }
                        }
                    }
                    if (pagesFirst) {
                        part.getRe().insertPage(target.getPid());
                    } else {
                        part.getRe().addRelation(RelsExt.HAS_PAGE, target.getPid(), false);
                    }
                } else if (from.startsWith("SOUNDCOLLECTION") || from.startsWith("SOUNDRECORDING") || from.startsWith("SOUNDPART")) {
                    if (to.startsWith("DIV_STOPA") || to.startsWith("DIV_AUDIO")) {
                        if (from.startsWith("SOUNDPART")) {
                            Collection<FileDescriptor> fileDescriptors = audioFilesMap.get(to);
                            for (FileDescriptor fileDescriptor : fileDescriptors) {
                                if (KConfiguration.getInstance().getConfiguration().getBoolean("convert.userAudio", true)) {
                                    if (StreamFileType.MASTER_AUDIO.equals(fileDescriptor.getFileType())) {
                                        continue;
                                    }
                                } else {
                                    if (StreamFileType.USER_AUDIO.equals(fileDescriptor.getFileType())) {
                                        continue;
                                    }
                                }
                                part.addFiles(fileDescriptor);
                            }
                        } else if (from.startsWith("SOUNDRECORDING")) {
                            Collection<FileDescriptor> fileDescriptors = audioFilesMap.get(to);
                            Foxml track = createTrack(part);
                            for (FileDescriptor fileDescriptor : fileDescriptors) {
                                if (KConfiguration.getInstance().getConfiguration().getBoolean("convert.userAudio", true)) {
                                    if (StreamFileType.MASTER_AUDIO.equals(fileDescriptor.getFileType())) {
                                        continue;
                                    }
                                } else {
                                    if (StreamFileType.USER_AUDIO.equals(fileDescriptor.getFileType())) {
                                        continue;
                                    }
                                }
                                track.addFiles(fileDescriptor);
                            }

                        } else if (from.startsWith("SOUNDCOLLECTION")) {
                            log.warn("Invalid structLink from: " + from + " to: " + to);
                            continue;
                        }

                    } else {
                        if (pagesFirst) {
                            part.getRe().insertPage(target.getPid());
                        } else {
                            part.getRe().addRelation(RelsExt.HAS_PAGE, target.getPid(), false);
                        }
                    }
                } else {
                    part.getRe().addRelation(RelsExt.IS_ON_PAGE, target.getPid(), false);
                    if (soundCollectionId != null) {
                        Foxml soundCollection = objects.get(soundCollectionId);
                        if (soundCollection != null) {
                            if (pagesFirst) {
                                soundCollection.getRe().insertPage(target.getPid());
                            } else {
                                soundCollection.getRe().addRelation(RelsExt.HAS_PAGE, target.getPid(), false);
                            }
                        }
                    }
                }
            }
        }
    }

    private Foxml createTrack(Foxml part) {
        String uuid = generateUUID();
        String pid = pid(uuid);
        String title = getTitlefromMods(part.getMods());
        Foxml track = new Foxml();
        track.setPid(pid);
        track.setTitle(title);
        OaiDcType trackDc = createDC(pid, title);
        setDCModelAndPolicy(trackDc, MODEL_TRACK, policyID);
        track.setDc(trackDc);
        track.setMods(part.getMods());
        RelsExt re = new RelsExt(pid, MODEL_TRACK);
        re.addRelation(RelsExt.POLICY, policyID, true);
        track.setRe(re);
        part.getRe().addRelation(RelsExt.CONTAINS_TRACK, pid, false);
        objects.put(pid, track);
        return track;
    }



    /*
    private String getContract(List<PeriodicalPage> pages) {
        if (pages != null) {
            for (PeriodicalPage page : pages) {
                List<PageRepresentation> reps = page.getPageRepresentation();
                if (reps != null) {
                    for (PageRepresentation rep : reps) {
                        if (rep.getPageImage() != null) {
                            String filename = removeSigla(rep.getPageImage()
                                    .getHref());
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
    }*/

}
