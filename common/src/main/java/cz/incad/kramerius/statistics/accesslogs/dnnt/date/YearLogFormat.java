package cz.incad.kramerius.statistics.accesslogs.dnnt.date;

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

    private static Pattern PARTIAL_YEAR_PATTERN =
            Pattern.compile("[0-9]{1}[0-9ux\\-]{0,3}");



    @Override
    public String format(String date) {
        // try to parse by default ndk parser
        List<String> years = fullYears(date);
        if (years.isEmpty()) {
            years = partialYears(date);
        }
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        List<Integer> collect = years.stream().map(Integer::valueOf).map(y->{
            return Math.min(y, currentYear);
        }).collect(Collectors.toList());


        sort(collect, (first,second) -> {
            return second.compareTo(first);
        });

        return collect.isEmpty() ? null : collect.get(0).toString();
    }

    private List<String> partialYears(String date) {
        List<String> dates = find(PARTIAL_YEAR_PATTERN, date);
        List<String> retvals = new ArrayList<>();
        for (String d :  dates) {
            d = d.replaceAll("[^0-9]", "9");
            retvals.add(d);
        }
        return retvals;
    }

    private List<String> fullYears(String date) {
        return find(YEAR_PATTERN, date);
    }

    private List<String> find(Pattern pattern, String date) {
        List<String> retvals = new ArrayList<>();
        Matcher dateMatcher = pattern.matcher(date);
        while (dateMatcher.find()) {
            String group = dateMatcher.group();
            retvals.add(group);
        }
        return retvals;
    }
}
