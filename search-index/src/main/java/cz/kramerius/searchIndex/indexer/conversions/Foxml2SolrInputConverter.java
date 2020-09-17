package cz.kramerius.searchIndex.indexer.conversions;

import cz.kramerius.searchIndex.indexer.SolrInput;
import cz.kramerius.searchIndex.indexer.conversions.extraction.*;
import cz.kramerius.searchIndex.indexer.utils.NamespaceRemovingVisitor;
import cz.kramerius.searchIndex.repositoryAccess.nodes.RepositoryNode;
import cz.kramerius.searchIndex.repositoryAccess.nodes.RepositoryNodeManager;
import cz.kramerius.shared.Dom4jUtils;
import cz.kramerius.shared.Title;
import org.dom4j.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
@see https://github.com/ceskaexpedice/kramerius/blob/akubra/processes/indexer/src/cz/incad/kramerius/indexer/res/K4.xslt
 */
public class Foxml2SolrInputConverter {

    private final Set<String> genreStopWords = initGenreStopWords();

    private Set<String> initGenreStopWords() {
        //see https://www.ndk.cz/standardy-digitalizace/metadata
        String[] stopWordsDef = {
                //from DMF periodical 1.8: https://www.ndk.cz/standardy-digitalizace/DMFperiodika_18_final.pdf
                "title", "volume", "issue", "article", "picture", "supplement", "page", "reprePage",
                //from DMF monograph 1.4: https://www.ndk.cz/standardy-digitalizace/DMFmonograf_14_final.pdf
                "title", "volume", "cartographic", "sheetmusic", "chapter", "picture", "supplement", "page", "reprePage",
                //from DMF audio 0.3: https://www.ndk.cz/standardy-digitalizace/DMFzvuk03_web.pdf
                "sound recording", "soundrecording", "sound part", "cover", "booklet", "imgdisc", "soundcollection",
                //from DMF eborn monograph 2.3: https://www.ndk.cz/standardy-digitalizace/DMF_ebornmonografie_2.3.pdf
                "electronic title", "electronic volume", "chapter", "supplement",
                //from DMF eborn periodical 2.3: https://www.ndk.cz/standardy-digitalizace/DMF_ebornperiodika_2.3.pdf
                "electronic_title", "electronic_volume", "electronic_issue", "electronic_article", "supplement"
        };
        Set<String> stopWords = new HashSet<>();
        for (String stopWord : stopWordsDef) {
            stopWords.add(stopWord.toLowerCase());
        }
        return stopWords;
    }

    public void convert(File inFoxmlFile, File outSolrImportFile) throws IOException, DocumentException {
        //System.out.println("processing " + inFoxmlFile.getName());
        Document foxmlDoc = Dom4jUtils.parseXmlFromFile(inFoxmlFile);
        SolrInput solrInput = convert(foxmlDoc, null, null, null, null);
        solrInput.printTo(outSolrImportFile, true);
    }

    public SolrInput convert(Document foxmlDoc, String ocrText, RepositoryNode repositoryNode, RepositoryNodeManager nodeManager, String imgFullMime) throws IOException, DocumentException {
        //remove namespaces before applying xpaths etc
        foxmlDoc.accept(new NamespaceRemovingVisitor(true, true));

        SolrInput solrInput = new SolrInput();

        //datastreams for data extractions
        Element relsExtRootEl = getLatestDatastreamVersionXmlContent(foxmlDoc, "RELS-EXT");
        Element modsRootEl = getLatestDatastreamVersionXmlContent(foxmlDoc, "BIBLIO_MODS");
        //Element dcRootEl = getLatestDatastreamVersionXmlContent(foxmlDoc, "DC");

        //PID
        String pid = Dom4jUtils.stringOrNullFromAttributeByXpath(foxmlDoc.getRootElement(), "/digitalObject/@PID");
        if (pid != null) {
            solrInput.addField("n.pid", pid);
        } else {
            //System.err.println("missing PID");
        }

        //model
        String model = null;
        if (repositoryNode != null) {
            model = repositoryNode.getModel();
        } else {
            //jen pro testy, jinak by to nemelo mit vyznam, kazdy objekt by mel mit krameriusNode
            Attribute rdfModelResource = (Attribute) Dom4jUtils.buildXpath("Description/hasModel/@resource").selectSingleNode(relsExtRootEl);
            if (rdfModelResource != null) {
                model = rdfModelResource.getValue().substring("info:fedora/model:".length());
            } else {
                //System.err.println("missing rdf model resource");
            }
        }
        solrInput.addField("n.model", model);

        //created_date
        addSolrField(solrInput, "n.created", extractProperty(foxmlDoc, "info:fedora/fedora-system:def/model#createdDate"));

        //modified_date
        addSolrField(solrInput, "n.modified", extractProperty(foxmlDoc, "info:fedora/fedora-system:def/view#lastModifiedDate"));

        //root, parent, path, children (own, foster), foster-parents
        if (repositoryNode != null) {
            addSolrField(solrInput, "n.root.pid", repositoryNode.getRootPid());
            addSolrField(solrInput, "n.root.model", repositoryNode.getRootModel());
            if (repositoryNode.getRootTitle() != null) {
                addSolrField(solrInput, "n.root.title", repositoryNode.getRootTitle().value);
                addSolrField(solrInput, "n.root.title.sort",prepareForSorting(repositoryNode.getRootTitle().value) );
            }

            addSolrField(solrInput, "n.own_parent.pid", repositoryNode.getOwnParentPid());
            addSolrField(solrInput, "n.own_parent.model", repositoryNode.getOwnParentModel());

            addSolrField(solrInput, "n.own_pid_path", repositoryNode.getPidPath());
            addSolrField(solrInput, "n.own_model_path", repositoryNode.getModelPath());

            Integer thisObjectsPositionInParent = repositoryNode.getPositionInOwnParent();
            if (thisObjectsPositionInParent != null) {
                addSolrField(solrInput, "n.rels_ext_index.sort", thisObjectsPositionInParent.toString());
            }
            if (repositoryNode.getOwnParentTitle() != null) {
                addSolrField(solrInput, "n.own_parent.title", repositoryNode.getOwnParentTitle().value);
            }
            if (repositoryNode.getPidsOfFosterParents() != null) {
                for (String fosterParent : repositoryNode.getPidsOfFosterParents()) {
                    addSolrField(solrInput, "n.foster_parents.pids", fosterParent);
                }
            }
            //collections
            if (repositoryNode.getPidsOfFosterParentsOfTypeCollection() != null) {
                for (String collection : repositoryNode.getPidsOfFosterParentsOfTypeCollection()) {
                    addSolrField(solrInput, "n.in_collections.direct", collection);
                }
            }
            if (repositoryNode.getPidsOfAnyAncestorsOfTypeCollection() != null) {
                for (String collection : repositoryNode.getPidsOfAnyAncestorsOfTypeCollection()) {
                    addSolrField(solrInput, "n.in_collections", collection);
                }
            }
            //languages from tree, foster trees
            for (String language : repositoryNode.getLanguages()) {
                addSolrField(solrInput, "n.languages.facet", language);
            }
            //authors
            for (String author : repositoryNode.getAuthors()) {
                solrInput.addField("n.authors", author);
                solrInput.addField("n.authors.facet", withFirstLetterInUpperCase(author));
            }

            //pid/model path
            /*addSolrFiled(solrInput, "n.pid_path", krameriusNode.toPidPath());
            addSolrFiled(solrInput, "n.model_path", krameriusNode.toModelPath());*/
            //own, foster children
            /*if (krameriusNode.getPidsOfOwnChildren() != null) {
                for (String ownChild : krameriusNode.getPidsOfOwnChildren()) {
                    addSolrFiled(solrInput, "n.own_children_pids", ownChild);
                }
            }*/
            /*if (krameriusNode.getPidsOfFosterChildren() != null) {
                for (String fosterChild : krameriusNode.getPidsOfFosterChildren()) {
                    addSolrFiled(solrInput, "n.foster_children_pids", fosterChild);
                }
            }*/
            //TODO: deprecated, jen pro testovani a dokud se neprizpusobi staty klient, do produkce zrusit
            //TODO: vsechny tri maji byt pole s own_parent path prvni a ostatni pathy nasledujici
            //addSolrFiled(solrInput, "parent_pid", repositoryNode.getParentPid());
            addSolrField(solrInput, "pid_path", repositoryNode.getPidPath());
            addSolrField(solrInput, "model_path", repositoryNode.getModelPath());
            if (repositoryNode.getPidsOfFosterParents() != null) {
                for (String fosterParent : repositoryNode.getPidsOfFosterParents()) {
                    RepositoryNode fosterParentNode = nodeManager.getKrameriusNode(fosterParent);
                    //addSolrFiled(solrInput, "parent_pid", fosterParent);
                    addSolrField(solrInput, "pid_path", fosterParentNode.getPidPath() + "/" + pid);
                    addSolrField(solrInput, "model_path", fosterParentNode.getModelPath() + "/" + model);
                }
            }
            //level je uroven ve vlastnim strome, pocitano od 0
            if (repositoryNode.getModelPath() != null) {
                Integer level = repositoryNode.getModelPath().split("/").length - 1;
                addSolrField(solrInput, "level", level.toString());
            }
        }

        //titles
        TitlesExtractor titlesExtractor = new TitlesExtractor();
        Title primaryTitle = titlesExtractor.extractPrimaryTitle(modsRootEl, model);
        if (primaryTitle != null) {
            solrInput.addField("n.title.search", primaryTitle.nonsort != null ? primaryTitle.nonsort + primaryTitle.value : primaryTitle.value);
            solrInput.addField("n.title.sort", prepareForSorting(primaryTitle.value));
        }
        List<Title> allTitles = titlesExtractor.extractAllTitles(modsRootEl, model);
        for (Title title : allTitles) {
            solrInput.addField("n.titles.search", title.nonsort != null ? title.nonsort + title.value : title.value);
        }

        //keywords
        List<Node> topicEls = Dom4jUtils.buildXpath("mods/subject/topic").selectNodes(modsRootEl);
        for (Node topicEl : topicEls) {
            String keyword = toStringOrNull(topicEl);
            solrInput.addField("n.keywords.search", keyword);
            solrInput.addField("n.keywords.facet", withFirstLetterInUpperCase(keyword));
        }

        //geographicName_search, geographicName_facet
        List<Node> geoNameEls = Dom4jUtils.buildXpath("mods/subject/geographic").selectNodes(modsRootEl);
        for (Node geoNameEl : geoNameEls) {
            String geoName = toStringOrNull(geoNameEl);
            solrInput.addField("n.geographic_names.search", geoName);
            solrInput.addField("n.geographic_names.facet", geoName);
        }

        //physicalLocations
        List<Node> physicalLocationEls = Dom4jUtils.buildXpath("mods/location/physicalLocation").selectNodes(modsRootEl);
        for (Node physicalLocationEl : physicalLocationEls) {
            solrInput.addField("n.physical_locations.facet", toStringOrNull(physicalLocationEl));
        }

        //shelfLocators
        List<Node> shelfLocatorEls = Dom4jUtils.buildXpath("mods/location/shelfLocator").selectNodes(modsRootEl);
        for (Node shelfLocatorEL : shelfLocatorEls) {
            solrInput.addField("n.shelf_locators", toStringOrNull(shelfLocatorEL));
        }

        //genres
        List<Node> genreEls = Dom4jUtils.buildXpath("mods/genre").selectNodes(modsRootEl);
        for (Node genreEl : genreEls) {
            String genre = toStringOrNull(genreEl);
            if (genre != null && !genreStopWords.contains(genre.toLowerCase())) {
                solrInput.addField("n.genres.search", genre);
                solrInput.addField("n.genres.facet", withFirstLetterInUpperCase(genre));
            }
        }

        //publishers
        List<Node> publisherEls = Dom4jUtils.buildXpath("mods/originInfo/publisher").selectNodes(modsRootEl);
        for (Node publisherEl : publisherEls) {
            String publisher = toStringOrNull(publisherEl);
            solrInput.addField("n.publishers.search", publisher);
            solrInput.addField("n.publishers.facet", withFirstLetterInUpperCase(publisher));
        }

        //geolocation
        Node coordinatesEl = Dom4jUtils.buildXpath("mods/subject/cartographics/coordinates").selectSingleNode(modsRootEl);
        if (coordinatesEl != null) {
            new CoordinatesExtractor().extract(coordinatesEl, solrInput);
        }

        // TODO: 2019-08-13 extended fields
        //https://github.com/ceskaexpedice/kramerius/blob/b7b173c3d664d4982483131ff6a547f49d96f47e/indexer/src/cz/incad/kramerius/indexer/ExtendedFields.java


        processDates(modsRootEl, solrInput);

        //n.part.name
        String partName = toStringOrNull(Dom4jUtils.buildXpath("mods/titleInfo/partName").selectSingleNode(modsRootEl));
        if (partName != null) {
            addSolrField(solrInput, "n.part.name", partName);
        }

        //specific for model:pages
        //see https://github.com/ceskaexpedice/kramerius-web-client/issues/250
        if ("page".equals(model)) {
            //TODO: muze byt vice elementu part?
            Element partEl = (Element) Dom4jUtils.buildXpath("mods/part").selectSingleNode(modsRootEl);
            if (partEl != null) {
                String type = Dom4jUtils.stringOrNullFromAttributeByName(partEl, "type");
                if (type != null) {
                    //System.out.println("page type: " + type);
                    addSolrField(solrInput, "n.page.type", type);
                } else {
                    System.err.println("WARNING: no page type for " + pid);
                }
                //TODO: mozne reseni:
                //cislovani se bude indexovat, ale klient pouzije pouze, pokud: vsichni sourozenci maji v indexu cislo (zde strany) a zadne cislo neni vickrat,
                //tim padem bude mozne menit usporadani jen zmenou v zaznamu (MODS) nekolika stranek. Na druhou stranu v tomhle priade bude potrebovat preindexovat vsechny sourozence, jinak hrozi duplikace stejneho cisla
                /*
                String index = toStringOrNull(Dom4jUtils.buildXpath("detail[@type='pageIndex']/number").selectSingleNode(partEl));
                if (index != null) {
                    try {
                        Integer indexInt = Integer.valueOf(index);
                        addSolrFiled(solrInput, "page.number", indexInt.toString());
                    } catch (NumberFormatException e) {
                        //TODO: taky casta situace, ze tam nejsou integery
                        System.err.println("WARNING: page number not integer for " + pid);
                    }
                } else {
                    //TODO: co ted, nejak pocitat? Bud se spolehat na existenci pageIndex, nebo pocitat, zadne kompromisy
                    System.err.println("WARNING: no page number for " + pid);
                }
               */
            }
        }

        //specific for model:periodicalitem (issue)
        if ("periodicalitem".equals(model)) {
            //n.issue.type.*
            IssueTypeExtractor.Type issueType = IssueTypeExtractor.extractFromModsEl(modsRootEl);
            if (issueType != null) {
                if (issueType.sort != null) {
                    addSolrField(solrInput, "n.issue.type.sort", issueType.sort.toString());
                }
                if (issueType.code != null) {
                    addSolrField(solrInput, "n.issue.type.code", issueType.code);
                }
            }
            //n.part.number.*
            String numberFromTitleInfo = toStringOrNull(Dom4jUtils.buildXpath("mods/titleInfo/partNumber").selectSingleNode(modsRootEl));
            if (numberFromTitleInfo != null) {
                addSolrField(solrInput, "n.part.number.str", numberFromTitleInfo);
                try {
                    Integer.valueOf(numberFromTitleInfo);
                    addSolrField(solrInput, "n.part.number.int", numberFromTitleInfo);
                } catch (NumberFormatException e) {
                    //nothing
                }
            } else {
                String numberFromPart = toStringOrNull(Dom4jUtils.buildXpath("mods/part[@type='issue']/detail/number").selectSingleNode(modsRootEl));
                if (numberFromPart == null) {
                    numberFromPart = toStringOrNull(Dom4jUtils.buildXpath("mods/part/detail[@type='issue']/number").selectSingleNode(modsRootEl));
                }

                if (numberFromPart != null) {
                    addSolrField(solrInput, "n.part.number.str", numberFromPart);
                    try {
                        Integer.valueOf(numberFromPart);
                        addSolrField(solrInput, "n.part.number.int", numberFromPart);
                    } catch (NumberFormatException e) {
                        //nothing
                    }
                }
            }
        }

        //specific for model:periodicalvolume
        if ("periodicalvolume".equals(model)) {
            //n.part.name
            DateExtractor dateExtractor = new DateExtractor();
            Element originInfoEl = (Element) Dom4jUtils.buildXpath("mods/originInfo").selectSingleNode(modsRootEl);
            if (originInfoEl != null) {
                DateExtractor.DateInfo fromOriginInfo = dateExtractor.extractFromOriginInfo(originInfoEl);
                if (!fromOriginInfo.isEmpty()) {
                    appendPeriodicalVolumeYear(solrInput, fromOriginInfo);
                } else {
                    String partDate = toStringOrNull(Dom4jUtils.buildXpath("mods/part/date").selectSingleNode(modsRootEl));
                    if (partDate != null) {
                        DateExtractor.DateInfo fromPartDate = dateExtractor.extractFromString(partDate);
                        appendPeriodicalVolumeYear(solrInput, fromPartDate);
                    }
                }
            } else {
                String partDate = toStringOrNull(Dom4jUtils.buildXpath("mods/part/date").selectSingleNode(modsRootEl));
                if (partDate != null) {
                    DateExtractor.DateInfo fromPartDate = dateExtractor.extractFromString(partDate);
                    appendPeriodicalVolumeYear(solrInput, fromPartDate);
                }
            }
            //n.part.number.*
            String numberFromTitleInfo = toStringOrNull(Dom4jUtils.buildXpath("mods/titleInfo/partNumber").selectSingleNode(modsRootEl));
            if (numberFromTitleInfo != null) {
                addSolrField(solrInput, "n.part.number.str", numberFromTitleInfo);
                try {
                    Integer.valueOf(numberFromTitleInfo);
                    addSolrField(solrInput, "n.part.number.int", numberFromTitleInfo);
                } catch (NumberFormatException e) {
                    //nothing
                }
            } else {
                String numberFromPart = toStringOrNull(Dom4jUtils.buildXpath("mods/part[@type='volume']/detail/number").selectSingleNode(modsRootEl));
                if (numberFromPart == null) {
                    numberFromPart = toStringOrNull(Dom4jUtils.buildXpath("mods/part/detail[@type='volume']/number").selectSingleNode(modsRootEl));
                }

                if (numberFromPart != null) {
                    addSolrField(solrInput, "n.part.number.str", numberFromPart);
                    try {
                        Integer.valueOf(numberFromPart);
                        addSolrField(solrInput, "n.part.number.int", numberFromPart);
                    } catch (NumberFormatException e) {
                        //nothing
                    }
                }
            }
        }

        //specific for model:monographunit
        if ("monographunit".equals(model)) {
            //n.part.number.*
            String numberFromTitleInfo = toStringOrNull(Dom4jUtils.buildXpath("mods/titleInfo/partNumber").selectSingleNode(modsRootEl));
            if (numberFromTitleInfo != null) {
                addSolrField(solrInput, "n.part.number.str", numberFromTitleInfo);
                try {
                    Integer.valueOf(numberFromTitleInfo);
                    addSolrField(solrInput, "n.part.number.int", numberFromTitleInfo);
                } catch (NumberFormatException e) {
                    //nothing
                }
            }
        }

        if ("supplement".equals(model)) {
            //n.part.number.*
            String numberFromTitleInfo = toStringOrNull(Dom4jUtils.buildXpath("mods/titleInfo/partNumber").selectSingleNode(modsRootEl));
            if (numberFromTitleInfo != null) {
                addSolrField(solrInput, "n.part.number.str", numberFromTitleInfo);
                try {
                    Integer.valueOf(numberFromTitleInfo);
                    addSolrField(solrInput, "n.part.number.int", numberFromTitleInfo);
                } catch (NumberFormatException e) {
                    //nothing
                }
            }
        }

        if ("collection".equals(model)) {
            //n.collection.desc
            String abstractFromTitleInfo = toStringOrNull(Dom4jUtils.buildXpath("mods/abstract").selectSingleNode(modsRootEl));
            if (abstractFromTitleInfo != null) {
                addSolrField(solrInput, "n.collection.desc", abstractFromTitleInfo);
            }
            //n.collection.is_standalone
            String standaloneStr = toStringOrNull(Dom4jUtils.buildXpath("Description/standalone").selectSingleNode(relsExtRootEl));
            addSolrField(solrInput, "n.collection.is_standalone", Boolean.valueOf(standaloneStr));
        }

        //accessibility
        String policyFromRelsExt = toStringOrNull(Dom4jUtils.buildXpath("Description/policy").selectSingleNode(relsExtRootEl));
        if (policyFromRelsExt != null) {
            String prefix = "policy:";
            if (policyFromRelsExt.startsWith(prefix)) {
                addSolrField(solrInput, "n.accessibility", policyFromRelsExt.substring(prefix.length()));
            } else {
                System.err.println("unexpected content of RELS-EXT policy '" + policyFromRelsExt + "'");
            }
        }

        //donator
        List<Node> hasDonatorEls = Dom4jUtils.buildXpath("Description/hasDonator").selectNodes(relsExtRootEl);
        for (Node hasDonatorEl : hasDonatorEls) {
            Attribute resourceAttr = ((Element) hasDonatorEl).attribute("resource");
            if (resourceAttr != null) {
                String prefix = "info:fedora/donator:";
                String value = resourceAttr.getValue();
                if (value != null && value.length() > prefix.length()) {
                    String donator = value.substring(prefix.length());
                    solrInput.addField("n.donator", donator);
                }
            }
        }

        //n.has_tiles
        String tilesUrlFromRelsExt = toStringOrNull(Dom4jUtils.buildXpath("Description/tiles-url").selectSingleNode(relsExtRootEl));
        addSolrField(solrInput, "n.has_tiles", tilesUrlFromRelsExt != null);

        //n.ds.img_full.mime
        addSolrField(solrInput, "n.ds.img_full.mime", imgFullMime);

        //status
        addSolrField(solrInput, "n.status", extractProperty(foxmlDoc, "info:fedora/fedora-system:def/model#state"));

        //mdt
        String mdt = toStringOrNull(Dom4jUtils.buildXpath("mods/classification[@authority='udc']").selectSingleNode(modsRootEl));
        if (mdt != null) {
            addSolrField(solrInput, "n.mdt", mdt);
        }

        //nddt
        String ddt = toStringOrNull(Dom4jUtils.buildXpath("mods/classification[@authority='ddc']").selectSingleNode(modsRootEl));
        if (ddt != null) {
            addSolrField(solrInput, "n.ddt", ddt);
        }

        //identifiers
        new IdentifiersExtractor().extract(modsRootEl, solrInput);

        //counters
        int countPage = Dom4jUtils.buildXpath("Description/hasPage").selectNodes(relsExtRootEl).size();
        if (countPage > 0) {
            addSolrField(solrInput, "n.count_page", Integer.toString(countPage));
        }
        int countTrack = Dom4jUtils.buildXpath("Description/hasTrack|Description/containsTrack").selectNodes(relsExtRootEl).size();
        if (countTrack > 0) {
            addSolrField(solrInput, "n.count_track", Integer.toString(countTrack));
        }

        //OCR text
        if (ocrText != null && !ocrText.isEmpty()) {
            addSolrField(solrInput, "n.text_ocr", ocrText);
        }

        return solrInput;
    }

    private String prepareForSorting(String value) {
        String result = value.toUpperCase()
                .replaceAll("Á", "A|")
                .replaceAll("Č", "C|")
                .replaceAll("Ď", "D|")
                .replaceAll("É", "E|")
                .replaceAll("Ě", "E|")
                .replaceAll("CH", "H|")
                .replaceAll("Í", "I|")
                .replaceAll("Ň", "N|")
                .replaceAll("Ó", "O|")
                .replaceAll("Ř", "R|")
                .replaceAll("Š", "S|")
                .replaceAll("Ť", "T|")
                .replaceAll("Ú", "U|")
                .replaceAll("Ů", "U|")
                .replaceAll("Ý", "Y|")
                .replaceAll("Ž", "Z|");
        if (result.startsWith("[")) {
            result = result.substring(1);
        }
        return result;
    }

    private String formatDate(Date date) {
        return MyDateTimeUtils.formatForSolr(date);
        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        //sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        return sdf.format(date) + "Z";*/
    }

    private void processDates(Element modsEl, SolrInput solrInput) {
        /*DateParser dateParser = new DateParser(modsEl);
        if (dateParser.getDatum() != null) {
            addSolrFiled(solrInput, "datum", formatDate(dateParser.getDatum()));
        }
        addSolrFiled(solrInput, "datum_str", dateParser.getDatum_str());
        addSolrFiled(solrInput, "datum_begin", dateParser.getDatum_begin());
        addSolrFiled(solrInput, "datum_end", dateParser.getDatum_end());
        addSolrFiled(solrInput, "rok", dateParser.getRok());*/

        //TODO: datum_page - k cemu to je, jak se konstruuje?

        DateExtractor dateExtractor = new DateExtractor();
        Element originInfoEl = (Element) Dom4jUtils.buildXpath("mods/originInfo").selectSingleNode(modsEl);
        if (originInfoEl != null) {
            DateExtractor.DateInfo fromOriginInfo = dateExtractor.extractFromOriginInfo(originInfoEl);
            if (!fromOriginInfo.isEmpty()) {
                appendDateFields(solrInput, fromOriginInfo);
            } else {
                String partDate = toStringOrNull(Dom4jUtils.buildXpath("mods/part/date").selectSingleNode(modsEl));
                if (partDate != null) {
                    DateExtractor.DateInfo fromPartDate = dateExtractor.extractFromString(partDate);
                    appendDateFields(solrInput, fromPartDate);
                }
            }
        } else {
            String partDate = toStringOrNull(Dom4jUtils.buildXpath("mods/part/date").selectSingleNode(modsEl));
            if (partDate != null) {
                DateExtractor.DateInfo fromPartDate = dateExtractor.extractFromString(partDate);
                appendDateFields(solrInput, fromPartDate);
            }
        }
    }

    private void appendDateFields(SolrInput solrInput, DateExtractor.DateInfo dateInfo) {
        //min-max dates
        if (dateInfo.dateMin != null) {
            addSolrField(solrInput, "n.date.min", formatDate(dateInfo.dateMin));
        }
        if (dateInfo.dateMax != null) {
            addSolrField(solrInput, "n.date.max", formatDate(dateInfo.dateMax));
        }
        if (dateInfo.isInstant()) { //instant
            if (dateInfo.instantYear != null) {
                addSolrField(solrInput, "n.date_instant.year", dateInfo.instantYear.toString());
            }
            if (dateInfo.instantMonth != null) {
                addSolrField(solrInput, "n.date_instant.month", dateInfo.instantMonth.toString());
            }
            if (dateInfo.instantDay != null) {
                addSolrField(solrInput, "n.date_instant.day", dateInfo.instantDay.toString());
            }
        } else { //range
            //range start
            if (dateInfo.rangeStartYear != null) {
                addSolrField(solrInput, "n.date_range_start.year", dateInfo.rangeStartYear.toString());
            }
            if (dateInfo.rangeStartMonth != null) {
                addSolrField(solrInput, "n.date_range_start.month", dateInfo.rangeStartMonth.toString());
            }
            if (dateInfo.rangeStartDay != null) {
                addSolrField(solrInput, "n.date_range_start.day", dateInfo.rangeStartDay.toString());
            }
            //range end
            if (dateInfo.rangeEndYear != null) {
                addSolrField(solrInput, "n.date_range_end.year", dateInfo.rangeEndYear.toString());
            }
            if (dateInfo.rangeEndMonth != null) {
                addSolrField(solrInput, "n.date_range_end.month", dateInfo.rangeEndMonth.toString());
            }
            if (dateInfo.rangeEndDay != null) {
                addSolrField(solrInput, "n.date_range_end.day", dateInfo.rangeEndDay.toString());
            }
        }
        //date_str
        if (dateInfo.value != null) { //existuje i hodnota v dateIssued bez @point
            addSolrField(solrInput, "n.date.str", dateInfo.value);
        } else if (dateInfo.valueStart != null || dateInfo.valueEnd != null) { //pouze dateIssued/@point (start a/nebo end)
            appendRangeDateStr(solrInput, dateInfo.valueStart, dateInfo.valueEnd);
        }
    }

    private void appendRangeDateStr(SolrInput solrInput, String start, String end) {
        if (start != null && end != null) {
            if (start.equals(end)) {
                addSolrField(solrInput, "n.date.str", replaceUncertainCharsWithQuestionMark(start));
            } else {
                String value = replaceUncertainCharsWithQuestionMark(start) + " - " + replaceUncertainCharsWithQuestionMark(end);
                addSolrField(solrInput, "n.date.str", value);
            }
        } else if (start != null) {
            String value = replaceUncertainCharsWithQuestionMark(start) + " - ?";
            addSolrField(solrInput, "n.date.str", value);
        } else if (end != null) {
            String value = "? - " + replaceUncertainCharsWithQuestionMark(end);
            addSolrField(solrInput, "n.date.str", value);
        }
    }


    private String replaceUncertainCharsWithQuestionMark(String valueStart) {
        return valueStart.replaceAll("[ux\\-]", "?").trim();
    }


    private void appendPeriodicalVolumeYear(SolrInput solrInput, DateExtractor.DateInfo dateInfo) {
        if (dateInfo.rangeStartYear != null && dateInfo.rangeEndYear != null) {
            if (dateInfo.rangeStartYear.equals(dateInfo.rangeEndYear)) { //1918
                addSolrField(solrInput, "n.part.name", dateInfo.rangeStartYear.toString());
            } else { //1918-1938
                addSolrField(solrInput, "n.part.name", dateInfo.rangeStartYear + " - " + dateInfo.rangeEndYear);
            }
        } else if (dateInfo.rangeStartYear != null) { // 1918-?
            addSolrField(solrInput, "n.part.name", dateInfo.rangeStartYear + " - ?");
        } else if (dateInfo.rangeEndYear != null) { // ?-1938
            addSolrField(solrInput, "n.part.name", "? - " + dateInfo.rangeEndYear);
        } else {
            //nothing
        }
    }

    private void addSolrField(SolrInput solrInput, String name, String value) {
        solrInput.addField(name, value);
    }

    private void addSolrField(SolrInput solrInput, String name, boolean value) {
        solrInput.addField(name, String.valueOf(value));
    }

    private String extractProperty(Document foxmlDoc, String name) {
        Node result = Dom4jUtils.buildXpath("/digitalObject/objectProperties/property[@NAME='" + name + "']/@VALUE").selectSingleNode(foxmlDoc);
        return result == null ? null : toStringOrNull(result);
    }

    private Element getLatestDatastreamVersionXmlContent(Document foxmlDoc, String dsId) {
        //List<Node> versionEls = Dom4jUtils.buildXpath(String.format("/foxml:digitalObject/foxml:datastream[@ID='%s']/foxml:datastreamVersion", dsId)).selectNodes(foxmlDoc);
        List<Node> versionEls = Dom4jUtils.buildXpath(String.format("/digitalObject/datastream[@ID='%s']/datastreamVersion", dsId)).selectNodes(foxmlDoc);
        int maxVersion = -1;
        Element latestVersionEl = null;
        for (Node node : versionEls) {
            Element versionEl = (Element) node;
            String ID = Dom4jUtils.stringOrNullFromAttributeByName(versionEl, "ID");
            int versionNumber = Integer.valueOf(ID.split("\\.")[1]);
            if (latestVersionEl == null || versionNumber > maxVersion) {
                latestVersionEl = versionEl;
            }
        }
        //return (Element) Dom4jUtils.buildXpath("foxml:xmlContent/*").selectSingleNode(latestVersionEl);
        return (Element) Dom4jUtils.buildXpath("xmlContent/*").selectSingleNode(latestVersionEl);
    }

    private String toStringOrNull(Node node) {
        return ExtractorUtils.toStringOrNull(node);
    }

    private String withFirstLetterInUpperCase(String string) {
        return ExtractorUtils.withFirstLetterInUpperCase(string);
    }

}
