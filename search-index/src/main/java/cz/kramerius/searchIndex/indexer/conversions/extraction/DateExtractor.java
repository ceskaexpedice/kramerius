package cz.kramerius.searchIndex.indexer.conversions.extraction;

import cz.kramerius.shared.DateInfo;
import cz.kramerius.shared.Dom4jUtils;
import cz.kramerius.shared.Pair;
import org.dom4j.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateExtractor {

    private static final String REGEXP_YEAR = "\\[?[p,c]?(\\d{4})\\??\\]?"; //'1920', '1920]', '[1920', '[1920]', '[1920?]', '1920?]', 'p1920', 'c1920'
    private static final String REGEXP_YEAR_CCA = "\\[?ca\\s(\\d{4})\\]?"; //'[ca 1690]', 'ca 1690]', '[ca 1690',
    private static final String REGEXP_YEAR_RANGE = "(\\d{1,4})\\s*-\\s*(\\d{1,4})"; //1900-1902, 1900 - 1903
    private static final String REGEXP_YEAR_RANGE_VERBAL = "\\[?mezi\\s(\\d{4})\\??\\sa\\s(\\d{4})\\??\\]?"; //'[mezi 1695 a 1730]', 'mezi 1620 a 1630', 'mezi 1680 a 1730]', '[mezi 1739? a 1750?]'

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
        if (isDayMonthYear(result.value)) { //7.4.1920, 07. 04. 1920
            String[] tokens = result.value.split("\\.");
            int day = Integer.valueOf(tokens[0].trim());
            int month = Integer.valueOf(tokens[1].trim());
            int year = Integer.valueOf(tokens[2].trim());
            result.setStart(day, month, year);
            result.setEnd(day, month, year);
            result.dateMin = MyDateTimeUtils.toDayStart(day, month, year);
            result.dateMax = MyDateTimeUtils.toDayEnd(day, month, year);
        } else if (isDayMonthYearRange1(result.value)) { //DD.MM.RRRR-DD.MM.RRRR
            String[] tokens = result.value.split("-");
            String[] startTokens = tokens[0].trim().split("\\.");
            String[] endTokens = tokens[1].trim().split("\\.");
            int startDay = Integer.valueOf(startTokens[0].trim());
            int startMonth = Integer.valueOf(startTokens[1].trim());
            int startYear = Integer.valueOf(startTokens[2].trim());
            result.setStart(startDay, startMonth, startYear);
            int endDay = Integer.valueOf(endTokens[0].trim());
            int endMonth = Integer.valueOf(endTokens[1].trim());
            int endYear = Integer.valueOf(endTokens[2].trim());
            result.setEnd(endDay, endMonth, endYear);
            result.dateMin = MyDateTimeUtils.toDayStart(startDay, startMonth, startYear);
            result.dateMax = MyDateTimeUtils.toDayEnd(endDay, endMonth, endYear);
        } else if (isDayMonthYearRange2(result.value)) { //DD.MM-DD.MM.RRRR
            String[] tokens = result.value.split("-");
            String[] startTokens = tokens[0].trim().split("\\.");
            String[] endTokens = tokens[1].trim().split("\\.");
            int endDay = Integer.valueOf(endTokens[0].trim());
            int endMonth = Integer.valueOf(endTokens[1].trim());
            int endYear = Integer.valueOf(endTokens[2].trim());
            result.setEnd(endDay, endMonth, endYear);
            int startDay = Integer.valueOf(startTokens[0].trim());
            int startMonth = Integer.valueOf(startTokens[1].trim());
            int startYear = endYear;
            result.setStart(startDay, startMonth, startYear);
            result.dateMin = MyDateTimeUtils.toDayStart(startDay, startMonth, startYear);
            result.dateMax = MyDateTimeUtils.toDayEnd(endDay, endMonth, endYear);
        } else if (isDayMonthYearRange3(result.value)) { //DD.-DD.MM.RRRR
            String[] tokens = result.value.split("-");
            String[] startTokens = tokens[0].trim().split("\\.");
            String[] endTokens = tokens[1].trim().split("\\.");
            int endDay = Integer.valueOf(endTokens[0].trim());
            int endMonth = Integer.valueOf(endTokens[1].trim());
            int endYear = Integer.valueOf(endTokens[2].trim());
            result.setEnd(endDay, endMonth, endYear);
            int startDay = Integer.valueOf(startTokens[0].trim());
            int startMonth = endMonth;
            int startYear = endYear;
            result.setStart(startDay, startMonth, startYear);
            result.dateMin = MyDateTimeUtils.toDayStart(startDay, startMonth, startYear);
            result.dateMax = MyDateTimeUtils.toDayEnd(endDay, endMonth, endYear);
        } else if (isMonthYear(result.value)) { //4.1920, 04. 1920
            String[] tokens = result.value.split("\\.");
            int month = Integer.valueOf(tokens[0].trim());
            int year = Integer.valueOf(tokens[1].trim());
            result.setStart(month, year);
            result.setEnd(month, year);
            result.dateMin = MyDateTimeUtils.toMonthStart(month, year);
            result.dateMax = MyDateTimeUtils.toMonthEnd(month, year);
        } else if (isMonthYearRange1(result.value)) { //MM.RRRR-MM.RRRR
            String[] tokens = result.value.split("-");
            String[] startTokens = tokens[0].trim().split("\\.");
            String[] endTokens = tokens[1].trim().split("\\.");
            int endMonth = Integer.valueOf(endTokens[0].trim());
            int endYear = Integer.valueOf(endTokens[1].trim());
            result.setEnd(endMonth, endYear);
            int startMonth = Integer.valueOf(startTokens[0].trim());
            int startYear = Integer.valueOf(startTokens[1].trim());
            result.setStart(startMonth, startYear);
            result.dateMin = MyDateTimeUtils.toMonthStart(startMonth, startYear);
            result.dateMax = MyDateTimeUtils.toMonthEnd(endMonth, endYear);
        } else if (isMonthYearRange2(result.value)) { //MM.-MM.RRRR
            String[] tokens = result.value.split("-");
            String[] startTokens = tokens[0].trim().split("\\.");
            String[] endTokens = tokens[1].trim().split("\\.");
            int endMonth = Integer.valueOf(endTokens[0].trim());
            int endYear = Integer.valueOf(endTokens[1].trim());
            result.setEnd(endMonth, endYear);
            int startMonth = Integer.valueOf(startTokens[0].trim());
            int startYear = endYear;
            result.setStart(startMonth, startYear);
            result.dateMin = MyDateTimeUtils.toMonthStart(startMonth, startYear);
            result.dateMax = MyDateTimeUtils.toMonthEnd(endMonth, endYear);
        } else if (matchesRegexp(result.value, REGEXP_YEAR)) { //'1920', '1920?', '1920]', '[1920', '[1920]', '[1920?]', '1920?]', 'p1920', 'c1920'
            Integer year = extractSingleYear(result.value, REGEXP_YEAR);
            if (year != null) {
                result.rangeStartYear = year;
                result.rangeEndYear = year;
                result.dateMin = MyDateTimeUtils.toYearStart(year);
                result.dateMax = MyDateTimeUtils.toYearEnd(year);
            }
        } else if (matchesRegexp(result.value, REGEXP_YEAR_CCA)) { //'1920', '1920?', '1920]', '[1920', '[1920]', '[1920?]', '1920?]', 'p1920', 'c1920'
            Integer year = extractSingleYear(result.value, REGEXP_YEAR_CCA);
            if (year != null) {
                result.rangeStartYear = year;
                result.rangeEndYear = year;
                result.dateMin = MyDateTimeUtils.toYearStart(year);
                result.dateMax = MyDateTimeUtils.toYearEnd(year);
            }
        } else if (matchesRegexp(result.value, REGEXP_YEAR_RANGE)) {//1900-1902, 1900 - 1903
            Pair<Integer, Integer> years = extractTwoYears(result.value, REGEXP_YEAR_RANGE);
            result.rangeStartYear = years.getFirst();
            result.rangeEndYear = years.getSecond();
            result.valueStart = result.rangeStartYear.toString();
            result.valueEnd = result.rangeEndYear.toString();
            result.dateMin = MyDateTimeUtils.toYearStart(result.rangeStartYear);
            result.dateMax = MyDateTimeUtils.toYearEnd(result.rangeEndYear);
        } else if (matchesRegexp(result.value, REGEXP_YEAR_RANGE_VERBAL)) { //'[mezi 1695 a 1730]', 'mezi 1620 a 1630', 'mezi 1680 a 1730]'
            Pair<Integer, Integer> years = extractTwoYears(result.value, REGEXP_YEAR_RANGE_VERBAL);
            if (years != null) {
                result.rangeStartYear = years.getFirst();
                result.rangeEndYear = years.getSecond();
                result.valueStart = result.rangeStartYear.toString();
                result.valueEnd = result.rangeEndYear.toString();
                result.dateMin = MyDateTimeUtils.toYearStart(result.rangeStartYear);
                result.dateMax = MyDateTimeUtils.toYearEnd(result.rangeEndYear);
            }
        } else if (isPartialYear(result.value)) { //194u, 18--
            result.dateMin = MyDateTimeUtils.toYearStartFromPartialYear(result.value);
            result.dateMax = MyDateTimeUtils.toYearEndFromPartialYear(result.value);
        } else if (isPartialYearRange(result.value)) { //184u-19uu
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
            if (isDayMonthYear(result.valueStart)) { //7.4.1920
                String[] tokens = result.valueStart.split("\\.");
                int day = Integer.valueOf(tokens[0].trim());
                int month = Integer.valueOf(tokens[1].trim());
                int year = Integer.valueOf(tokens[2].trim());
                result.setStart(day, month, year);
                result.dateMin = MyDateTimeUtils.toDayStart(day, month, year);
            } else if (isMonthYear(result.valueStart)) { //4.1920
                String[] tokens = result.valueStart.split("\\.");
                int month = Integer.valueOf(tokens[0].trim());
                int year = Integer.valueOf(tokens[1].trim());
                result.setStart(month, year);
                result.dateMin = MyDateTimeUtils.toMonthStart(month, year);
            } else if (matchesRegexp(result.valueStart, REGEXP_YEAR)) { //'1920', '1920?', '1920]', '[1920', '[1920]', '[1920?]', '1920?]', 'p1920', 'c1920'
                Integer year = extractSingleYear(result.valueStart, REGEXP_YEAR);
                if (year != null) {
                    result.rangeStartYear = year;
                    result.dateMin = MyDateTimeUtils.toYearStart(year);
                }
            } else if (isPartialYear(result.valueStart)) { //194u, 18--
                result.dateMin = MyDateTimeUtils.toYearStartFromPartialYear(result.valueStart);
            } else {
                System.err.println(String.format("Chyba v datech objektu %s: nelze parsovat '%s'", pid, result.valueStart));
            }
        }
        //END
        if (result.valueEnd != null) {
            if (isDayMonthYear(result.valueEnd)) { //7.4.1920
                String[] tokens = result.valueEnd.split("\\.");
                int day = Integer.valueOf(tokens[0].trim());
                int month = Integer.valueOf(tokens[1].trim());
                int year = Integer.valueOf(tokens[2].trim());
                result.setEnd(day, month, year);
                result.dateMax = MyDateTimeUtils.toDayEnd(day, month, year);
            } else if (isMonthYear(result.valueEnd)) { //4.1920
                String[] tokens = result.valueEnd.split("\\.");
                int month = Integer.valueOf(tokens[0].trim());
                int year = Integer.valueOf(tokens[1].trim());
                result.setEnd(month, year);
                result.dateMax = MyDateTimeUtils.toMonthEnd(month, year);
            } else if (matchesRegexp(result.valueEnd, REGEXP_YEAR)) { //'1920', '1920?', '1920]', '[1920', '[1920]', '[1920?]', '1920?]', 'p1920', 'c1920'
                Integer year = extractSingleYear(result.valueEnd, REGEXP_YEAR);
                if (year != null) {
                    result.rangeEndYear = year;
                    result.dateMax = MyDateTimeUtils.toYearEnd(year);
                }
            } else if (isPartialYear(result.valueEnd)) { //194u, 18--
                result.dateMax = MyDateTimeUtils.toYearEndFromPartialYear(result.valueEnd);
            } else {
                System.err.println(String.format("Chyba v datech objektu %s: nelze parsovat '%s'", pid, result.valueEnd));
            }
        }
        result.updateInstantData();
        return result;
    }

    private Integer extractSingleYear(String str, String regexp) {
        Pattern pattern = Pattern.compile(regexp);
        Matcher m = pattern.matcher(str);
        if (m.find()) {
            return Integer.valueOf(m.group(1).trim());
        } else {
            return null;
        }
    }

    private Pair<Integer, Integer> extractTwoYears(String str, String regexp) {
        Pattern pattern = Pattern.compile(regexp);
        Matcher m = pattern.matcher(str);
        if (m.find()) {
            return new Pair(Integer.valueOf(m.group(1).trim()), Integer.valueOf(m.group(2).trim()));
        } else {
            return null;
        }
    }

    private boolean isDayMonthYear(String string) {
        return string != null && string.matches("\\d{1,2}\\.\\s*\\d{1,2}\\.\\s*\\d{1,4}"); //7.4.1920, 07. 04. 1920
    }

    private boolean isDayMonthYearRange1(String string) { //DD.MM.RRRR-DD.MM.RRRR
        return string != null && string.matches("\\d{1,2}\\.\\s*\\d{1,2}\\.\\s*\\d{1,4}\\s*-\\s*\\d{1,2}\\.\\s*\\d{1,2}\\.\\s*\\d{1,4}");
    }

    private boolean isDayMonthYearRange2(String string) { //DD.MM-DD.MM.RRRR
        return string != null && string.matches("\\d{1,2}\\.\\s*\\d{1,2}\\.\\s*-\\s*\\d{1,2}\\.\\s*\\d{1,2}\\.\\s*\\d{1,4}");
    }

    private boolean isDayMonthYearRange3(String string) { //DD.-DD.MM.RRRR
        return string != null && string.matches("\\d{1,2}\\.\\s*-\\s*\\d{1,2}\\.\\s*\\d{1,2}\\.\\s*\\d{1,4}");
    }

    private boolean isMonthYear(String string) {
        return string != null && string.matches("\\d{1,2}\\.\\s*\\d{1,4}"); //4.1920, 04. 1920
    }

    private boolean isMonthYearRange1(String string) { //MM.RRRR-MM.RRRR
        return string != null && string.matches("\\d{1,2}\\.\\s*\\d{1,4}\\s*-\\s*\\d{1,2}\\.\\s*\\d{1,4}");
    }

    private boolean isMonthYearRange2(String string) { //MM.-MM.RRRR
        return string != null && string.matches("\\d{1,2}\\.\\s*-\\s*\\d{1,2}\\.\\s*\\d{1,4}");
    }

    private boolean matchesRegexp(String str, String regexp) {
        return str != null && str.matches(regexp);
    }

    private boolean isPartialYear(String string) {
        return string != null && string.matches("[0-9]{1}[0-9ux\\-]{0,3}"); //19uu, 180-, 19u7
    }

    private boolean isPartialYearRange(String string) { //192u-19uu, NOT '18uu-195-' (combination of range and '-' for uknown value are not supported due to uncertainty)
        return string != null && string.matches("[0-9]{1}[0-9ux]{0,3}\\s*-\\s*[0-9]{1}[0-9ux]{0,3}");
    }
}
