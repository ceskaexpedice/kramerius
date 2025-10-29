package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.searchIndex.indexer.SolrInput;
import org.dom4j.Node;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor of coordinates from MODS metadata.
 * <p>
 * Input format is either (with example of Africa, which hits both equator and prime meridian):
 * 1. Czech   - degrees, minutes, seconds:        (25°25´00" z.d.--63°30´00" v.d./37°32´00" s.š.--34°51´15" j.š.)
 * 2. English - degrees, minutes, seconds:        (W 25°25'00"--E 63°30'00"/N 37°32'00"--S 34°51'15")
 * 3. English - degrees in floating point:        (W 25.4167° -- E 63.5000° / N 37.5333° -- S 34.8542°)
 * 4. English - minLon, minLat, maxLon, maxLat:   (-25.4167, -34.8542, 63.5000, 37.5333)
 * <p>
 * So in the formats 1-3, the meaning is always (LONGITUDE_RANGE/LATITUDE_RANGE), where:
 * - LONGITUDE_RANGE is (MOST_WESTERN_LONGITUDE -- MOST_EASTERN_LONGITUDE)
 * - LATITUDE_RANGE is (MOST_NORTHERN_LATITUDE -- MOST_SOUTHERN_LATITUDE)
 * In the format 4, the meaning is (MIN_LONGITUDE, MIN_LATITUDE, MAX_LONGITUDE, MAX_LATITUDE) without hemisphere letters but with negative values for west/south.
 */
public class CoordinatesExtractor {

    public BoundingBox extract(Node coordinatesEl, SolrInput solrInput, String pid) {
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
            return bb;
        } else {
            return null;
        }
    }

    private BoundingBox extractBoundingBox(String coordinatesStr, String pid) {
        //(-25.4167, -34.8542, 63.5000, 37.5333)
        BoundingBox bb = parseBbFormatEnDirectBb(coordinatesStr);
        if (bb != null) return bb;

        //(W 25.4167° -- E 63.5000° / N 37.5333° -- S 34.8542°)
        bb = parseBbFormatEnDegFloat(coordinatesStr);
        if (bb != null) return bb;

        //(W 25°25'00"--E 63°30'00"/N 37°32'00"--S 34°51'15")
        bb = parseBbFormatEnDegMinSec(coordinatesStr);
        if (bb != null) return bb;

        //(25°25´00" z.d.--63°30´00" v.d./37°32´00" s.š.--34°51´15" j.š.)
        bb = parseBbFormatCzDegMinSec(coordinatesStr);
        if (bb != null) return bb;

        //zadny z formatu se nechytl
        System.err.println(String.format("Chyba v datech objektu %s: nelze parsovat souřadnice: %s", pid, coordinatesStr));
        return null;
    }

    private BoundingBox parseBbFormatEnDirectBb(String coordinatesStr) {
        //(-25.4167, -34.8542, 63.5000, 37.5333)
        Pattern pattern = Pattern.compile("^\\s*\\(?" +
                "\\s*([+-]?\\d{1,3}(?:\\.\\d+)?)\\s*,\\s*" +   // 1: minLon
                "([+-]?\\d{1,2}(?:\\.\\d+)?)\\s*,\\s*" +       // 2: minLat
                "([+-]?\\d{1,3}(?:\\.\\d+)?)\\s*,\\s*" +       // 3: maxLon
                "([+-]?\\d{1,2}(?:\\.\\d+)?)\\s*" +            // 4: maxLat
                "\\)?\\s*$"
        );
        Matcher m = pattern.matcher(coordinatesStr);
        if (m.find()) {
            double w = Double.parseDouble(m.group(1));
            double e = Double.parseDouble(m.group(2));
            double n = Double.parseDouble(m.group(3));
            double s = Double.parseDouble(m.group(4));
            return new BoundingBox(w, e, n, s);
        } else {
            return null;
        }
    }

    private BoundingBox parseBbFormatEnDegFloat(String coordinatesStr) {
        //(W 25.4167° -- E 63.5000° / N 37.5333° -- S 34.8542°)
        Pattern pattern = Pattern.compile("^\\(?\\s*" +
                "([EeWw])\\s*(\\d{1,3}(?:\\.\\d+)?)°?\\s*-{1,2}\\s*([EeWw])\\s*(\\d{1,3}(?:\\.\\d+)?)°?" +
                "\\s*/\\s*" +
                "([NnSs])\\s*(\\d{1,3}(?:\\.\\d+)?)°?\\s*-{1,2}\\s*([NnSs])\\s*(\\d{1,3}(?:\\.\\d+)?)°?" +
                "\\s*\\)?$");
        Matcher m = pattern.matcher(coordinatesStr);
        if (m.find()) {
            double w = normalizeCoordEn(m.group(1), Double.parseDouble(m.group(2)));
            double e = normalizeCoordEn(m.group(3), Double.parseDouble(m.group(4)));
            double n = normalizeCoordEn(m.group(5), Double.parseDouble(m.group(6)));
            double s = normalizeCoordEn(m.group(7), Double.parseDouble(m.group(8)));
            return new BoundingBox(w, e, n, s);
        } else {
            return null;
        }
    }

    private BoundingBox parseBbFormatEnDegMinSec(String coordinatesStr) {
        //(E 12°02'00"--E 19°11'00"/N 51°03'00"--N 48°31'00")
        coordinatesStr = coordinatesStr.replaceAll("''", "\""); //E 12°02'00''
        Pattern pattern = Pattern.compile("^\\(?\\s*" +
                "([EeWw])\\s*(\\d{1,3})°(\\d{1,2})'(\\d{1,2})\"\\s*-{1,2}\\s*([EeWw])\\s*(\\d{1,3})°(\\d{1,2})'(\\d{1,2})\"" +
                "\\s*/\\s*" +
                "([NnSs])\\s*(\\d{1,3})°(\\d{1,2})'(\\d{1,2})\"\\s*-{1,2}\\s*([NnSs])\\s*(\\d{1,3})°(\\d{1,2})'(\\d{1,2})\"" +
                "\\s*\\)?$");
        Matcher m = pattern.matcher(coordinatesStr);
        if (m.find()) {
            double w = normalizeCoordEn(m.group(1), m.group(2), m.group(3), m.group(4));
            double e = normalizeCoordEn(m.group(5), m.group(6), m.group(7), m.group(8));
            double n = normalizeCoordEn(m.group(9), m.group(10), m.group(11), m.group(12));
            double s = normalizeCoordEn(m.group(13), m.group(14), m.group(15), m.group(16));
            return new BoundingBox(w, e, n, s);
        } else {
            return null;
        }
    }

    private BoundingBox parseBbFormatCzDegMinSec(String coordinatesStr) {
        //(16°33´47" v.d.--16°39´07" v.d./49°14´06" s.š.--49°10´18" s.š.)
        coordinatesStr = coordinatesStr.replaceAll("''", "\""); //012°57´53'' v.d.
        if (coordinatesStr.endsWith("].")) { //(014°02´08" v.d.--014°48´09" v.d./050°15´16" s.š.--049°55´22" s.š.)].
            coordinatesStr = coordinatesStr.substring(0, coordinatesStr.length() - "].".length());
        }
        Pattern pattern = Pattern.compile("^\\(?\\s*" +
                "(\\d{1,3})°(\\d{1,2})[´'](\\d{1,2})\"\\s*([vVzZ])\\.[dD]\\.\\s*-{1,2}\\s*(\\d{1,3})°(\\d{1,2})[´'](\\d{1,2})\"\\s*([vVzZ])\\.[dD]\\." +
                "\\s*/\\s*" +
                "(\\d{1,3})°(\\d{1,2})[´'](\\d{1,2})\"\\s*([sSjJ])\\.[šŠ]\\.\\s*-{1,2}\\s*(\\d{1,3})°(\\d{1,2})[´'](\\d{1,2})\"\\s*([sSjJ])\\.[šŠ]\\." +
                "\\s*\\)?$");
        Matcher m = pattern.matcher(coordinatesStr);
        if (m.matches()) {
            double w = normalizeCoordCz(m.group(4), m.group(1), m.group(2), m.group(3));
            double e = normalizeCoordCz(m.group(8), m.group(5), m.group(6), m.group(7));
            double n = normalizeCoordCz(m.group(12), m.group(9), m.group(10), m.group(11));
            double s = normalizeCoordCz(m.group(16), m.group(13), m.group(14), m.group(15));
            return new BoundingBox(w, e, n, s);
        } else {
            return null;
        }
    }

    private String toStringOrNull(Node node) {
        return ExtractorUtils.toStringOrNull(node);
    }

    private double normalizeCoordCz(String hemi, String deg, String min, String sec) {
        double coord = convertCoordDMStoDF(Integer.parseInt(deg), Integer.parseInt(min), Integer.parseInt(sec));
        char h = hemi.charAt(0);
        if (h == 'z' || h == 'Z' || h == 'j' || h == 'J') { //západ, jih
            coord = -1d * coord; // western or southern hemisphere is stored as negative value
        }
        return coord;
    }

    private double normalizeCoordEn(String hemi, String deg, String min, String sec) {
        double coord = convertCoordDMStoDF(Integer.parseInt(deg), Integer.parseInt(min), Integer.parseInt(sec));
        char h = hemi.charAt(0);
        if (h == 'W' || h == 'w' || h == 'S' || h == 's') { //west, south
            coord = -1d * coord; // western or southern hemisphere is stored as negative value
        }
        return coord;
    }

    private double normalizeCoordEn(String hemi, double coord) {
        char h = hemi.charAt(0);
        if (h == 'W' || h == 'w' || h == 'S' || h == 's') { //west, south
            coord = -1d * coord; // western or southern hemisphere is stored as negative value
        }
        return coord;
    }

    private double convertCoordDMStoDF(int deg, int min, int sec) {
        return deg + min / 60d + sec / 3600d;
    }

    public static class BoundingBox {

        public BoundingBox(double w, double e, double n, double s) {
            this.w = w;
            this.e = e;
            this.n = n;
            this.s = s;
        }

        public final double w;
        public final double e;
        public final double n;
        public final double s;

        public boolean isPoint() {
            return this.w == this.e && this.n == this.s;
        }
    }
}
