package indexer;

import cz.kramerius.searchIndex.indexer.conversions.SolrInputBuilder;
import cz.kramerius.searchIndex.indexer.utils.CzechAlphabetComparator;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//import org.junit.Test;
//import static org.junit.Assert.assertEquals;

public class Foxml2SolrInputConverterTest {

    private final SolrInputBuilder converter = new SolrInputBuilder();
    private final XmlTestBuilder testBuilder = new XmlTestBuilder();

   /* @Test
    public void convert() throws IOException, DocumentException {
        Document foxmlDoc = new FoxmlBuilder()
                .withPid("uuid:123")
                .withMods("<modsCollection><mods>" +
                        "<titleInfo><title>Hovory s TGM</title></titleInfo>" +
                        "</mods></modsCollection>")
                .withDc("<dc>" +
                        "<title>Hovory s T.G. Masarykem</title>" +
                        "<type>model:monograph</type>" +
                        "</dc>")
                .withRelsExt("<RDF><Description about='info:fedora/uuid:123'>" +
                        "<hasModel resource='info:fedora/model:monograph'/>" +
                        "<hasPage resource='info:fedora/uuid:479b487a-39c5-49f0-b601-d27bb460961d'/>" +
                        "<policy>policy:public</policy>" +
                        "<isMemberOfCollection resource='info:fedora/vc:d3f011e3-f1fd-4025-a907-68a860460841'/>" +
                        "</Description></RDF>")
                .build();
        prettyPrint(foxmlDoc);
        SolrInput solrInput = converter.convert(foxmlDoc);
        prettyPrint(solrInput.getDocument());
    }*/

    /*@Test
    public void titleDep() throws IOException, DocumentException {
        Document foxmlDoc = new FoxmlBuilder()
                .withPid("uuid:123")
                .withMods("<modsCollection><mods>" +
                        "<titleInfo>" +
                        "   <title>first titleInfo first title</title>" +
                        "   <title>first titleInfo second title</title>" +
                        "</titleInfo>" +
                        "<titleInfo>" +
                        "   <title>second titleInfo first title</title>" +
                        "   <title>second titleInfo second title</title>" +
                        "</titleInfo>" +
                        "</mods></modsCollection>")
                .build();
        //prettyPrint(foxmlDoc);
        SolrInput solrInput = converter.convert(foxmlDoc);
        //prettyPrint(solrInput.getDocument());

        //just one field dc.title
        assertEquals(1, solrInput.getDocument().selectNodes("//field[@name='dc.title']").size());
        //and contains exactly first titleInfo/title
        assertEquals("first titleInfo first title", solrInput.getDocument().selectNodes("//field[@name='dc.title']").get(0).getStringValue());
    }*/

    /*@Test
    public void creatorsDep() throws IOException, DocumentException {
        Document foxmlDoc = new FoxmlBuilder()
                .withPid("uuid:123")
                .withMods("<modsCollection><mods>" +
                        "    <name type='personal' usage='primary'>" +
                        "        <namePart>Neruda, Jan</namePart>" +
                        "        <namePart type='date'>1890-1938</namePart>" +
                        "        <role>" +
                        "            <roleTerm authority='marcrelator' type='code'>aut</roleTerm>" +
                        "        </role>" +
                        "        <nameIdentifier>jk01021023</nameIdentifier>" +
                        "    </name>" +
                        "    <name type='personal'>" +
                        "        <namePart>Masaryk, Tomáš Garrigue</namePart>" +
                        "        <namePart type='date'>1850-1937</namePart>" +
                        "        <role>" +
                        "            <roleTerm authority='marcrelator' type='code'>aut</roleTerm>" +
                        "        </role>" +
                        "        <nameIdentifier>jk01080472</nameIdentifier>" +
                        "    </name>" +
                        "</mods></modsCollection>")
                .build();
        //prettyPrint(foxmlDoc);
        SolrInput solrInput = converter.convert(foxmlDoc);
        //prettyPrint(solrInput.getDocument());

        //two fields dc.creator
        assertEquals(2, solrInput.getDocument().selectNodes("//field[@name='dc.creator']").size());
        //and contains expected values
        assertEquals("Masaryk, Tomáš Garrigue", solrInput.getDocument().selectNodes("//field[@name='dc.creator']").get(0).getStringValue());
        assertEquals("Neruda, Jan", solrInput.getDocument().selectNodes("//field[@name='dc.creator']").get(1).getStringValue());
    }*/


    private void prettyPrint(Document doc) throws IOException {
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(System.out, format);
        writer.write(doc);
    }

    @Test
    public void regexpMatch() {
        assertTrue("1776".matches("\\d\\d\\d\\d"));
    }

    @Test
    public void sortDiacritics() throws DocumentException, IOException {
        List<String> list = new ArrayList<>();
        list.add("Žák");
        list.add("Čapek");
        list.add("Jirásek");
        Collections.sort(list, new CzechAlphabetComparator());
        assertEquals("Čapek", list.get(0));
        assertEquals("Jirásek", list.get(1));
        assertEquals("Žák", list.get(2));
    }

    @TestFactory
    public Collection<DynamicTest> titleMonograph() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/title-monograph.xml");
    }

    @TestFactory
    public Collection<DynamicTest> titlePeriodical() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/title-periodical.xml");
    }

    @TestFactory
    public Collection<DynamicTest> titlePeriodicalVolume() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/title-periodicalvolume.xml");
    }

    @TestFactory
    public Collection<DynamicTest> titlePage() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/title-page.xml");
    }

    @TestFactory
    public Collection<DynamicTest> titleSort() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/title-sort.xml");
    }

    @TestFactory
    public Collection<DynamicTest> author() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/author.xml");
    }

    @TestFactory
    public Collection<DynamicTest> language() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/language.xml");
    }

    @TestFactory
    public Collection<DynamicTest> publication() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/publication.xml");
    }

    @TestFactory
    public Collection<DynamicTest> modsMisc() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/mods_misc.xml");
    }

    @TestFactory
    public Collection<DynamicTest> genre() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/genre.xml");
    }

    @TestFactory
    public Collection<DynamicTest> geolocation() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/geolocation.xml");
    }

    @TestFactory
    public Collection<DynamicTest> periodicalvolumeVolumeYear() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/periodicalvolume-volume.year.xml");
    }

    @TestFactory
    public Collection<DynamicTest> periodicalitemIssueDate() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/periodicalitem-issue.date.xml");
    }

    @TestFactory
    public Collection<DynamicTest> identifier() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/identifiers.xml");
    }

    @TestFactory
    public Collection<DynamicTest> dateIssuedCorrectIncorrectYear() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/dateIssued-correctIncorrectYear.xml");
    }

    @TestFactory
    public Collection<DynamicTest> dateIssuedGranularityDay() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/dateIssued-granularityDay.xml");
    }

    @TestFactory
    public Collection<DynamicTest> dateIssuedGranularityMonth() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/dateIssued-granularityMonth.xml");
    }

    @TestFactory
    public Collection<DynamicTest> dateIssuedGranularityYear() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/dateIssued-granularityYear.xml");
    }

    @TestFactory
    public Collection<DynamicTest> dateIssuedTextRange() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/dateIssued-textRange.xml");
    }

    @TestFactory
    public Collection<DynamicTest> periodicalitemIssueDatePart() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/periodicalitem-issue.type.xml");
    }

    @TestFactory
    public Collection<DynamicTest> periodicalitemIssueNumber() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/periodicalitem-issue.number.xml");
    }

    @TestFactory
    public Collection<DynamicTest> monographUnit() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/monographunit.xml");
    }

    @TestFactory
    public Collection<DynamicTest> supplement() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/supplement.xml");
    }

    @TestFactory
    public Collection<DynamicTest> collection() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/collection.xml");
    }

    @TestFactory
    public Collection<DynamicTest> page() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/page.xml");
    }

    @TestFactory
    public Collection<DynamicTest> hasTiles() throws DocumentException {
        return testBuilder.buildTests("src/test/resources/xmlTests/rels-ext.xml");
    }


}
