package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.searchIndex.indexer.SolrInput;
import org.dom4j.Node;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoordinatesExtractor {

    public void extract(Node coordinatesEl, SolrInput solrInput) {
        String coordinatesStr = toStringOrNull(coordinatesEl);
        if (coordinatesStr != null) {
            BoundingBox bb = extractBoundingBox(coordinatesStr);
            if (bb != null) {
                Locale locale = new Locale("en", "US");
                solrInput.addField("n.coords.bbox", String.format(locale, "ENVELOPE(%.6f,%.6f,%.6f,%.6f)", bb.w, bb.e, bb.n, bb.s));
                solrInput.addField("n.coords.bbox.center", String.format(locale, "%.6f,%.6f", (bb.n + bb.s) / 2, (bb.w + bb.e) / 2));
                solrInput.addField("n.coords.bbox.corner_sw", String.format(locale, "%.6f,%.6f", bb.s, bb.w));
                solrInput.addField("n.coords.bbox.corner_ne", String.format(locale, "%.6f,%.6f", bb.n, bb.e));
            }
        }
    }

    private BoundingBox extractBoundingBox(String coordinatesStr) {
        BoundingBox bbIn = parseBoundingBoxInternational(coordinatesStr);
        if (bbIn != null) {
            return bbIn;
        } else {
            BoundingBox bbCz = parseBoundingBoxCzech(coordinatesStr);
            if (bbCz != null) {
                return bbCz;
            } else {
                // TODO: 05/09/2019: not any of those formats, best to inform user through indexation process log
                return null;
            }
        }
    }

    private BoundingBox parseBoundingBoxInternational(String coordinatesStr) {
        //(E 12°02'00"--E 19°11'00"/N 51°03'00"--N 48°31'00")
        //System.out.println(coordinatesStr);
        Pattern pattern = Pattern.compile("^\\(" +
                "([E,W])\\s(\\d{1,3})°(\\d{1,2})'(\\d{1,2})\"--([E,W])\\s(\\d{1,3})°(\\d{1,2})'(\\d{1,2})\"" +
                "/" +
                "([N,S])\\s(\\d{1,3})°(\\d{1,2})'(\\d{1,2})\"--([N,S])\\s(\\d{1,3})°(\\d{1,2})'(\\d{1,2})\"" +
                "\\)$");
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
        //System.out.println(coordinatesStr);
        //(16°33´47" v.d.--16°39´07" v.d./49°14´06" s.š.--49°10´18" s.š.)
        Pattern pattern = Pattern.compile("^\\(" +
                "(\\d{1,3})°(\\d{1,2})[´'](\\d{1,2})\"\\s([v,z])\\.d\\.--(\\d{1,3})°(\\d{1,2})[´'](\\d{1,2})\"\\s([v,z])\\.d\\." +
                "/" +
                "(\\d{1,3})°(\\d{1,2})[´'](\\d{1,2})\"\\s([s,j])\\.š\\.--(\\d{1,3})°(\\d{1,2})[´'](\\d{1,2})\"\\s([s,j])\\.š\\." +
                "\\)$");
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
