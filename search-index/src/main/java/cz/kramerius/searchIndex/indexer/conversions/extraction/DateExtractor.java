package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.shared.DateInfo;
import cz.kramerius.shared.Dom4jUtils;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateExtractor {

    private static final String REGEXP_DAY_MONTH_YEAR = "(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{1,4})"; //7.4.1920, 07. 04. 1920
    private static final String REGEXP_MONTH_YEAR = "(\\d{1,2})\\.\\s*(\\d{1,4})"; //4.1920, 04. 1920
    private static final String REGEXP_YEAR = "\\[?[p,c]?(\\d{4})\\??\\]?"; //'1920', '1920]', '[1920', '[1920]', '[1920?]', '1920?]', 'p1920', 'c1920'
    private static final String REGEXP_YEAR_CCA = "\\[?ca\\s(\\d{4})\\]?"; //'[ca 1690]', 'ca 1690]', '[ca 1690',
    private static final String REGEXP_YEAR_PARTIAL = "[0-9]{1}[0-9ux\\-]{0,3}"; //194u, 18--, 19uu, 180-, 19u7
    private static final String REGEXP_CENTURY = "\\[?(\\d{2})--\\??\\]?"; //'[18--]', '[18--?]', '18--?', '18--?]'
    private static final String REGEXP_DECADE = "\\[?(\\d{3})-\\??\\]?"; //'[183-]', '[183-?]', '183-?', '183-?]', '183-'

    private static final String REGEXP_DAY_MONTH_YEAR_RANGE1 = "(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{1,4})\\s*-\\s*(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{1,4})"; //DD.MM.RRRR-DD.MM.RRRR
    private static final String REGEXP_DAY_MONTH_YEAR_RANGE2 = "(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*-\\s*(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{1,4})"; //DD.MM-DD.MM.RRRR
    private static final String REGEXP_DAY_MONTH_YEAR_RANGE3 = "(\\d{1,2})\\.\\s*-\\s*(\\d{1,2})\\.\\s*(\\d{1,2})\\.\\s*(\\d{1,4})"; //DD.-DD.MM.RRRR

    private static final String REGEXP_MONTH_YEAR_RANGE1 = "(\\d{1,2})\\.\\s*(\\d{1,4})\\s*-\\s*(\\d{1,2})\\.\\s*(\\d{1,4})";  //MM.RRRR-MM.RRRR
    private static final String REGEXP_MONTH_YEAR_RANGE2 = "(\\d{1,2})\\.\\s*-\\s*(\\d{1,2})\\.\\s*(\\d{1,4})";  //MM.-MM.RRRR

    private static final String REGEXP_YEAR_RANGE = "\\[?(\\d{1,4})\\]?\\s*-\\s*(\\d{1,4})\\??\\]?"; //1900-1902, 1900 - 1903, [1900-1902], [1900-1902]?, 1900-1902?, [1881]-1938
    private static final String REGEXP_YEAR_RANGE_VERBAL1 = "\\[?mezi\\s(\\d{4})\\??\\sa\\s(\\d{4})\\??\\]?"; //'[mezi 1695 a 1730]', 'mezi 1620 a 1630', 'mezi 1680 a 1730]', '[mezi 1739? a 1750?]'
    private static final String REGEXP_YEAR_RANGE_VERBAL2 = "\\[?mezi\\s(\\d{4})\\??-(\\d{4})\\??\\]?"; //'[mezi 1897-1908]', '[mezi 1898-1914?]', '[mezi 1898?-1914]', '[mezi 1895-1919', 'mezi 1895-1919]'
    private static final String REGEXP_YEAR_RANGE_PARTIAL = "[0-9]{1}[0-9ux]{0,3}\\s*-\\s*[0-9]{1}[0-9ux]{0,3}"; //192u-19uu, NOT '18uu-195-' (combination of range and '-' for uknown value are not supported due to uncertainty)

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
        if (matchesRegexp(result.value, REGEXP_DAY_MONTH_YEAR)) { //7.4.1920, 07. 04. 1920
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
        } else if (matchesRegexp(result.value, REGEXP_DAY_MONTH_YEAR_RANGE3)) { //DD.-DD.MM.RRRR
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
        } else if (matchesRegexp(result.value, REGEXP_MONTH_YEAR)) { //4.1920, 04. 1920
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
        } else if (matchesRegexp(result.value, REGEXP_MONTH_YEAR_RANGE2)) { //MM.-MM.RRRR
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
        } else if (matchesRegexp(result.value, REGEXP_YEAR_RANGE)) {//1900-1902, 1900 - 1903
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
        } else if (matchesRegexp(result.value, REGEXP_YEAR_PARTIAL)) { //194u, 18--, 19uu, 180-, 19u7
            result.dateMin = MyDateTimeUtils.toYearStartFromPartialYear(result.value);
            result.dateMax = MyDateTimeUtils.toYearEndFromPartialYear(result.value);
        } else if (matchesRegexp(result.value, REGEXP_YEAR_RANGE_PARTIAL)) { //192u-19uu
            String[] tokens = result.value.split("-");
            result.dateMin = MyDateTimeUtils.toYearStartFromPartialYear(tokens[0].trim());
            result.dateMax = MyDateTimeUtils.toYearEndFromPartialYear(tokens[1].trim());
        } else {
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
            if (matchesRegexp(result.valueStart, REGEXP_DAY_MONTH_YEAR)) { //7.4.1920, 07. 04. 1920
                List<Integer> numbers = extractNumbers(result.valueStart, REGEXP_DAY_MONTH_YEAR);
                if (numbers != null) {
                    int day = numbers.get(0);
                    int month = numbers.get(1);
                    int year = numbers.get(2);
                    result.setStart(day, month, year);
                    result.dateMin = MyDateTimeUtils.toDayStart(day, month, year);
                }
            } else if (matchesRegexp(result.valueStart, REGEXP_MONTH_YEAR)) { //4.1920, 04. 1920
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
            } else if (matchesRegexp(result.valueStart, REGEXP_YEAR_PARTIAL)) { //194u, 18--, 19uu, 180-, 19u7
                result.dateMin = MyDateTimeUtils.toYearStartFromPartialYear(result.valueStart);
            } else {
                System.err.println(String.format("Chyba v datech objektu %s: nelze parsovat '%s'", pid, result.valueStart));
            }
        }
        //END
        if (result.valueEnd != null) {
            if (matchesRegexp(result.valueEnd, REGEXP_DAY_MONTH_YEAR)) { //7.4.1920, 07. 04. 1920
                List<Integer> numbers = extractNumbers(result.valueEnd, REGEXP_DAY_MONTH_YEAR);
                if (numbers != null) {
                    int day = numbers.get(0);
                    int month = numbers.get(1);
                    int year = numbers.get(2);
                    result.setEnd(day, month, year);
                    result.dateMax = MyDateTimeUtils.toDayEnd(day, month, year);
                }
            } else if (matchesRegexp(result.valueEnd, REGEXP_MONTH_YEAR)) { //4.1920, 04. 1920
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
            } else if (matchesRegexp(result.valueEnd, REGEXP_YEAR_PARTIAL)) { //194u, 18--, 19uu, 180-, 19u7
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
}
