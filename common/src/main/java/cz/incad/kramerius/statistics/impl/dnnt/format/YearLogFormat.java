package cz.incad.kramerius.statistics.impl.dnnt.format;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.security.impl.criteria.mw.DateLexer;
import cz.incad.kramerius.security.impl.criteria.mw.DatesParser;

import java.io.StringReader;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.*;

public class YearLogFormat implements DNNTStatisticsDateFormat{

    public static final Logger LOGGER = Logger.getLogger(YearLogFormat.class.getName());

    private static Pattern YEAR_PATTERN =
            Pattern.compile("[0-9]{4}");

    @Override
    public String format(String date) {
        // try to parse by default ndk parser
        try {
            DatesParser dateParse = new DatesParser(new DateLexer(
                    new StringReader(date)));
            Date parsed = dateParse.dates();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsed);
            return ""+calendar.get(Calendar.YEAR);
        } catch (TokenStreamException | RecognitionException e) {
            List<String> years = tryToFindYears(date);
            List<Integer> collect = years.stream().map(Integer::valueOf).collect(Collectors.toList());
            sort(collect, (first,second) -> {
                return second.compareTo(first);
            });
            return collect.isEmpty() ? date : collect.get(0).toString();
        }
    }

    private List<String> tryToFindYears(String date) {
        List<String> retvals = new ArrayList<>();
        Matcher dateMatcher = YEAR_PATTERN.matcher(date);
        while (dateMatcher.find()) {
            String group = dateMatcher.group();
            retvals.add(group);
        }
        return retvals;
    }
}
