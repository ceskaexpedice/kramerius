package cz.kramerius.searchIndex.indexer.conversions;

import cz.kramerius.searchIndex.indexer.SolrInput;
import cz.kramerius.searchIndex.indexer.conversions.extraction.*;
import cz.kramerius.searchIndex.indexer.utils.NamespaceRemovingVisitor;
import cz.kramerius.searchIndex.indexer.execution.Indexer;
import cz.kramerius.searchIndex.indexer.nodes.RepositoryNode;
import cz.kramerius.searchIndex.indexer.nodes.RepositoryNodeManager;
import cz.kramerius.shared.AuthorInfo;
import cz.kramerius.shared.DateInfo;
import cz.kramerius.shared.Dom4jUtils;
import cz.kramerius.shared.Title;
import org.dom4j.*;

import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static cz.kramerius.searchIndex.indexer.execution.Indexer.*;

/*
@see https://github.com/ceskaexpedice/kramerius/blob/akubra/processes/indexer/src/cz/incad/kramerius/indexer/res/K4.xslt
 */
public class SolrInputBuilder {

    private final Set<String> genreStopWords = initGenreStopWords();
    private final SortingNormalizer sortingNormalizer = new SortingNormalizer();

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

    public void convertFoxmlToSolrInput(File inFoxmlFile, File outSolrImportFile) throws IOException, DocumentException {
        //System.out.println("processing " + inFoxmlFile.getName());
        Document foxmlDoc = Dom4jUtils.parseXmlFromFile(inFoxmlFile);
        SolrInput solrInput = processObjectFromRepository(foxmlDoc, null, null, null, null, null, false);
        solrInput.printTo(outSolrImportFile, true);
    }

    public SolrInput processPageFromPdf(RepositoryNodeManager nodeManager, RepositoryNode parentNode, int pageNumber, String pageOcrText) {
        SolrInput solrInput = new SolrInput();
        String pid = parentNode.getPid() + "_" + pageNumber;
        String model = "page";

        solrInput.addField("pid", pid);
        solrInput.addField("model", model);
        addSolrField(solrInput, "root.pid", parentNode.getRootPid());
        addSolrField(solrInput, "root.model", parentNode.getRootModel());
        if (parentNode.getRootTitle() != null) {
            addSolrField(solrInput, "root.title", parentNode.getRootTitle().toString());
            addSolrField(solrInput, "root.title.sort", sortingNormalizer.normalize(parentNode.getRootTitle().getValueWithoutNonsort()));
        }
        addSolrField(solrInput, "own_parent.pid", parentNode.getPid());
        addSolrField(solrInput, "own_parent.model", parentNode.getModel());

        addSolrField(solrInput, "own_pid_path", parentNode.getPidPath() + "/" + pid);
        addSolrField(solrInput, "own_model_path", parentNode.getModelPath() + "/" + model);


        addSolrField(solrInput, "rels_ext_index.sort", pageNumber);
        if (parentNode.getTitle() != null) {
            addSolrField(solrInput, "own_parent.title", parentNode.getTitle().toString());
        }

        //level je uroven ve vlastnim strome, pocitano od 0
        if (parentNode.getModelPath() != null) {
            Integer level = parentNode.getModelPath().split("/").length;
            addSolrField(solrInput, "level", level.toString());
        }
        //pid_paths
        addSolrField(solrInput, "pid_paths", parentNode.getPidPath() + "/" + pid);
        if (parentNode.getPidsOfFosterParents() != null) {
            for (String fosterParent : parentNode.getPidsOfFosterParents()) {
                RepositoryNode fosterParentNode = nodeManager.getKrameriusNode(fosterParent);
                if (fosterParentNode != null) {
                    addSolrField(solrInput, "pid_paths", fosterParentNode.getPidPath() + "/" + parentNode.getPid() + "/" + pid);
                }
            }
        }

        //collections
        if (parentNode.getPidsOfAnyAncestorsOfTypeCollection() != null) {
            for (String collection : parentNode.getPidsOfAnyAncestorsOfTypeCollection()) {
                addSolrField(solrInput, "in_collections", collection);
            }
        }
        //languages from tree, foster trees
        for (String language : parentNode.getLanguages()) {
            addSolrField(solrInput, "languages.facet", language);
        }
        //authors
        for (AuthorInfo author : parentNode.getPrimaryAuthors()) {
            solrInput.addField("authors", author.getDate() != null ? author.getName() + ", " + author.getDate() : author.getName());
            solrInput.addField("authors.facet", withFirstLetterInUpperCase(author.getName()));
            solrInput.addField("authors.search", author.getName());
        }
        for (AuthorInfo author : parentNode.getOtherAuthors()) {
            solrInput.addField("authors", author.getDate() != null ? author.getName() + ", " + author.getDate() : author.getName());
            solrInput.addField("authors.facet", withFirstLetterInUpperCase(author.getName()));
            solrInput.addField("authors.search", author.getName());
        }

        //dates
        if (parentNode.getDateInfo() != null && !parentNode.getDateInfo().isEmpty()) {
            appendDateFields(solrInput, parentNode.getDateInfo());
        }

        //titles
        solrInput.addField("title.search", String.valueOf(pageNumber));
        solrInput.addField("title.sort", String.valueOf(pageNumber));
        solrInput.addField("titles.search", String.valueOf(pageNumber));

        //page.*
        addSolrField(solrInput, "page.number", String.valueOf(pageNumber));
        addSolrField(solrInput, "page.index", String.valueOf(pageNumber - 1));
        //addSolrField(solrInput, "page.type", "NormalPage");
        //addSolrField(solrInput, "page.placement", "single");


        //optional support for compositeId in SOLR Cloud
        ensureCompositeId(solrInput, parentNode, pid);

        //OCR text
        if (pageOcrText != null && !pageOcrText.isEmpty()) {
            addSolrField(solrInput, "text_ocr", pageOcrText);
        }
        return solrInput;
    }

    public SolrInput processObjectFromRepository(Document foxmlDoc, String ocrText, RepositoryNode repositoryNode, RepositoryNodeManager nodeManager, String imgFullMime, Integer audioLength, boolean setFullIndexationInProgress) throws IOException, DocumentException {
        //remove namespaces before applying xpaths etc
        foxmlDoc.accept(new NamespaceRemovingVisitor(true, true));

        SolrInput solrInput = new SolrInput();

        //indexer version
        addSolrField(solrInput, "indexer_version", Indexer.INDEXER_VERSION);
        if (setFullIndexationInProgress) {
            addSolrField(solrInput, "full_indexation_in_progress", true);
        }

        //datastreams for data extractions
        Element relsExtRootEl = getLatestDatastreamVersionXmlContent(foxmlDoc, "RELS-EXT");
        Element modsRootEl = getLatestDatastreamVersionXmlContent(foxmlDoc, "BIBLIO_MODS");
        //Element dcRootEl = getLatestDatastreamVersionXmlContent(foxmlDoc, "DC");

        //pid
        String pid = Dom4jUtils.stringOrNullFromAttributeByXpath(foxmlDoc.getRootElement(), "/digitalObject/@PID");
        if (pid != null) {
            solrInput.addField("pid", pid);
        } else {
            //System.err.println("missing PID");
        }

        //optional support for compositeId in SOLR Cloud
        ensureCompositeId(solrInput, repositoryNode, pid);


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
        solrInput.addField("model", model);

        //created_date
        addSolrField(solrInput, "created", extractProperty(foxmlDoc, "info:fedora/fedora-system:def/model#createdDate"));

        //modified_date
        addSolrField(solrInput, "modified", extractProperty(foxmlDoc, "info:fedora/fedora-system:def/view#lastModifiedDate"));

        //indexation date (only total reindexation updates this)
        addSolrField(solrInput, "indexed", ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));

        //root, parent, path, children (own, foster), foster-parents
        if (repositoryNode != null) {
            addSolrField(solrInput, "root.pid", repositoryNode.getRootPid());
            addSolrField(solrInput, "root.model", repositoryNode.getRootModel());
            if (repositoryNode.getRootTitle() != null) {
                addSolrField(solrInput, "root.title", repositoryNode.getRootTitle().toString());
                addSolrField(solrInput, "root.title.sort", sortingNormalizer.normalize(repositoryNode.getRootTitle().getValueWithoutNonsort()));
            }

            addSolrField(solrInput, "own_parent.pid", repositoryNode.getOwnParentPid());
            addSolrField(solrInput, "own_parent.model", repositoryNode.getOwnParentModel());

            addSolrField(solrInput, "own_pid_path", repositoryNode.getPidPath());
            addSolrField(solrInput, "own_model_path", repositoryNode.getModelPath());

            Integer thisObjectsPositionInParent = repositoryNode.getPositionInOwnParent();
            if (thisObjectsPositionInParent != null) {
                addSolrField(solrInput, "rels_ext_index.sort", thisObjectsPositionInParent.toString());
            }
            if (repositoryNode.getOwnParentTitle() != null) {
                addSolrField(solrInput, "own_parent.title", repositoryNode.getOwnParentTitle().toString());
            }
            if (repositoryNode.getPidsOfFosterParents() != null) {
                for (String fosterParent : repositoryNode.getPidsOfFosterParents()) {
                    addSolrField(solrInput, "foster_parents.pids", fosterParent);
                }
            }

            //level je uroven ve vlastnim strome, pocitano od 0
            if (repositoryNode.getModelPath() != null) {
                Integer level = repositoryNode.getModelPath().split("/").length - 1;
                addSolrField(solrInput, "level", level.toString());
            }
            //pid_paths (vsechny cesty do pres vsechny rodice - pro kazdeho rodice muze byt x cest, napr. pres nekolik sbirek/hierarchi sbirek)
            for (String path : repositoryNode.getAllPidPathsThroughAllParents()) {
                addSolrField(solrInput, "pid_paths", path);
            }
            //own, foster children
            /*if (krameriusNode.getPidsOfOwnChildren() != null) {
                for (String ownChild : krameriusNode.getPidsOfOwnChildren()) {
                    addSolrField(solrInput, "own_children_pids", ownChild);
                }
            }*/
            /*if (krameriusNode.getPidsOfFosterChildren() != null) {
                for (String fosterChild : krameriusNode.getPidsOfFosterChildren()) {
                    addSolrField(solrInput, "foster_children_pids", fosterChild);
                }
            }*/

            //collections
            if (repositoryNode.getPidsOfFosterParentsOfTypeCollection() != null) {
                for (String collection : repositoryNode.getPidsOfFosterParentsOfTypeCollection()) {
                    addSolrField(solrInput, "in_collections.direct", collection);
                }
            }
            if (repositoryNode.getPidsOfAnyAncestorsOfTypeCollection() != null) {
                for (String collection : repositoryNode.getPidsOfAnyAncestorsOfTypeCollection()) {
                    addSolrField(solrInput, "in_collections", collection);
                }
            }
            //licenses
            List<Node> containsLicenseEls = Dom4jUtils.buildXpath(
                    "Description/containsLicense" + //toto je spravny zapis, ostatni jsou chybne/stara data
                            "|Description/containsLicenses" +
                            "|Description/containsLicence" +
                            "|Description/containsLicences" +
                            "|Description/contains-license" +
                            "|Description/contains-licenses" +
                            "|Description/contains-licence" +
                            "|Description/contains-licences" +
                            "|Description/contains-dnnt-label" +
                            "|Description/contains-dnnt-labels"
            ).selectNodes(relsExtRootEl);
            for (Node containsLicenseEl : containsLicenseEls) {
                String license = containsLicenseEl.getStringValue();
                addSolrField(solrInput, "contains_licenses", license);
            }
            for (String license : repositoryNode.getLicenses()) {
                addSolrField(solrInput, "licenses", license);
            }
            for (String license : repositoryNode.getLicensesOfAncestors()) {
                addSolrField(solrInput, "licenses_of_ancestors", license);
            }
            //languages from tree, foster trees
            for (String language : repositoryNode.getLanguages()) {
                addSolrField(solrInput, "languages.facet", language);
            }
            //authors
            for (AuthorInfo author : repositoryNode.getPrimaryAuthors()) {
                solrInput.addField("authors", author.getDate() != null ? author.getName() + ", " + author.getDate() : author.getName());
                solrInput.addField("authors.facet", withFirstLetterInUpperCase(author.getName()));
                solrInput.addField("authors.search", author.getName());
            }
            for (AuthorInfo author : repositoryNode.getOtherAuthors()) {
                solrInput.addField("authors", author.getDate() != null ? author.getName() + ", " + author.getDate() : author.getName());
                solrInput.addField("authors.facet", withFirstLetterInUpperCase(author.getName()));
                solrInput.addField("authors.search", author.getName());
            }
        }

        //titles
        TitlesExtractor titlesExtractor = new TitlesExtractor();
        Title primaryTitle = titlesExtractor.extractPrimaryTitle(modsRootEl, model);
        if (primaryTitle != null) {
            solrInput.addField("title.search", primaryTitle.toString());
            solrInput.addField("title.sort", sortingNormalizer.normalize(primaryTitle.getValueWithoutNonsort()));
        }
        List<Title> allTitles = titlesExtractor.extractAllTitles(modsRootEl, model);
        for (Title title : allTitles) {
            solrInput.addField("titles.search", title.toString());
        }

        // collection, titles in different languages
        if ("collection".equals(model)) {
            Map<String, List<String>> localizedTitles = titlesExtractor.extractLocalizedTitles(modsRootEl, model);
            localizedTitles.keySet().forEach(lang -> {
                List<String> titles = localizedTitles.get(lang);
                titles.forEach(title -> {
                    solrInput.addField("title.search_" + lang, title);
                });
            });
        }

        //keywords
        System.err.println("processing keywords for " + pid);
        for (String keyword : repositoryNode.getKeywords()) {
            solrInput.addField("keywords.search", keyword);
            solrInput.addField("keywords.facet", withFirstLetterInUpperCase(keyword));
        }

        //geographicName_search, geographicName_facet
        List<Node> geoNameEls = Dom4jUtils.buildXpath("mods/subject/geographic").selectNodes(modsRootEl);
        for (Node geoNameEl : geoNameEls) {
            String geoName = toStringOrNull(geoNameEl);
            solrInput.addField("geographic_names.search", geoName);
            solrInput.addField("geographic_names.facet", geoName);
        }

        //physicalLocations
        List<Node> physicalLocationEls = Dom4jUtils.buildXpath("mods/location/physicalLocation").selectNodes(modsRootEl);
        for (Node physicalLocationEl : physicalLocationEls) {
            solrInput.addField("physical_locations.facet", toStringOrNull(physicalLocationEl));
        }

        //shelfLocators
        List<Node> shelfLocatorEls = Dom4jUtils.buildXpath("mods/location/shelfLocator").selectNodes(modsRootEl);
        for (Node shelfLocatorEL : shelfLocatorEls) {
            solrInput.addField("shelf_locators", toStringOrNull(shelfLocatorEL));
        }

        //genres
        List<Node> genreEls = Dom4jUtils.buildXpath("mods/genre").selectNodes(modsRootEl);
        for (Node genreEl : genreEls) {
            String genre = toStringOrNull(genreEl);
            if (genre != null && !genreStopWords.contains(genre.toLowerCase())) {
                solrInput.addField("genres.search", genre);
                solrInput.addField("genres.facet", withFirstLetterInUpperCase(genre));
            }
        }

        //publishers
        List<Node> publisherEls = Dom4jUtils.buildXpath("mods/originInfo/publisher").selectNodes(modsRootEl);
        for (Node publisherEl : publisherEls) {
            String publisher = toStringOrNull(publisherEl);
            solrInput.addField("publishers.search", publisher);
            solrInput.addField("publishers.facet", withFirstLetterInUpperCase(publisher));
        }

        //publication places
        List<Node> publicationPlaceEls = Dom4jUtils.buildXpath("mods/originInfo/place/placeTerm[@type='text']").selectNodes(modsRootEl);
        for (Node publicationPlaceEl : publicationPlaceEls) {
            String publisher = toStringOrNull(publicationPlaceEl);
            solrInput.addField("publication_places.search", publisher);
            solrInput.addField("publication_places.facet", withFirstLetterInUpperCase(publisher));
        }

        //geolocation
        Node coordinatesEl = Dom4jUtils.buildXpath("mods/subject/cartographics/coordinates").selectSingleNode(modsRootEl);
        if (coordinatesEl != null) {
            new CoordinatesExtractor().extract(coordinatesEl, solrInput, pid);
        }

        //dates
        if (repositoryNode != null) {
            if (repositoryNode.getDateInfo() != null && !repositoryNode.getDateInfo().isEmpty()) {
                appendDateFields(solrInput, repositoryNode.getDateInfo());
            }
        }

        //part.name
        String partName = toStringOrNull(Dom4jUtils.buildXpath("mods/titleInfo/partName").selectSingleNode(modsRootEl));
        if (partName != null) {
            addSolrField(solrInput, "part.name", partName);
        }

        //specific for model:pages
        //see https://github.com/ceskaexpedice/kramerius-web-client/issues/250
        if ("page".equals(model)) {
            //page type
            List<Node> partWithTypeEls = Dom4jUtils.buildXpath("mods/part[@type]").selectNodes(modsRootEl);
            //System.out.println("partWithTypeEls:" + partWithTypeEls.size());
            if (partWithTypeEls.isEmpty()) {
                System.err.println("WARNING: no page type for " + pid);
            } else {
                if (partWithTypeEls.size() > 1) {
                    System.err.println("WARNING: multiple page types for " + pid + ", using first one");
                }
                String type = Dom4jUtils.stringOrNullFromAttributeByName((Element) partWithTypeEls.get(0), "type");
                //System.out.println("type: " + type);
                addSolrField(solrInput, "page.type", type);
            }
            //page number (string)
            String number = Dom4jUtils.stringOrNullFromFirstElementByXpath(modsRootEl, "mods/part/detail[@type='pageNumber']/number|mods/part/detail[@type='page number']/number");
            if (number == null) {
                System.err.println("WARNING: no page number for " + pid);
            } else {
                addSolrField(solrInput, "page.number", number);
            }
            //page index (integer)
            Integer index = Dom4jUtils.integerOrNullFromFirstElementByXpath(modsRootEl, "mods/part/detail[@type='pageIndex']/number");
            if (index == null) {
                System.err.println("WARNING: no page index for " + pid);
            } else {
                addSolrField(solrInput, "page.index", index.toString());
            }
            //page placement (left, right, single)
            List<Node> noteEls = Dom4jUtils.buildXpath("mods/note").selectNodes(modsRootEl);
            for (Node noteEl : noteEls) {
                String note = noteEl.getStringValue();
                boolean found = false;
                if (note != null && !note.isEmpty()) {
                    switch (note) {
                        case "singlePage":
                            addSolrField(solrInput, "page.placement", "single");
                            found = true;
                            break;
                        case "right":
                            addSolrField(solrInput, "page.placement", "right");
                            found = true;
                            break;
                        case "left":
                            addSolrField(solrInput, "page.placement", "left");
                            found = true;
                            break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }

        //specific for model:periodicalitem (issue)
        if ("periodicalitem".equals(model)) {
            //issue.type.*
            IssueTypeExtractor.Type issueType = IssueTypeExtractor.extractFromModsEl(modsRootEl);
            if (issueType != null) {
                if (issueType.sort != null) {
                    addSolrField(solrInput, "issue.type.sort", issueType.sort.toString());
                }
                if (issueType.code != null) {
                    addSolrField(solrInput, "issue.type.code", issueType.code);
                }
            }
            //part.number.*
            String numberFromTitleInfo = toStringOrNull(Dom4jUtils.buildXpath("mods/titleInfo/partNumber").selectSingleNode(modsRootEl));
            if (numberFromTitleInfo != null) {
                addSolrField(solrInput, "part.number.str", numberFromTitleInfo);
                try {
                    addSolrField(solrInput, "part.number.sort", extractLeadingNumber(numberFromTitleInfo));
                } catch (NumberFormatException e) {
                    //nothing
                }
            } else {
                String numberFromPart = toStringOrNull(Dom4jUtils.buildXpath("mods/part[@type='issue']/detail/number").selectSingleNode(modsRootEl));
                if (numberFromPart == null) {
                    numberFromPart = toStringOrNull(Dom4jUtils.buildXpath("mods/part/detail[@type='issue']/number").selectSingleNode(modsRootEl));
                }

                if (numberFromPart != null) {
                    addSolrField(solrInput, "part.number.str", numberFromPart);
                    try {
                        addSolrField(solrInput, "part.number.sort", extractLeadingNumber(numberFromPart));
                    } catch (NumberFormatException e) {
                        //nothing
                    }
                }
            }
        }

        //specific for model:periodicalvolume
        if ("periodicalvolume".equals(model)) {
            //part.number.*
            String numberFromTitleInfo = toStringOrNull(Dom4jUtils.buildXpath("mods/titleInfo/partNumber").selectSingleNode(modsRootEl));
            if (numberFromTitleInfo != null) {
                addSolrField(solrInput, "part.number.str", numberFromTitleInfo);
                try {
                    addSolrField(solrInput, "part.number.sort", extractLeadingNumber(numberFromTitleInfo));
                } catch (NumberFormatException e) {
                    //nothing
                }
            } else {
                String numberFromPart = toStringOrNull(Dom4jUtils.buildXpath("mods/part[@type='volume']/detail/number").selectSingleNode(modsRootEl));
                if (numberFromPart == null) {
                    numberFromPart = toStringOrNull(Dom4jUtils.buildXpath("mods/part/detail[@type='volume']/number").selectSingleNode(modsRootEl));
                }

                if (numberFromPart != null) {
                    addSolrField(solrInput, "part.number.str", numberFromPart);
                    try {
                        addSolrField(solrInput, "part.number.sort", extractLeadingNumber(numberFromPart));
                    } catch (NumberFormatException e) {
                        //nothing
                    }
                }
            }
        }

        //specific for model:monographunit
        if ("monographunit".equals(model)) {
            //part.number.*
            String numberFromTitleInfo = toStringOrNull(Dom4jUtils.buildXpath("mods/titleInfo/partNumber").selectSingleNode(modsRootEl));
            if (numberFromTitleInfo != null) {
                addSolrField(solrInput, "part.number.str", numberFromTitleInfo);
                try {
                    addSolrField(solrInput, "part.number.sort", extractLeadingNumber(numberFromTitleInfo));
                } catch (NumberFormatException e) {
                    //nothing
                }
            }
        }

        if ("supplement".equals(model)) {
            //part.number.*
            String numberFromTitleInfo = toStringOrNull(Dom4jUtils.buildXpath("mods/titleInfo/partNumber").selectSingleNode(modsRootEl));
            if (numberFromTitleInfo != null) {
                addSolrField(solrInput, "part.number.str", numberFromTitleInfo);
                try {
                    addSolrField(solrInput, "part.number.sort", extractLeadingNumber(numberFromTitleInfo));
                } catch (NumberFormatException e) {
                    //nothing
                }
            }
        }

        if ("collection".equals(model)) {
            for (Node node : Dom4jUtils.buildXpath("mods/abstract[@lang]").selectNodes(modsRootEl)) {
                Element nodeElm = (Element) node;
                Attribute attribute = nodeElm.attribute("lang");
                if (attribute != null) {
                    String desc = toStringOrNull(nodeElm);
                    addSolrField(solrInput, "collection.desc_" + attribute.getValue(), desc);
                    addSolrField(solrInput, "collection.desc", desc);
                }
            }
            for (Node node : Dom4jUtils.buildXpath("mods/note[@lang]").selectNodes(modsRootEl)) {
                Element nodeElm = (Element) node;
                Attribute attribute = nodeElm.attribute("lang");
                if (attribute != null) {
                    String desc = decodeXml(toStringOrNull(nodeElm));
                    addSolrField(solrInput, "collection.desc_" + attribute.getValue(), desc);
                    addSolrField(solrInput, "collection.desc", desc);
                }
            }
            String abstractFromTitleInfoNoLang = toStringOrNull(Dom4jUtils.buildXpath("mods/abstract[not(@lang)]").selectSingleNode(modsRootEl));
            if (abstractFromTitleInfoNoLang != null) {
                addSolrField(solrInput, "collection.desc", abstractFromTitleInfoNoLang);
            }
            String noteFromTitleInfoNoLang = toStringOrNull(Dom4jUtils.buildXpath("mods/note[not(@lang)]").selectSingleNode(modsRootEl));
            if (noteFromTitleInfoNoLang != null) {
                addSolrField(solrInput, "collection.desc", decodeXml(noteFromTitleInfoNoLang));
            }


            //collection.is_standalone
            String standaloneStr = toStringOrNull(Dom4jUtils.buildXpath("Description/standalone").selectSingleNode(relsExtRootEl));
            addSolrField(solrInput, "collection.is_standalone", Boolean.valueOf(standaloneStr));
        }

        if ("track".equals(model) && audioLength != null) {
            addSolrField(solrInput, "track.length", audioLength);
        }

        //accessibility
        String policyFromRelsExt = toStringOrNull(Dom4jUtils.buildXpath("Description/policy").selectSingleNode(relsExtRootEl));
        if (policyFromRelsExt != null) {
            String prefix = "policy:";
            if (policyFromRelsExt.startsWith(prefix)) {
                addSolrField(solrInput, "accessibility", policyFromRelsExt.substring(prefix.length()));
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
                    solrInput.addField("donator", donator);
                }
            }
        }

        //has_tiles
        String tilesUrlFromRelsExt = toStringOrNull(Dom4jUtils.buildXpath("Description/tiles-url").selectSingleNode(relsExtRootEl));
        addSolrField(solrInput, "has_tiles", tilesUrlFromRelsExt != null);

        //ds.img_full.mime
        addSolrField(solrInput, "ds.img_full.mime", imgFullMime);

        //mdt
        String mdt = toStringOrNull(Dom4jUtils.buildXpath("mods/classification[@authority='udc']").selectSingleNode(modsRootEl));
        if (mdt != null) {
            addSolrField(solrInput, "mdt", mdt);
        }

        //nddt
        String ddt = toStringOrNull(Dom4jUtils.buildXpath("mods/classification[@authority='ddc']").selectSingleNode(modsRootEl));
        if (ddt != null) {
            addSolrField(solrInput, "ddt", ddt);
        }

        //identifiers
        new IdentifiersExtractor().extract(modsRootEl, solrInput);

        //counters
        int countPage = Dom4jUtils.buildXpath("Description/hasPage|Description/isOnPage").selectNodes(relsExtRootEl).size();
        if (countPage > 0) {
            addSolrField(solrInput, "count_page", Integer.toString(countPage));
        }
        int countTrack = Dom4jUtils.buildXpath("Description/hasTrack|Description/containsTrack").selectNodes(relsExtRootEl).size();
        if (countTrack > 0) {
            addSolrField(solrInput, "count_track", Integer.toString(countTrack));
        }
        int countMonographUnit = Dom4jUtils.buildXpath("Description/hasUnit").selectNodes(relsExtRootEl).size();
        if (countMonographUnit > 0) {
            addSolrField(solrInput, "count_monograph_unit", Integer.toString(countMonographUnit));
        }
        int countSoundUnit = Dom4jUtils.buildXpath("Description/hasSoundUnit").selectNodes(relsExtRootEl).size();
        if (countSoundUnit > 0) {
            addSolrField(solrInput, "count_sound_unit", Integer.toString(countSoundUnit));
        }
        int countIssue = Dom4jUtils.buildXpath("Description/hasItem").selectNodes(relsExtRootEl).size();
        if (countIssue > 0) {
            addSolrField(solrInput, "count_issue", Integer.toString(countIssue));
        }
        int countVolume = Dom4jUtils.buildXpath("Description/hasVolume").selectNodes(relsExtRootEl).size();
        if (countVolume > 0) {
            addSolrField(solrInput, "count_volume", Integer.toString(countVolume));
        }
        int countIntCompPart = Dom4jUtils.buildXpath("Description/hasIntCompPart").selectNodes(relsExtRootEl).size();
        if (countIntCompPart > 0) {
            addSolrField(solrInput, "count_internal_part", Integer.toString(countIntCompPart));
        }

        //OCR text
        if (ocrText != null && !ocrText.isEmpty()) {
            addSolrField(solrInput, "text_ocr", ocrText);
        }

        return solrInput;
    }

    public static String decodeXml(String encodedXml) {
        return StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeXml(encodedXml));
    }

    private Integer extractLeadingNumber(String stringPossiblyStartingWithNumber) {
        if (stringPossiblyStartingWithNumber != null && !stringPossiblyStartingWithNumber.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < stringPossiblyStartingWithNumber.length(); i++) {
                char c = stringPossiblyStartingWithNumber.charAt(i);
                if (Character.isDigit(c)) {
                    builder.append(c);
                } else {
                    break;
                }
            }
            return Integer.valueOf(builder.toString());
        }
        return null;
    }

    private void appendDateFields(SolrInput solrInput, DateInfo dateInfo) {
        //min-max dates
        if (dateInfo.dateMin != null) {
            addSolrField(solrInput, "date.min", formatDate(dateInfo.dateMin));
        }
        if (dateInfo.dateMax != null) {
            addSolrField(solrInput, "date.max", formatDate(dateInfo.dateMax));
        }
        if (dateInfo.isInstant()) { //instant
            if (dateInfo.instantYear != null) {
                //addSolrField(solrInput, "date_instant.year", dateInfo.instantYear.toString());
                addSolrField(solrInput, "date_range_start.year", dateInfo.instantYear.toString());
                addSolrField(solrInput, "date_range_end.year", dateInfo.instantYear.toString());
            }
            if (dateInfo.instantMonth != null) {
                //addSolrField(solrInput, "date_instant.month", dateInfo.instantMonth.toString());
                addSolrField(solrInput, "date_range_start.month", dateInfo.instantMonth.toString());
                addSolrField(solrInput, "date_range_end.month", dateInfo.instantMonth.toString());
            }
            if (dateInfo.instantDay != null) {
                //addSolrField(solrInput, "date_instant.day", dateInfo.instantDay.toString());
                addSolrField(solrInput, "date_range_start.day", dateInfo.instantDay.toString());
                addSolrField(solrInput, "date_range_end.day", dateInfo.instantDay.toString());
            }
        } else { //range
            //range start
            if (dateInfo.rangeStartYear != null) {
                addSolrField(solrInput, "date_range_start.year", dateInfo.rangeStartYear.toString());
            }
            if (dateInfo.rangeStartMonth != null) {
                addSolrField(solrInput, "date_range_start.month", dateInfo.rangeStartMonth.toString());
            }
            if (dateInfo.rangeStartDay != null) {
                addSolrField(solrInput, "date_range_start.day", dateInfo.rangeStartDay.toString());
            }
            //range end
            if (dateInfo.rangeEndYear != null) {
                addSolrField(solrInput, "date_range_end.year", dateInfo.rangeEndYear.toString());
            }
            if (dateInfo.rangeEndMonth != null) {
                addSolrField(solrInput, "date_range_end.month", dateInfo.rangeEndMonth.toString());
            }
            if (dateInfo.rangeEndDay != null) {
                addSolrField(solrInput, "date_range_end.day", dateInfo.rangeEndDay.toString());
            }
        }
        //date_str
        if (dateInfo.value != null) { //existuje i hodnota v dateIssued bez @point
            addSolrField(solrInput, "date.str", dateInfo.value);
        } else if (dateInfo.valueStart != null || dateInfo.valueEnd != null) { //pouze dateIssued/@point (start a/nebo end)
            appendRangeDateStr(solrInput, dateInfo.valueStart, dateInfo.valueEnd);
        }
    }

    private String formatDate(Date date) {
        return MyDateTimeUtils.formatForSolr(date);
        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        //sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        return sdf.format(date) + "Z";*/
    }

    private void appendRangeDateStr(SolrInput solrInput, String start, String end) {
        if (start != null && end != null) {
            if (start.equals(end)) {
                addSolrField(solrInput, "date.str", replaceUncertainCharsWithQuestionMark(start));
            } else {
                String value = replaceUncertainCharsWithQuestionMark(start) + " - " + replaceUncertainCharsWithQuestionMark(end);
                addSolrField(solrInput, "date.str", value);
            }
        } else if (start != null) {
            String value = replaceUncertainCharsWithQuestionMark(start) + " - ?";
            addSolrField(solrInput, "date.str", value);
        } else if (end != null) {
            String value = "? - " + replaceUncertainCharsWithQuestionMark(end);
            addSolrField(solrInput, "date.str", value);
        }
    }

    private String replaceUncertainCharsWithQuestionMark(String valueStart) {
        return valueStart.replaceAll("[ux\\-]", "?").trim();
    }

    private void addSolrField(SolrInput solrInput, String name, Object value) {
        if (value != null) {
            solrInput.addField(name, value.toString());
        }
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
