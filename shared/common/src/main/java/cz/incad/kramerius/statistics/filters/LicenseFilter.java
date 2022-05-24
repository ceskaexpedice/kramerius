package cz.incad.kramerius.statistics.filters;

import java.util.logging.Logger;

public class LicenseFilter implements  StatisticsFilter {

	public static final Logger LOGGER = Logger.getLogger(LicenseFilter.class.getName());
		
	private String licence;


	public LicenseFilter(String licence) {
		super();
		this.licence = licence;
	}
		
	public String getLicence() {
		return licence;
	}

}
