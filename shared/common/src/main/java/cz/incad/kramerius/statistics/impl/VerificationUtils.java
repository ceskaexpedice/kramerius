package cz.incad.kramerius.statistics.impl;

import java.text.ParseException;
import java.util.List;

import cz.incad.kramerius.statistics.StatisticReport;

public class VerificationUtils {
	
	private VerificationUtils() {}

	public static void dateVerification(List<String> list, String date) {
		if (date != null && !"".equals(date.trim())) {
			try {
				StatisticReport.DATE_FORMAT.parse(date);
			} catch (ParseException e) {
				String dateFrom = e.getMessage();
				list.add(dateFrom);
			}
		}
	}
}
