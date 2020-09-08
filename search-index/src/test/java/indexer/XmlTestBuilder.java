package indexer;

import cz.kramerius.searchIndex.indexer.SolrInput;
import cz.kramerius.searchIndex.indexer.conversions.Foxml2SolrInputConverter;
import cz.kramerius.searchIndex.indexer.conversions.extraction.AuthorsExtractor;
import cz.kramerius.searchIndex.indexer.conversions.extraction.LanguagesExtractor;
import cz.kramerius.searchIndex.repositoryAccess.nodes.RepositoryNode;
import cz.kramerius.shared.Dom4jUtils;
import org.dom4j.*;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XmlTestBuilder {
    private final Foxml2SolrInputConverter converter = new Foxml2SolrInputConverter();

    private List<XmlTest> extractTests(String testType, String model, List<Node> testEls) {
        List<XmlTest> result = new ArrayList<>();
        for (Node testEl : testEls) {
            //description
            Attribute nameAttr = (Attribute) testEl.selectSingleNode("@name");
            String name = nameAttr != null ? nameAttr.getStringValue().trim() : null;

            //description
            Element descriptionEl = (Element) testEl.selectSingleNode("desc");
            String description = descriptionEl != null ? descriptionEl.getStringValue().trim() : null;

            //input
            List<Node> inEls = testEl.selectNodes("in/*");
            Document inDoc = buildInDoc(testType, inEls);
            //System.out.println(inDoc.asXML());

            //expected output
            List<Node> outFields = testEl.selectNodes("out/field");
            Document outDoc = buildOutDoc(outFields);
            //System.out.println(outDoc.asXML());

            XmlTest test = new XmlTest(name, description, testType, model, inDoc, outDoc);
            result.add(test);
        }
        return result;
    }

    private Document buildInDoc(String testType, List<Node> inEls) {
        if ("mods".matches(testType)) {
            Document doc = DocumentHelper.createDocument();
            Element docEl = doc.addElement("modsCollection").addElement("mods");
            for (Node inEl : inEls) {
                inEl.detach();
                docEl.add(inEl);
            }
            return doc;
        } else if ("rels-ext".matches(testType)) {
            Document doc = DocumentHelper.createDocument();
            Element docEl = doc.addElement("RDF").addElement("Description");
            for (Node inEl : inEls) {
                inEl.detach();
                docEl.add(inEl);
            }
            return doc;
        } else {
            throw new IllegalArgumentException("unknown test type '" + testType + "'");
        }
    }

    private Document buildOutDoc(List<Node> fieldEls) {
        Document doc = DocumentHelper.createDocument();
        Element docEl = doc.addElement("add").addElement("doc");
        for (Node fieldEl : fieldEls) {
            fieldEl.detach();
            docEl.add(fieldEl);
        }
        return doc;
    }

    public List<DynamicTest> buildTests(String testXmlPath) throws DocumentException {
        File testFile = new File(testXmlPath);
        Document testDoc = Dom4jUtils.parseXmlFromFile(testFile);
        String testType = testDoc.selectSingleNode("/testSuite/@testType").getStringValue().trim();
        String model = testDoc.selectSingleNode("/testSuite/@model").getStringValue().trim();

        List<Node> testEls = testDoc.selectNodes("/testSuite/test");
        List<XmlTest> tests = extractTests(testType, model, testEls);
        List<DynamicTest> result = new ArrayList<>();

        for (XmlTest test : tests) {
            // TODO: 2019-08-20 jmena testu se nepouzivaji, viz https://github.com/gradle/gradle/issues/5975
            DynamicTest dTest = DynamicTest.dynamicTest(test.getName(), buildTestExec(test));
            result.add(dTest);
        }
        return result;
    }

    private Executable buildTestExec(XmlTest test) throws DocumentException {
        switch (test.getTestType()) {
            case "mods":
                return buildModsTestExec(test);
            case "rels-ext":
                return buildRelsExtTestExec(test);
            default:
                throw new RuntimeException("unknown test type: " + test.getTestType());
        }
    }

    private Executable buildModsTestExec(XmlTest test) throws DocumentException {
        Executable exec = () -> {
            System.out.println("Spouštím test: " + test.getName());
            if (test.getDescription() != null && !test.getDescription().trim().isEmpty()) {
                System.out.println(test.getDescription().trim());
            }
            System.out.println();

            Document foxmlDoc = new FoxmlBuilder()
                    .withMods(test.getInDoc().asXML())
                    .build();

            List<String> languages = new LanguagesExtractor().extractLanguages(test.getInDoc().getRootElement(), null);
            List<String> authors = new AuthorsExtractor().extractAuthors(test.getInDoc().getRootElement(), null);
            RepositoryNode node = new RepositoryNode(null, test.getDocType(), null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, languages, authors
            );
            SolrInput solrInput = converter.convert(foxmlDoc, null, node, null);
            SolrInput cleared = withoutFields(solrInput,
                    "n.model",
                    "n.root.pid",
                    "n.root.title",
                    "n.root.model",
                    "n.own_pid_path",
                    "n.own_model_path",
                    "n.has_tiles",

                    "fedora.model",
                    "root_pid",
                    "root_model",
                    "root_title",
                    "parent_pid",
                    "pid_path",
                    "model_path",
                    "level");
            assertEquals(test.getOutDoc().asXML(), cleared.getDocument().asXML());
        };
        return exec;
    }

    private Executable buildRelsExtTestExec(XmlTest test) throws DocumentException {
        Executable exec = () -> {
            System.out.println("Spouštím test: " + test.getName());
            if (test.getDescription() != null && !test.getDescription().trim().isEmpty()) {
                System.out.println(test.getDescription().trim());
            }
            System.out.println();

            Document foxmlDoc = new FoxmlBuilder()
                    .withRelsExt(test.getInDoc().asXML())
                    .build();
            RepositoryNode node = new RepositoryNode(null, test.getDocType(), null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null,
                    null, null, null
            );
            SolrInput solrInput = converter.convert(foxmlDoc, null, node, null);
            SolrInput cleared = withoutFields(solrInput,
                    "n.model",
                    /*"n.root.pid",
                    "n.root.title",
                    "n.root.model",
                    "n.own_pid_path",
                    "n.own_model_path",
                    "n.has_tiles",*/

                    "fedora.model",
                    "root_pid",
                    "root_model",
                    "root_title",
                    "parent_pid",
                    "pid_path",
                    "model_path",
                    "level");
            assertEquals(test.getOutDoc().asXML(), cleared.getDocument().asXML());
        };
        return exec;
    }


    private SolrInput withoutFields(SolrInput solrInput, String... fieldsToIgnore) {
        Set<String> toIgnore = new HashSet<>();
        toIgnore.addAll(Arrays.asList(fieldsToIgnore));
        SolrInput result = new SolrInput();
        Map<String, List<String>> fields = solrInput.getFieldsCopy();
        for (String fieldName : fields.keySet()) {
            if (!toIgnore.contains(fieldName)) {
                for (String value : fields.get(fieldName)) {
                    result.addField(fieldName, value);
                }
            }
        }
        return result;
    }
}

