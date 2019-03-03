package cz.incad.kramerius.indexer.coordinates;

import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class CoordinatesTest extends TestCase {

    public void testParsing() throws LexerException {
        String sample = "( 014°20´55\" v.d. -- 014°20´55\" v. d. / 046°40´51\" s.š.--046°40´51\" s.š. )";
        ParsingCoordinates coordinates = new ParsingCoordinates(sample);
        Pair<Range, Range> rangeRangePair = coordinates.simpleParse();
        Range left = rangeRangePair.getLeft();
        Range right = rangeRangePair.getRight();

        Assert.assertTrue(left.getFrom().getCoordinate() == 14.34861);
        Assert.assertTrue(left.getTo().getCoordinate() == 14.34861);

        Assert.assertTrue(right.getFrom().getCoordinate() == 46.68084);
        Assert.assertTrue(right.getTo().getCoordinate() == 46.68084);

    }

    public void testParsing2() throws LexerException {
        String sample = " 014° 20´  55\" v.d. -- 014°20´55\" v. d. / 046°40´51\" s. š.--046°40´51\" s.š.  ";
        ParsingCoordinates coordinates = new ParsingCoordinates(sample);
        Pair<Range, Range> rangeRangePair = coordinates.simpleParse();
        Range left = rangeRangePair.getLeft();
        Range right = rangeRangePair.getRight();

        Assert.assertTrue(left.getFrom().getCoordinate() == 14.34861);
        Assert.assertTrue(left.getTo().getCoordinate() == 14.34861);

        Assert.assertTrue(right.getFrom().getCoordinate() == 46.68084);
        Assert.assertTrue(right.getTo().getCoordinate() == 46.68084);

    }


    //(E 12°02'00"--E 19°11'00"/N 51°03'00"--N 48°31'00”)

    public void testParsing3() throws LexerException {
        String sample = "(E 12°02'00\"--E 19°11'00\"/N 51°03'00\"--N 48°31'00”) ";
        ParsingCoordinates coordinates = new ParsingCoordinates(sample);
        Pair<Range, Range> rangeRangePair = coordinates.simpleParse();
        Range left = rangeRangePair.getLeft();

        Range right = rangeRangePair.getRight();
        System.out.println(right.getFrom().getCoordinate());
        System.out.println(right.getTo().getCoordinate());

        Assert.assertTrue(left.getFrom().getCoordinate() == 12.03333);
        Assert.assertTrue(right.getFrom().getCoordinate() == 51.05);
    }

    public void testProcess() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, LexerException {
        InputStream stream = CoordinatesTest.class.getClassLoader().getResourceAsStream("cz/incad/kramerius/indexer/res/coordinates_mods.xml");
        Document document = XMLUtils.parseDocument(stream, true);
        List<String> strings = ParsingCoordinates.processBibloModsCoordinates(document, XPathFactory.newInstance());
        Assert.assertTrue(strings.size() == 4);
    }

    public void testProcess2() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, LexerException {
        InputStream stream = CoordinatesTest.class.getClassLoader().getResourceAsStream("cz/incad/kramerius/indexer/res/coordinates_mods_2.xml");
        Document document = XMLUtils.parseDocument(stream, true);
        List<String> strings = ParsingCoordinates.processBibloModsCoordinates(document, XPathFactory.newInstance());
        Assert.assertTrue(strings.size() == 4);
    }

    public void testProcess3() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, LexerException {
        InputStream stream = CoordinatesTest.class.getClassLoader().getResourceAsStream("cz/incad/kramerius/indexer/res/coordinates_mods_3.xml");
        Document document = XMLUtils.parseDocument(stream, true);
        List<String> strings = ParsingCoordinates.processBibloModsCoordinates(document, XPathFactory.newInstance());
        Assert.assertTrue(strings.size() == 4);
    }

    public void testProcess4() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, LexerException {
        InputStream stream = CoordinatesTest.class.getClassLoader().getResourceAsStream("cz/incad/kramerius/indexer/res/coordinates_mods_4.xml");
        Document document = XMLUtils.parseDocument(stream, true);
        List<String> strings = ParsingCoordinates.processBibloModsCoordinates(document, XPathFactory.newInstance());
        Assert.assertTrue(strings.size() == 0);
    }

}
