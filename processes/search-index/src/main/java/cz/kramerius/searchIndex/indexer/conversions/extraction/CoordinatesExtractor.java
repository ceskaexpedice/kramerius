package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.searchIndex.indexer.SolrInput;
import org.dom4j.Node;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoordinatesExtractor {

    public void extract(Node coordinatesEl, SolrInput solrInput, String pid) {
        String coordinatesStr = toStringOrNull(coordinatesEl);
        if (coordinatesStr != null) {
            BoundingBox bb = extractBoundingBox(coordinatesStr, pid);
            if (bb != null) {
                //ignore bbox if it is defined with invalid value
                if (bb.n < -90 || bb.n > 90 || bb.s < -90 || bb.s > 90) { //neplatná zeměpisná šírka: nenachází se v intervalu <-90;90>
                    System.err.println(String.format("Chyba v datech objektu %s: neplatná zeměpisná šírka: nenachází se v intervalu <-90;90>: %s", pid, coordinatesStr));
                    bb = null;
                } else if (bb.w < -180 || bb.w > 180 || bb.e < -180 || bb.e > 180) { //neplatná zeměpisná délka: nenachází se v intervalu <-180;180>
                    System.err.println(String.format("Chyba v datech objektu %s: neplatná zeměpisná délka: nenachází se v intervalu <-180;180>: %s", pid, coordinatesStr));
                    bb = null;
                }
                //ignore bbox that does not define rectangle
                else if (bb.s > bb.n) {//neplatná zeměpisná šířka: jižní ohraničení je větší než severní ohraničení
                    System.err.println(String.format("Chyba v datech objektu %s: neplatná zeměpisná šířka: jižní ohraničení je větší než severní ohraničení: %s", pid, coordinatesStr));
                    bb = null;
                } /*else if (bb.w > bb.e) {//neplatná zeměpisná délka: západní ohraničení je větší než východní ohraničení
                    //tohle je platné, např. Čukotský autonomní okruh (157° - -169° , nebo Nový Zéland včetně východních ostrovů (166° - -172°)
                    System.err.println(String.format("Chyba v datech objektu %s: neplatná zeměpisná šířka: západní ohraničení je větší než východní ohraničení: %s", pid, coordinatesStr));
                    bb = null;
                }*/
            }
            if (bb != null) {
                Locale locale = new Locale("en", "US");
                //ENVELOPE(minX, maxX, maxY, minY)
                solrInput.addField("coords.bbox", String.format(locale, "ENVELOPE(%.6f,%.6f,%.6f,%.6f)", bb.w, bb.e, bb.n, bb.s));
                solrInput.addField("coords.bbox.center", String.format(locale, "%.6f,%.6f", (bb.n + bb.s) / 2, (bb.w + bb.e) / 2));
                solrInput.addField("coords.bbox.corner_sw", String.format(locale, "%.6f,%.6f", bb.s, bb.w));
                solrInput.addField("coords.bbox.corner_ne", String.format(locale, "%.6f,%.6f", bb.n, bb.e));
            }
        }
    }

    private BoundingBox extractBoundingBox(String coordinatesStr, String pid) {
        BoundingBox bbIn = parseBoundingBoxInternational(coordinatesStr);
        if (bbIn != null) {
            return bbIn;
        } else {
            BoundingBox bbCz = parseBoundingBoxCzech(coordinatesStr);
            if (bbCz != null) {
                return bbCz;
            } else {
                System.err.println(String.format("Chyba v datech objektu %s: nelze parsovat souřadnice: %s", pid, coordinatesStr));
                return null;
            }
        }
    }

    private BoundingBox parseBoundingBoxInternational(String coordinatesStr) {
        //(E 12°02'00"--E 19°11'00"/N 51°03'00"--N 48°31'00")
        coordinatesStr = coordinatesStr.replaceAll("''", "\""); //E 12°02'00''
        Pattern pattern = Pattern.compile("^\\(?" +
                "([E,W])\\s(\\d{1,3})°(\\d{1,2})'(\\d{1,2})\"\\s*-{1,2}\\s*([E,W])\\s(\\d{1,3})°(\\d{1,2})'(\\d{1,2})\"" +
                "/" +
                "([N,S])\\s(\\d{1,3})°(\\d{1,2})'(\\d{1,2})\"\\s*-{1,2}\\s*([N,S])\\s(\\d{1,3})°(\\d{1,2})'(\\d{1,2})\"" +
                "\\)?$");
        Matcher m = pattern.matcher(coordinatesStr);
        if (m.find()) {
            BoundingBox result = new BoundingBox();
            result.w = coordinate(m.group(1), m.group(2), m.group(3), m.group(4));
            result.e = coordinate(m.group(5), m.group(6), m.group(7), m.group(8));
            result.n = coordinate(m.group(9), m.group(10), m.group(11), m.group(12));
            result.s = coordinate(m.group(13), m.group(14), m.group(15), m.group(16));
            return result;
        } else {
            return null;
        }
    }

    private BoundingBox parseBoundingBoxCzech(String coordinatesStr) {
        //(16°33´47" v.d.--16°39´07" v.d./49°14´06" s.š.--49°10´18" s.š.)
        coordinatesStr = coordinatesStr.replaceAll("''", "\""); //012°57´53'' v.d.
        if (coordinatesStr.endsWith("].")) { //(014°02´08" v.d.--014°48´09" v.d./050°15´16" s.š.--049°55´22" s.š.)].
            coordinatesStr = coordinatesStr.substring(0, coordinatesStr.length() - "].".length());
        }
        Pattern pattern = Pattern.compile("^\\(?" +
                "(\\d{1,3})°(\\d{1,2})[´'](\\d{1,2})\"\\s?([v,z])\\.d\\.\\s*-{1,2}\\s*(\\d{1,3})°(\\d{1,2})[´'](\\d{1,2})\"\\s?([v,z])\\.d\\." +
                "/" +
                "(\\d{1,3})°(\\d{1,2})[´'](\\d{1,2})\"\\s?([s,j])\\.š\\.\\s*-{1,2}\\s*(\\d{1,3})°(\\d{1,2})[´'](\\d{1,2})\"\\s?([s,j])\\.š\\." +
                "\\)?$");
        Matcher m = pattern.matcher(coordinatesStr);
        if (m.matches()) {
            BoundingBox result = new BoundingBox();
            result.w = coordinate(m.group(4), m.group(1), m.group(2), m.group(3));
            result.e = coordinate(m.group(8), m.group(5), m.group(6), m.group(7));
            result.n = coordinate(m.group(12), m.group(9), m.group(10), m.group(11));
            result.s = coordinate(m.group(16), m.group(13), m.group(14), m.group(15));
            return result;
        } else {
            return null;
        }
    }

    private String toStringOrNull(Node node) {
        return ExtractorUtils.toStringOrNull(node);
    }

    private double coordinate(String xStr, String d, String m, String s) {
        double c = calcCoordinate(Integer.valueOf(d), Integer.valueOf(m), Integer.valueOf(s));
        char x = xStr.charAt(0);
        if (x == 'W' || x == 'z' || x == 'S' || x == 'j') {
            c = -1d * c;
        }
        return c;
    }

    private double calcCoordinate(int d, int m, int s) {
        return d + m / 60d + s / 3600d;
    }

    static class BoundingBox {
        double w;
        double e;
        double n;
        double s;
    }
}
