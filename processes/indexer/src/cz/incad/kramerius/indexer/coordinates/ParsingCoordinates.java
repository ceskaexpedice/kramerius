package cz.incad.kramerius.indexer.coordinates;

import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.utils.pid.Lexer;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.Token;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.xpath.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParsingCoordinates {



    private static Logger LOGGER = Logger.getLogger(ParsingCoordinates.class.getName());

    private Token token;
    private Lexer lexer;


    public ParsingCoordinates(String input) throws LexerException {
        this.lexer = new Lexer(input);
    }

    public Pair<Range, Range> simpleParse() throws LexerException {
        this.consume();
        if (this.token.getType().equals(Token.TokenType.LPAREN)) {
            this.consume();
        }
        Range rangef = range();
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.DIV);
        this.consumeWhitespace();
        Range ranges = range();
        if (this.token.getType().equals(Token.TokenType.RPAREN)) {
            this.consume();
        }
        return new ImmutablePair<>(rangef, ranges);
    }

    private Coordinate coordinate() throws LexerException {
        Coordinate.CoordinationType type = null;
        if (token.getValue().equals("e") || token.getValue().equals("E")) {
            type = this.e();
        } else if (token.getValue().equals("w") || token.getValue().equals("W")) {
            type = this.w();
        } else if (token.getValue().equals("n") || token.getValue().equals("N")) {
            type = this.n();
        } else if (token.getValue().equals("s") || token.getValue().equals("S")) {
            type = this.s();
        }

        this.consumeWhitespace();
        Long degrees = number('°');
        this.consumeWhitespace();
        Long minutes = number('´', '\'');
        this.consumeWhitespace();
        Long seconds = number('"','”');
        this.consumeWhitespace();

        if (token.getValue().equals("v") || token.getValue().equals("V")) {
            type = this.vd();
        } else if (token.getValue().equals("s") || token.getValue().equals("S")) {
            type = this.ss();
        } else if (token.getValue().equals("z") || token.getValue().equals("Z")) {
            type = this.zd();
        } else if (token.getValue().equals("j") || token.getValue().equals("j")) {
            type = this.js();
        }
        return new Coordinate(degrees, minutes, seconds, type);

    }

    private Coordinate.CoordinationType e() throws LexerException {
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.ALPHA, 'E');
        this.consumeWhitespace();
        return Coordinate.CoordinationType.VD;
    }

    private Coordinate.CoordinationType w() throws LexerException {
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.ALPHA, 'W');
        this.consumeWhitespace();
        return Coordinate.CoordinationType.ZD;
    }


    private Coordinate.CoordinationType vd() throws LexerException {
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.ALPHA, 'v');
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.DOT);
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.ALPHA, 'd');
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.DOT);
        this.consumeWhitespace();
        return Coordinate.CoordinationType.VD;
    }

    private Coordinate.CoordinationType n() throws LexerException {
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.ALPHA, 'N');
        this.consumeWhitespace();
        return Coordinate.CoordinationType.SS;
    }

    private Coordinate.CoordinationType s() throws LexerException {
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.ALPHA, 'S');
        this.consumeWhitespace();
        return Coordinate.CoordinationType.JS;
    }

    private Coordinate.CoordinationType ss() throws LexerException {
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.ALPHA, 's');
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.DOT);
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.ALPHA, 'š');
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.DOT);
        this.consumeWhitespace();
        return Coordinate.CoordinationType.SS;
    }

    private Coordinate.CoordinationType js() throws LexerException {
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.ALPHA, 'j');
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.DOT);
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.ALPHA, 'š');
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.DOT);
        this.consumeWhitespace();
        return Coordinate.CoordinationType.JS;
    }

    private Coordinate.CoordinationType zd() throws LexerException {
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.ALPHA, 'z');
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.DOT);
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.ALPHA, 'd');
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.DOT);
        this.consumeWhitespace();
        return Coordinate.CoordinationType.ZD;
    }

    private void matchToken(Token.TokenType expecting, char ... expectingChars) throws LexerException {
        if (this.token.getType() == expecting) {
            if (expectingChars.length == 0) {
                this.consume();
            } else {
                for (char ch : expectingChars) {
                    String value = this.token.getValue();
                    if (value.equals(""+ch)) {
                        this.consume();
                        return;
                    }
                }
                throw new LexerException("Expecting type '"+expecting+"' and expecting characters are "+Arrays.asList(expectingChars));
            }
        } else {
            throw new LexerException("Expecting type '"+expecting+"' and expecting characters are "+Arrays.asList(expectingChars));
        }
    }

    private Range range() throws LexerException {
        this.consumeWhitespace();
        Coordinate from = coordinate();
        this.consumeWhitespace();
        this.matchToken(Token.TokenType.MINUS );
        this.matchToken(Token.TokenType.MINUS);
        this.consumeWhitespace();
        Coordinate to = coordinate();
        this.consumeWhitespace();
        return new Range(from, to);
    }

    private void consumeWhitespace() throws LexerException {
        List<Token.TokenType> types = Arrays.asList(new Token.TokenType[]{
                Token.TokenType.SPACE,
                Token.TokenType.TAB,
        });
        while(types.contains(this.token.getType()) ) {
            this.consume();
            if (this.token.getType().equals(Token.TokenType.EOI)) return;
        }
    }


    public void consume() throws LexerException {
        this.token = this.lexer.readToken();
    }

    private Long number(char ... expectingEndChars) throws LexerException {
        StringBuilder builder = new StringBuilder();
        List<Token.TokenType> types = Arrays.asList(new Token.TokenType[]{
                Token.TokenType.DIGIT
        });
        while(types.contains(this.token.getType())) {
            builder.append(this.token.getValue());
            this.consume();
        }
        this.matchToken(Token.TokenType.ALPHA, expectingEndChars);
        return new Long(builder.toString());
    }


    public static List<String> processBibloModsCoordinates(Document doc, XPathFactory factory) throws XPathExpressionException, LexerException {
        List<String> retvals = new ArrayList<>();
        try {
            XPath xPath = factory.newXPath();
            xPath.setNamespaceContext(new FedoraNamespaceContext());
            XPathExpression compiled = xPath.compile("//mods:coordinates/text()");
            Text node = (Text) compiled.evaluate(doc, XPathConstants.NODE);
            if (node != null) {
                String wholeText = node.getData();
                ParsingCoordinates coordinates = new ParsingCoordinates(wholeText);
                Pair<Range, Range> rangeRangePair = coordinates.simpleParse();

                StringBuilder builder = new StringBuilder("<field name=\"range\">ENVELOPE(");
                builder.append(rangeRangePair.getLeft().getFrom().getCoordinate()).append(',');
                builder.append(rangeRangePair.getLeft().getTo().getCoordinate()).append(',');
                builder.append(rangeRangePair.getRight().getFrom().getCoordinate()).append(',');
                builder.append(rangeRangePair.getRight().getTo().getCoordinate()).append(')');
                builder.append("</field>");

                retvals.add(builder.toString());

                StringBuilder min = new StringBuilder();
                min.append(rangeRangePair.getRight().getTo().getCoordinate()).append(',');
                min.append(rangeRangePair.getLeft().getFrom().getCoordinate());


                StringBuilder max = new StringBuilder();
                max.append(rangeRangePair.getRight().getFrom().getCoordinate()).append(',');
                max.append(rangeRangePair.getLeft().getTo().getCoordinate());

                builder = new StringBuilder("<field name=\"location\">");
                builder.append(min);
                builder.append("</field>");
                retvals.add(builder.toString());

                builder = new StringBuilder("<field name=\"location\">");
                builder.append(max);
                builder.append("</field>");
                retvals.add(builder.toString());


                // central point indexing
                double toX  = rangeRangePair.getRight().getFrom().getCoordinate();
                double fromX = rangeRangePair.getRight().getTo().getCoordinate();

                double fromY = rangeRangePair.getLeft().getFrom().getCoordinate();
                double toY = rangeRangePair.getLeft().getTo().getCoordinate();

                builder = new StringBuilder("<field name=\"center\">");
                builder.append((fromX + ((toX - fromX) / 2 ))).append(",").append((fromY + ((toY - fromY) / 2 )));
                builder.append("</field>");
                retvals.add(builder.toString());


            }
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        } catch (DOMException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        } catch (LexerException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
        return retvals;
    }

}
