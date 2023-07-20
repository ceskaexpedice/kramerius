package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.shared.DateInfo;
import cz.kramerius.shared.Dom4jUtils;
import org.dom4j.Element;

import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateExtractor {

    private static final String REGEXP_DAY_MONTH_YEAR = "(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{1,4})"; //'7.4.1920', '07. 04. 1920'
    private static final String REGEXP_DAY_MONTH_YEAR_BRACKETS = "\\[(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{1,4})\\]"; //'[7.4.1920]', '[07. 04. 1920]'

    private static final String REGEXP_MONTH_YEAR = "(\\d{1,2})\\.\\s*(\\d{1,4})"; //'4.1920', '04. 1920'
    private static final String REGEXP_YEAR = "\\[?[p,c]?(\\d{4})\\??\\]?"; //'1920', '1920]', '[1920', '[1920]', '[1920?]', '1920?]', 'p1920', 'c1920'
    private static final String REGEXP_YEAR_CCA = "\\[?(?:ca|asi)\\s(\\d{4})\\]?"; //'[ca 1690]', 'ca 1690]', '[ca 1690', 'ca 1690', '[asi 1690]', 'asi 1690]', '[asi 1690', 'asi 1690'
    private static final String REGEXP_YEAR_PARTIAL = "[0-9]{1}[0-9ux\\-]{0,3}"; //'194u', '18--', '19uu', '180-', '19u7'
    private static final String REGEXP_CENTURY = "\\[?(\\d{2})--\\??\\]?"; //'[18--]', '[18--?]', '18--?', '18--?]'
    private static final String REGEXP_DECADE = "\\[?(\\d{3})-\\??\\]?"; //'[183-]', '[183-?]', '183-?', '183-?]', '183-'
    private static final String REGEXP_YEAR_AND_COPYRIGHT_YEAR = "\\[?[p,c]?(\\d{4})\\??\\]?(?:,?\\s?c\\d{4})?"; //'1920, c1910', '[1920, c1910]', '[1920?], c1920?]'

    private static final String REGEXP_DAY_MONTH_YEAR_RANGE1 = "(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{1,4})\\s*-\\s*(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{1,4})"; //DD.MM.RRRR-DD.MM.RRRR
    private static final String REGEXP_DAY_MONTH_YEAR_RANGE2 = "(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*-\\s*(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{1,4})"; //DD.MM-DD.MM.RRRR
    private static final String REGEXP_DAY_MONTH_YEAR_RANGE3 = "(\\d{1,2})\\.?\\s*-\\s*(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{1,4})"; //DD.-DD.MM.RRRR

    private static final String REGEXP_MONTH_YEAR_RANGE1 = "(\\d{1,2})\\.\\s*(\\d{1,4})\\s*-\\s*(\\d{1,2})\\.\\s*(\\d{1,4})";  //MM.RRRR-MM.RRRR
    private static final String REGEXP_MONTH_YEAR_RANGE2 = "(\\d{1,2})\\.?\\s*-\\s*(\\d{1,2})\\.\\s*(\\d{1,4})";  //MM.-MM.RRRR

    /
    private static final String REGEXP_YEAR_RANGE = "\\[?(\\d{1,4})\\]?\\s*-\\s*(\\d{1,4})\\??\\]?"; //''1900-1902', '1900 - 1903', '[1900-1902]', '[1900-1902]?', '1900-1902?', '[1881]-1938'
    private static final String REGEXP_YEAR_RANGE_VERBAL1 = "\\[?mezi\\s(\\d{4})\\??\\sa\\s(\\d{4})\\??\\]?"; //'[mezi 1695 a 1730]', 'mezi 1620 a 1630', 'mezi 1680 a 1730]', '[mezi 1739? a 1750?]'
    private static final String REGEXP_YEAR_RANGE_VERBAL2 = "\\[?mezi\\s(\\d{4})\\??-(\\d{4})\\??\\]?"; //'[mezi 1897-1908]', '[mezi 1898-1914?]', '[mezi 1898?-1914]', '[mezi 1895-1919', 'mezi 1895-1919]'
    private static final String REGEXP_YEAR_RANGE_VERBAL3 = "\\[?(\\d{4})\\??\\snebo\\s(\\d{4})\\??\\]?"; //'[1897 nebo 1898]', '[1897 nebo 1898?]', '[1897? nebo 1898]', '[1897 nebo 1898', '1897 nebo 1898]'
    private static final String REGEXP_YEAR_RANGE_PARTIAL = "[0-9]{1}[0-9ux]{0,3}\\s*-\\s*[0-9]{1}[0-9ux]{0,3}"; //'192u-19uu', NOT '18uu-195-' (combination of range and '-' for uknown value are not supported due to uncertainty)

    // indexing both years for searching purposes
    private static final String REGEXP_CORRECT_INCORRECT_YEAR1 = "(\\d{4}),?\\s\\[i\\.e\\.\\sc?(\\d{4})\\]"; //'1997 [i.e. 1998]', '1997, [i.e. 1998]', '1997, [i.e. c1998]'
    private static final String REGEXP_CORRECT_INCORRECT_YEAR2 = "(\\d{4}),?\\s\\[?(?:v\\stir(?:\\.|áži))?(?:\\s?(?:ne)?(?:spr\\.|správně))?\\]?\\s(\\d{4})\\]?"; //'1948, [spr. 1947]', '1952, [v tir. spr. 1953]'
    private static final String REGEXP_CORRECT_INCORRECT_YEAR3 = "(\\d{4}),?\\s\\[?na\\s(?:tit\\.\\s(?:listě|listu|l\\.)|(?:ob\\.|obálce))\\s?(?:\\s?(?:ne)?(?:spr\\.|správně)|chybně)?\\]?\\s?(\\d{4})\\]?"; // '1922 [na ob. 1923]', '1976, [na tit. listu nesprávně] 1975'

    // meaning of second year is unknown, therefore indexing only first year
    private static final String REGEXP_CORRECT_INCORRECT_YEAR4 = "\\[?(\\d{4}),?\\s\\[?[äöüßÄÖÜẞěščřžýáíéóúůďťňĎŇŤŠČŘŽÝÁÍÉÚŮĚÓa-zA-z\\.\\s]*\\]?(?:\\d{4})\\]?"; // '1933, [přetisk 1936]', '2009 [soubor distribuován 2011]'

    private static final String REGEXP_SPRING_OF_YEAR = "(?:Jaro|jaro)\\s(\\d{4})"; // 'Jaro 1920', 'jaro 1920'
    private static final String REGEXP_SUMMER_OF_YEAR = "(?:Léto|léto)\\s(\\d{4})"; // 'Léto 1920', 'léto 1920'
    private static final String REGEXP_AUTUMN_OF_YEAR = "(?:Podzim|podzim)\\s(\\d{4})"; // 'Podzim 1920', 'podzim 1920'
    private static final String REGEXP_WINTER_OF_YEAR = "(?:Zima|zima)\\s(\\d{4})"; // 'Zima 1920', 'zima 1920' (not exactly correct, it's actually 2020/2021, still accepted)

    private static final String REGEXP_MONTH_NAME_YEAR = "[äöüßÄÖÜẞěščřžýáíéóúůďťňĎŇŤŠČŘŽÝÁÍÉÚŮĚÓa-zA-Z]*,?\\s(\\d{4})"; // 'červenec, 1999', 'prosinec 2000', 'Prosinec 2000', 'März 1932', 'March 1932', 'march 1932',
    // parsing of month name depends on avaiable locales
    private static final String DATE_FORMAT_MONTH_NAME_YEAR1 = "LLLL yyyy";
    private static final String DATE_FORMAT_MONTH_NAME_YEAR2 = "LLLL, yyyy";
    private static final String REGEXP_DAY_MONTH_NAME_YEAR = "(\\d{1,2})\\.\\s[äöüßÄÖÜẞěščřžýáíéóúůďťňĎŇŤŠČŘŽÝÁÍÉÚŮĚÓa-zA-Z]*,?\\s(\\d{4})"; // '1. února 1886', '1. März 1886', '1. March 1886', '1. march 1886' (non-czech ones are not exactly correct, still accepted)
    private static final String DATE_FORMAT_DAY_MONTH_NAME_YEAR = "d. MMMM yyyy";

    //too broad definition, enables typos like 'juny 2000' to be parsed as year with note;
    //private static final String REGEXP_YEAR_WITH_NOTE = "[äöüßÄÖÜẞěščřžýáíéóúůďťňĎŇŤŠČŘŽÝÁÍÉÚŮĚÓa-zA-Z\\.\\s\\[\\]]*(\\d{4})\\??\\]?[äöüßÄÖÜẞěščřžýáíéóúůďťňĎŇŤŠČŘŽÝÁÍÉÚŮĚÓa-zA-Z\\.\\s\\[\\],]*";

    public DateInfo extractDateInfoFromMultipleSources(Element modsEl, String pid) {
        DateExtractor dateExtractor = new DateExtractor();
        Element originInfoEl = (Element) Dom4jUtils.buildXpath("mods/originInfo").selectSingleNode(modsEl);
        if (originInfoEl != null) {
            DateInfo fromOriginInfo = dateExtractor.extractFromOriginInfo(originInfoEl, pid);
            if (!fromOriginInfo.isEmpty()) {
                return fromOriginInfo;
            } else {
                String partDate = ExtractorUtils.toStringOrNull(Dom4jUtils.buildXpath("mods/part/date").selectSingleNode(modsEl));
                if (partDate != null) {
                    DateInfo fromPartDate = dateExtractor.extractFromString(partDate, pid);
                    if (!fromPartDate.isEmpty()) {
                        return fromPartDate;
                    }
                }
            }
        } else {
            String partDate = ExtractorUtils.toStringOrNull(Dom4jUtils.buildXpath("mods/part/date").selectSingleNode(modsEl));
            if (partDate != null) {
                DateInfo fromPartDate = dateExtractor.extractFromString(partDate, pid);
                if (!fromPartDate.isEmpty()) {
                    return fromPartDate;
                }
            }
        }
        return null;
    }

    public DateInfo extractFromOriginInfo(Element originInfoEl, String pid) {
        DateInfo result = new DateInfo();
        //<dateIssued point="start">2004</dateIssued>
        //<dateIssued point="end">2005</dateIssued>
        DateInfo fromStartEnd = extractFromStartEnd(originInfoEl, pid);
        result = merge(result, fromStartEnd);
        //<dateIssued>2004</dateIssued>
        String noPointValue = ExtractorUtils.toStringOrNull(Dom4jUtils.buildXpath("dateIssued[not(@point)]").selectSingleNode(originInfoEl));
        if (noPointValue != null) {
            DateInfo fromNoPointValue = extractFromString(noPointValue, pid);
            result = merge(result, fromNoPointValue);
        }
        //System.out.println(result);
        return result;
    }

    public DateInfo extractFromString(String string, String pid) {
        DateInfo result = new DateInfo();
        result.value = string.trim();
        if (matchesRegexp(result.value, REGEXP_DAY_MONTH_YEAR)) { //'7.4.1920', '07. 04. 1920'
            List<Integer> numbers = extractNumbers(result.value, REGEXP_DAY_MONTH_YEAR);
            if (numbers != null) {
                int day = numbers.get(0);
                int month = numbers.get(1);
                int year = numbers.get(2);
                result.setStart(day, month, year);
                result.setEnd(day, month, year);
                result.dateMin = MyDateTimeUtils.toDayStart(day, month, year);
                result.dateMax = MyDateTimeUtils.toDayEnd(day, month, year);
            }
        } else if (matchesRegexp(result.value, REGEXP_DAY_MONTH_YEAR_BRACKETS)) { //'[7.4.1920]', '[07. 04. 1920]'
            List<Integer> numbers = extractNumbers(result.value, REGEXP_DAY_MONTH_YEAR_BRACKETS);
            if (numbers != null) {
                int day = numbers.get(0);
                int month = numbers.get(1);
                int year = numbers.get(2);
                result.setStart(day, month, year);
                result.setEnd(day, month, year);
                result.dateMin = MyDateTimeUtils.toDayStart(day, month, year);
                result.dateMax = MyDateTimeUtils.toDayEnd(day, month, year);
            }
        } else if (matchesRegexp(result.value, REGEXP_DAY_MONTH_YEAR_RANGE1)) { //DD.MM.RRRR-DD.MM.RRRR
            List<Integer> numbers = extractNumbers(result.value, REGEXP_DAY_MONTH_YEAR_RANGE1);
            if (numbers != null) {
                int startDay = numbers.get(0);
                int startMonth = numbers.get(1);
                int startYear = numbers.get(2);
                result.setStart(startDay, startMonth, startYear);
                int endDay = numbers.get(3);
                int endMonth = numbers.get(4);
                int endYear = numbers.get(5);
                result.setEnd(endDay, endMonth, endYear);
                result.dateMin = MyDateTimeUtils.toDayStart(startDay, startMonth, startYear);
                result.dateMax = MyDateTimeUtils.toDayEnd(endDay, endMonth, endYear);
            }
        } else if (matchesRegexp(result.value, REGEXP_DAY_MONTH_YEAR_RANGE2)) { //DD.MM-DD.MM.RRRR
            List<Integer> numbers = extractNumbers(result.value, REGEXP_DAY_MONTH_YEAR_RANGE2);
            if (numbers != null) {
                int startDay = numbers.get(0);
                int startMonth = numbers.get(1);
                int startYear = numbers.get(4);
                result.setStart(startDay, startMonth, startYear);
                int endDay = numbers.get(2);
                int endMonth = numbers.get(3);
                int endYear = numbers.get(4);
                result.setEnd(endDay, endMonth, endYear);
                result.dateMin = MyDateTimeUtils.toDayStart(startDay, startMonth, startYear);
                result.dateMax = MyDateTimeUtils.toDayEnd(endDay, endMonth, endYear);
            }
        } else if (matchesRegexp(result.value, REGEXP_DAY_MONTH_YEAR_RANGE3)) { //DD.-DD.MM.RRRR, '03 - 04.12.2012'
            List<Integer> numbers = extractNumbers(result.value, REGEXP_DAY_MONTH_YEAR_RANGE3);
            if (numbers != null) {
                int startDay = numbers.get(0);
                int startMonth = numbers.get(2);
                int startYear = numbers.get(3);
                result.setStart(startDay, startMonth, startYear);
                int endDay = numbers.get(1);
                int endMonth = numbers.get(2);
                int endYear = numbers.get(3);
                result.setEnd(endDay, endMonth, endYear);
                result.dateMin = MyDateTimeUtils.toDayStart(startDay, startMonth, startYear);
                result.dateMax = MyDateTimeUtils.toDayEnd(endDay, endMonth, endYear);
            }
        } else if (matchesRegexp(result.value, REGEXP_MONTH_YEAR)) { //'4.1920', '04. 1920'
            List<Integer> numbers = extractNumbers(result.value, REGEXP_MONTH_YEAR);
            if (numbers != null) {
                int month = numbers.get(0);
                int year = numbers.get(1);
                result.setStart(month, year);
                result.setEnd(month, year);
                result.dateMin = MyDateTimeUtils.toMonthStart(month, year);
                result.dateMax = MyDateTimeUtils.toMonthEnd(month, year);
            }
        } else if (matchesRegexp(result.value, REGEXP_MONTH_YEAR_RANGE1)) { //MM.RRRR-MM.RRRR
            List<Integer> numbers = extractNumbers(result.value, REGEXP_MONTH_YEAR_RANGE1);
            if (numbers != null) {
                int startMonth = numbers.get(0);
                int startYear = numbers.get(1);
                result.setStart(startMonth, startYear);
                int endMonth = numbers.get(2);
                int endYear = numbers.get(3);
                result.setEnd(endMonth, endYear);
                result.dateMin = MyDateTimeUtils.toMonthStart(startMonth, startYear);
                result.dateMax = MyDateTimeUtils.toMonthEnd(endMonth, endYear);
            }
        } else if (matchesRegexp(result.value, REGEXP_MONTH_YEAR_RANGE2)) { //MM.-MM.RRRR, 'MM-MM.RRRR'
            List<Integer> numbers = extractNumbers(result.value, REGEXP_MONTH_YEAR_RANGE2);
            if (numbers != null) {
                int startMonth = numbers.get(0);
                int startYear = numbers.get(2);
                result.setStart(startMonth, startYear);
                int endMonth = numbers.get(1);
                int endYear = numbers.get(2);
                result.setEnd(endMonth, endYear);
                result.dateMin = MyDateTimeUtils.toMonthStart(startMonth, startYear);
                result.dateMax = MyDateTimeUtils.toMonthEnd(endMonth, endYear);
            }
        } else if (matchesRegexp(result.value, REGEXP_YEAR)) { //'1920', '1920?', '1920]', '[1920', '[1920]', '[1920?]', '1920?]', 'p1920', 'c1920'
            List<Integer> numbers = extractNumbers(result.value, REGEXP_YEAR);
            if (numbers != null) {
                int year = numbers.get(0);
                result.rangeStartYear = year;
                result.rangeEndYear = year;
                result.dateMin = MyDateTimeUtils.toYearStart(year);
                result.dateMax = MyDateTimeUtils.toYearEnd(year);
            }
        } else if (matchesRegexp(result.value, REGEXP_YEAR_CCA)) { //'1920', '1920?', '1920]', '[1920', '[1920]', '[1920?]', '1920?]', 'p1920', 'c1920'
            List<Integer> numbers = extractNumbers(result.value, REGEXP_YEAR_CCA);
            if (numbers != null) {
                int year = numbers.get(0);
                result.rangeStartYear = year;
                result.rangeEndYear = year;
                result.dateMin = MyDateTimeUtils.toYearStart(year);
                result.dateMax = MyDateTimeUtils.toYearEnd(year);
            }
        } else if (matchesRegexp(result.value, REGEXP_YEAR_RANGE)) {//'1900-1902', '1900 - 1903', '[1900-1902]', '[1900-1902]?', '1900-1902?', '[1881]-1938
            List<Integer> numbers = extractNumbers(result.value, REGEXP_YEAR_RANGE);
            if (numbers != null) {
                result.rangeStartYear = numbers.get(0);
                result.rangeEndYear = numbers.get(1);
                result.valueStart = result.rangeStartYear.toString();
                result.valueEnd = result.rangeEndYear.toString();
                result.dateMin = MyDateTimeUtils.toYearStart(result.rangeStartYear);
                result.dateMax = MyDateTimeUtils.toYearEnd(result.rangeEndYear);
            }
        } else if (matchesRegexp(result.value, REGEXP_YEAR_RANGE_VERBAL1)) { //'[mezi 1695 a 1730]', 'mezi 1620 a 1630', 'mezi 1680 a 1730]'
            List<Integer> numbers = extractNumbers(result.value, REGEXP_YEAR_RANGE_VERBAL1);
            if (numbers != null) {
                result.rangeStartYear = numbers.get(0);
                result.rangeEndYear = numbers.get(1);
                result.valueStart = result.rangeStartYear.toString();
                result.valueEnd = result.rangeEndYear.toString();
                result.dateMin = MyDateTimeUtils.toYearStart(result.rangeStartYear);
                result.dateMax = MyDateTimeUtils.toYearEnd(result.rangeEndYear);
            }
        } else if (matchesRegexp(result.value, REGEXP_YEAR_RANGE_VERBAL2)) { //'[mezi 1897-1908]', '[mezi 1898-1914?]', '[mezi 1898?-1914]', '[mezi 1895-1919', 'mezi 1895-1919]'
            List<Integer> numbers = extractNumbers(result.value, REGEXP_YEAR_RANGE_VERBAL2);
            if (numbers != null) {
                result.rangeStartYear = numbers.get(0);
                result.rangeEndYear = numbers.get(1);
                result.valueStart = result.rangeStartYear.toString();
                result.valueEnd = result.rangeEndYear.toString();
                result.dateMin = MyDateTimeUtils.toYearStart(result.rangeStartYear);
                result.dateMax = MyDateTimeUtils.toYearEnd(result.rangeEndYear);
            }
        } else if (matchesRegexp(result.value, REGEXP_YEAR_RANGE_VERBAL3)) { //'[1897 nebo 1898]', '[1897 nebo 1898?]', '[1897? nebo 1898]', '[1897 nebo 1898', '1897 nebo 1898]'
            List<Integer> numbers = extractNumbers(result.value, REGEXP_YEAR_RANGE_VERBAL3);
            if (numbers != null) {
                result.rangeStartYear = numbers.get(0);
                result.rangeEndYear = numbers.get(1);
                result.valueStart = result.rangeStartYear.toString();
                result.valueEnd = result.rangeEndYear.toString();
                result.dateMin = MyDateTimeUtils.toYearStart(result.rangeStartYear);
                result.dateMax = MyDateTimeUtils.toYearEnd(result.rangeEndYear);
            }
        } else if (matchesRegexp(result.value, REGEXP_DECADE)) { //'[183-]', '[183-?]', '183-?', '183-?]', '183-'
            List<Integer> numbers = extractNumbers(result.value, REGEXP_DECADE);
            if (numbers != null) {
                int century = numbers.get(0);
                result.dateMin = MyDateTimeUtils.toYearStart(century * 10);
                result.dateMax = MyDateTimeUtils.toYearEnd(century * 10 + 9);
            }
        } else if (matchesRegexp(result.value, REGEXP_CENTURY)) { //'[18--]', '[18--?]', '18--?', '18--?]'
            List<Integer> numbers = extractNumbers(result.value, REGEXP_CENTURY);
            if (numbers != null) {
                int century = numbers.get(0);
                result.dateMin = MyDateTimeUtils.toYearStart(century * 100);
                result.dateMax = MyDateTimeUtils.toYearEnd(century * 100 + 99);
            }
        } else if (matchesRegexp(result.value, REGEXP_YEAR_AND_COPYRIGHT_YEAR)) { //'1920', '1920?', '1920]', '[1920', '[1920]', '[1920?]', '1920?]', 'p1920', 'c1920'
            List<Integer> numbers = extractNumbers(result.value, REGEXP_YEAR_AND_COPYRIGHT_YEAR);
            if (numbers != null) {
                int year = numbers.get(0);
                result.rangeStartYear = year;
                result.rangeEndYear = year;
                result.dateMin = MyDateTimeUtils.toYearStart(year);
                result.dateMax = MyDateTimeUtils.toYearEnd(year);
            }
        } else if (matchesRegexp(result.value, REGEXP_YEAR_PARTIAL)) { //'194u', '18--', '19uu', '180-', '19u7'
            result.dateMin = MyDateTimeUtils.toYearStartFromPartialYear(result.value);
            result.dateMax = MyDateTimeUtils.toYearEndFromPartialYear(result.value);
        } else if (matchesRegexp(result.value, REGEXP_YEAR_RANGE_PARTIAL)) { //'192u-19uu', NOT '18uu-195-' (combination of range and '-' for uknown value are not supported due to uncertainty)
            String[] tokens = result.value.split("-");
            result.dateMin = MyDateTimeUtils.toYearStartFromPartialYear(tokens[0].trim());
            result.dateMax = MyDateTimeUtils.toYearEndFromPartialYear(tokens[1].trim());
        } else if (matchesRegexp(result.value, REGEXP_CORRECT_INCORRECT_YEAR1)) {
            List<Integer> numbers = extractNumbers(result.value, REGEXP_CORRECT_INCORRECT_YEAR1);
            if (numbers != null) {
                result.rangeStartYear = numbers.get(0) < numbers.get(1) ? numbers.get(0) : numbers.get(1);
                result.rangeEndYear = numbers.get(1) > numbers.get(0) ? numbers.get(1) : numbers.get(0);
                result.valueStart = result.rangeStartYear.toString();
                result.valueEnd = result.rangeEndYear.toString();
                result.dateMin = MyDateTimeUtils.toYearStart(result.rangeStartYear);
                result.dateMax = MyDateTimeUtils.toYearEnd(result.rangeEndYear);
            }
        } else if (matchesRegexp(result.value, REGEXP_CORRECT_INCORRECT_YEAR2)) {
            List<Integer> numbers = extractNumbers(result.value, REGEXP_CORRECT_INCORRECT_YEAR2);
            if (numbers != null) {
                result.rangeStartYear = numbers.get(0) < numbers.get(1) ? numbers.get(0) : numbers.get(1);
                result.rangeEndYear = numbers.get(1) > numbers.get(0) ? numbers.get(1) : numbers.get(0);
                result.valueStart = result.rangeStartYear.toString();
                result.valueEnd = result.rangeEndYear.toString();
                result.dateMin = MyDateTimeUtils.toYearStart(result.rangeStartYear);
                result.dateMax = MyDateTimeUtils.toYearEnd(result.rangeEndYear);
            }
        } else if (matchesRegexp(result.value, REGEXP_CORRECT_INCORRECT_YEAR3)) {
            List<Integer> numbers = extractNumbers(result.value, REGEXP_CORRECT_INCORRECT_YEAR3);
            if (numbers != null) {
                result.rangeStartYear = numbers.get(0) < numbers.get(1) ? numbers.get(0) : numbers.get(1);
                result.rangeEndYear = numbers.get(1) > numbers.get(0) ? numbers.get(1) : numbers.get(0);
                result.valueStart = result.rangeStartYear.toString();
                result.valueEnd = result.rangeEndYear.toString();
                result.dateMin = MyDateTimeUtils.toYearStart(result.rangeStartYear);
                result.dateMax = MyDateTimeUtils.toYearEnd(result.rangeEndYear);
            }
        } else if (matchesRegexp(result.value, REGEXP_CORRECT_INCORRECT_YEAR4)) {
            List<Integer> numbers = extractNumbers(result.value, REGEXP_CORRECT_INCORRECT_YEAR4);
            if (numbers != null) {
                result.rangeStartYear = numbers.get(0);
                result.rangeEndYear = numbers.get(0);
                result.valueStart = result.rangeStartYear.toString();
                result.valueEnd = result.rangeEndYear.toString();
                result.dateMin = MyDateTimeUtils.toYearStart(result.rangeStartYear);
                result.dateMax = MyDateTimeUtils.toYearEnd(result.rangeEndYear);
            }
        } else if (matchesRegexp(result.value, REGEXP_SPRING_OF_YEAR)) {
            List<Integer> numbers = extractNumbers(result.value, REGEXP_SPRING_OF_YEAR);
            if (numbers != null) {
                int year = numbers.get(0);
                result.setStart(1, 3, year);
                result.setEnd(31, 5, year);
                result.dateMin = MyDateTimeUtils.toDayStart(1, 3, year);
                result.dateMax = MyDateTimeUtils.toDayEnd(31, 5, year);
            }
        } else if (matchesRegexp(result.value, REGEXP_SUMMER_OF_YEAR)) {
            List<Integer> numbers = extractNumbers(result.value, REGEXP_SUMMER_OF_YEAR);
            if (numbers != null) {
                int year = numbers.get(0);
                result.setStart(1, 6, year);
                result.setEnd(31, 8, year);
                result.dateMin = MyDateTimeUtils.toDayStart(1, 6, year);
                result.dateMax = MyDateTimeUtils.toDayEnd(31, 8, year);
            }
        } else if (matchesRegexp(result.value, REGEXP_AUTUMN_OF_YEAR)) {
            List<Integer> numbers = extractNumbers(result.value, REGEXP_AUTUMN_OF_YEAR);
            if (numbers != null) {
                int year = numbers.get(0);
                result.setStart(1, 9, year);
                result.setEnd(30, 11, year);
                result.dateMin = MyDateTimeUtils.toDayStart(1, 9, year);
                result.dateMax = MyDateTimeUtils.toDayEnd(30, 11, year);
            }
        } else if (matchesRegexp(result.value, REGEXP_WINTER_OF_YEAR)) {
            List<Integer> numbers = extractNumbers(result.value, REGEXP_WINTER_OF_YEAR);
            if (numbers != null) {
                int year = numbers.get(0);
                boolean endYearLeap = MyDateTimeUtils.isLeapYear(year + 1);
                result.setStart(1, 12, year);
                result.setEnd(endYearLeap ? 29 : 28, 2, year + 1);
                result.dateMin = MyDateTimeUtils.toDayStart(1, 12, year);
                result.dateMax = MyDateTimeUtils.toDayEnd(endYearLeap ? 29 : 28, 2, year + 1);
            }
        } else if (matchesRegexp(result.value, REGEXP_MONTH_NAME_YEAR) //'prosinec 2000', 'März 1932'
                && matchesDateFormatContainingMonthByName(result.value, DATE_FORMAT_MONTH_NAME_YEAR1)) {
            List<Integer> numbers = extractNumbers(result.value, REGEXP_MONTH_NAME_YEAR);
            if (numbers != null) {
                int month = extractMonthNumber(result.value, DATE_FORMAT_MONTH_NAME_YEAR1);
                int year = numbers.get(0);
                result.setStart(month, year);
                result.setEnd(month, year);
                result.dateMin = MyDateTimeUtils.toMonthStart(month, year);
                result.dateMax = MyDateTimeUtils.toMonthEnd(month, year);
            }
        } else if (matchesRegexp(result.value, REGEXP_MONTH_NAME_YEAR) //'prosinec, 2000', 'März, 1932'
                && matchesDateFormatContainingMonthByName(result.value, DATE_FORMAT_MONTH_NAME_YEAR2)) {
            List<Integer> numbers = extractNumbers(result.value, REGEXP_MONTH_NAME_YEAR);
            if (numbers != null) {
                int month = extractMonthNumber(result.value, DATE_FORMAT_MONTH_NAME_YEAR2);
                int year = numbers.get(0);
                result.setStart(month, year);
                result.setEnd(month, year);
                result.dateMin = MyDateTimeUtils.toMonthStart(month, year);
                result.dateMax = MyDateTimeUtils.toMonthEnd(month, year);
            }
        } else if (matchesRegexp(result.value, REGEXP_DAY_MONTH_NAME_YEAR) //'1. února 1886'
                && matchesDateFormatContainingMonthByName(result.value, DATE_FORMAT_DAY_MONTH_NAME_YEAR)) {
            List<Integer> numbers = extractNumbers(result.value, REGEXP_DAY_MONTH_NAME_YEAR);
            if (numbers != null) {
                int day = numbers.get(0);
                int month = extractMonthNumber(result.value, DATE_FORMAT_DAY_MONTH_NAME_YEAR);
                int year = numbers.get(1);
                result.setStart(day, month, year);
                result.setEnd(day, month, year);
                result.dateMin = MyDateTimeUtils.toDayStart(day, month, year);
                result.dateMax = MyDateTimeUtils.toDayEnd(day, month, year);
            }
        }/* else if (matchesRegexp(result.value, REGEXP_YEAR_WITH_NOTE)) {
            List<Integer> numbers = extractNumbers(result.value, REGEXP_YEAR_WITH_NOTE);
            if (numbers != null) {
                result.rangeStartYear = numbers.get(0);
                result.rangeEndYear = numbers.get(0);
                result.valueStart = result.rangeStartYear.toString();
                result.valueEnd = result.rangeEndYear.toString();
                result.dateMin = MyDateTimeUtils.toYearStart(result.rangeStartYear);
                result.dateMax = MyDateTimeUtils.toYearEnd(result.rangeEndYear);
            }
        }*/ else {
            System.err.println(String.format("Chyba v datech objektu %s: nelze parsovat '%s'", pid, result.value));
        }
        result.updateInstantData();
        return result;
    }

    private DateInfo merge(DateInfo first, DateInfo second) {
        DateInfo result = new DateInfo();
        result.value = first.value != null ? first.value : second.value;
        result.valueStart = first.valueStart != null ? first.valueStart : second.valueStart;
        result.valueEnd = first.valueEnd != null ? first.valueEnd : second.valueEnd;

        result.instantYear = first.instantYear != null ? first.instantYear : second.instantYear;
        result.instantMonth = first.instantMonth != null ? first.instantMonth : second.instantMonth;
        result.instantDay = first.instantDay != null ? first.instantDay : second.instantDay;

        result.rangeStartYear = first.rangeStartYear != null ? first.rangeStartYear : second.rangeStartYear;
        result.rangeStartMonth = first.rangeStartMonth != null ? first.rangeStartMonth : second.rangeStartMonth;
        result.rangeStartDay = first.rangeStartDay != null ? first.rangeStartDay : second.rangeStartDay;

        result.rangeEndYear = first.rangeEndYear != null ? first.rangeEndYear : second.rangeEndYear;
        result.rangeEndMonth = first.rangeEndMonth != null ? first.rangeEndMonth : second.rangeEndMonth;
        result.rangeEndDay = first.rangeEndDay != null ? first.rangeEndDay : second.rangeEndDay;

        result.dateMin = first.dateMin != null ? first.dateMin : second.dateMin;
        result.dateMax = first.dateMax != null ? first.dateMax : second.dateMax;
        return result;
    }

    private DateInfo extractFromStartEnd(Element originInfoEl, String pid) {
        DateInfo result = new DateInfo();
        result.valueStart = ExtractorUtils.toStringOrNull(Dom4jUtils.buildXpath("dateIssued[@point='start']").selectSingleNode(originInfoEl));
        result.valueEnd = ExtractorUtils.toStringOrNull(Dom4jUtils.buildXpath("dateIssued[@point='end']").selectSingleNode(originInfoEl));
        //START
        if (result.valueStart != null) {
            if (matchesRegexp(result.valueStart, REGEXP_DAY_MONTH_YEAR)) { //'7.4.1920', '07. 04. 1920'
                List<Integer> numbers = extractNumbers(result.valueStart, REGEXP_DAY_MONTH_YEAR);
                if (numbers != null) {
                    int day = numbers.get(0);
                    int month = numbers.get(1);
                    int year = numbers.get(2);
                    result.setStart(day, month, year);
                    result.dateMin = MyDateTimeUtils.toDayStart(day, month, year);
                }
            } else if (matchesRegexp(result.valueStart, REGEXP_DAY_MONTH_YEAR_BRACKETS)) { //'[7.4.1920]', '[07. 04. 1920]'
                List<Integer> numbers = extractNumbers(result.valueStart, REGEXP_DAY_MONTH_YEAR_BRACKETS);
                if (numbers != null) {
                    int day = numbers.get(0);
                    int month = numbers.get(1);
                    int year = numbers.get(2);
                    result.setStart(day, month, year);
                    result.dateMin = MyDateTimeUtils.toDayStart(day, month, year);
                }
            } else if (matchesRegexp(result.valueStart, REGEXP_MONTH_YEAR)) { //'4.1920', '04. 1920'
                List<Integer> numbers = extractNumbers(result.valueStart, REGEXP_MONTH_YEAR);
                if (numbers != null) {
                    int month = numbers.get(0);
                    int year = numbers.get(1);
                    result.setStart(month, year);
                    result.dateMin = MyDateTimeUtils.toMonthStart(month, year);
                }
            } else if (matchesRegexp(result.valueStart, REGEXP_YEAR)) { //'1920', '1920?', '1920]', '[1920', '[1920]', '[1920?]', '1920?]', 'p1920', 'c1920'
                List<Integer> numbers = extractNumbers(result.valueStart, REGEXP_YEAR);
                if (numbers != null) {
                    result.rangeStartYear = numbers.get(0);
                    result.dateMin = MyDateTimeUtils.toYearStart(numbers.get(0));
                }
            } else if (matchesRegexp(result.valueStart, REGEXP_YEAR_PARTIAL)) { //'194u', '18--', '19uu', '180-', '19u7'
                result.dateMin = MyDateTimeUtils.toYearStartFromPartialYear(result.valueStart);
            } else {
                System.err.println(String.format("Chyba v datech objektu %s: nelze parsovat '%s'", pid, result.valueStart));
            }
        }
        //END
        if (result.valueEnd != null) {
            if (matchesRegexp(result.valueEnd, REGEXP_DAY_MONTH_YEAR)) { //'7.4.1920', '07. 04. 1920'
                List<Integer> numbers = extractNumbers(result.valueEnd, REGEXP_DAY_MONTH_YEAR);
                if (numbers != null) {
                    int day = numbers.get(0);
                    int month = numbers.get(1);
                    int year = numbers.get(2);
                    result.setEnd(day, month, year);
                    result.dateMax = MyDateTimeUtils.toDayEnd(day, month, year);
                }
            } else if (matchesRegexp(result.valueEnd, REGEXP_DAY_MONTH_YEAR_BRACKETS)) { //'[7.4.1920]', '[07. 04. 1920]'
                List<Integer> numbers = extractNumbers(result.valueEnd, REGEXP_DAY_MONTH_YEAR_BRACKETS);
                if (numbers != null) {
                    int day = numbers.get(0);
                    int month = numbers.get(1);
                    int year = numbers.get(2);
                    result.setEnd(day, month, year);
                    result.dateMax = MyDateTimeUtils.toDayEnd(day, month, year);
                }
            } else if (matchesRegexp(result.valueEnd, REGEXP_MONTH_YEAR)) { //'4.1920', '04. 1920'
                List<Integer> numbers = extractNumbers(result.valueEnd, REGEXP_MONTH_YEAR);
                if (numbers != null) {
                    int month = numbers.get(0);
                    int year = numbers.get(1);
                    result.setEnd(month, year);
                    result.dateMax = MyDateTimeUtils.toMonthEnd(month, year);
                }
            } else if (matchesRegexp(result.valueEnd, REGEXP_YEAR)) { //'1920', '1920?', '1920]', '[1920', '[1920]', '[1920?]', '1920?]', 'p1920', 'c1920'
                List<Integer> numbers = extractNumbers(result.valueEnd, REGEXP_YEAR);
                if (numbers != null) {
                    result.rangeEndYear = numbers.get(0);
                    result.dateMax = MyDateTimeUtils.toYearEnd(numbers.get(0));
                }
            } else if (matchesRegexp(result.valueEnd, REGEXP_YEAR_PARTIAL)) { //'194u', '18--', '19uu', '180-', '19u7'
                result.dateMax = MyDateTimeUtils.toYearEndFromPartialYear(result.valueEnd);
            } else {
                System.err.println(String.format("Chyba v datech objektu %s: nelze parsovat '%s'", pid, result.valueEnd));
            }
        }
        result.updateInstantData();
        return result;
    }

    private List<Integer> extractNumbers(String str, String regexp) {
        Pattern pattern = Pattern.compile(regexp);
        Matcher m = pattern.matcher(str);
        if (m.find()) {
            List<Integer> result = new ArrayList<>();
            for (int i = 1; i <= m.groupCount(); i++) {
                result.add(Integer.valueOf(m.group(i).trim()));
            }
            return result;
        } else {
            return null;
        }
    }

    private boolean matchesRegexp(String str, String regexp) {
        return str != null && str.matches(regexp);
    }

    private boolean matchesDateFormatContainingMonthByName(String str, String dateFormat) {
        return extractMonthNumber(str, dateFormat) > 0;
    }

    private int extractMonthNumber(String str, String dateFormat) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(dateFormat);
        Month month = null;
        for (Locale loc : Locale.getAvailableLocales()) {
            try {
                month = Month.from(fmt.withLocale(loc).parse(str));
                return month.getValue();
            } catch (DateTimeParseException e) {
                // can't parse, go to next locale
            }
        }
        return 0;
    }
}
