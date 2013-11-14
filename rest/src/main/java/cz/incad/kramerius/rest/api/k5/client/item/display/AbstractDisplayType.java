package cz.incad.kramerius.rest.api.k5.client.item.display;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.RelsExtHelper;
import cz.incad.kramerius.utils.imgs.ImageMimeType;


public abstract class AbstractDisplayType implements DisplayType {

	public static final Logger LOGGER = Logger
			.getLogger(AbstractDisplayType.class.getName());

	public static final String FIRST_VIEWABLE_PID = "first";

	public static final int SORTING_KEY = 10000;

	@Inject
	@Named("securedFedoraAccess")
	FedoraAccess fedoraAccess;


	@Override
	public int getSortingKey() {
		return SORTING_KEY;
	}

	protected boolean oneOf(ImageMimeType imType, ImageMimeType... expTypes) {
		for (ImageMimeType eType : expTypes) {
			if (eType.equals(imType))
				return true;
		}
		return false;
	}
	
	
	
	// cannot bind as singleton
	protected String calculateVieablePID(String rootPid,
			FedoraAccess fedoraAccess, HashMap<String, Object> options) throws IOException {
		if (!options.containsKey(FIRST_VIEWABLE_PID)) {
			options.put(FIRST_VIEWABLE_PID, fedoraAccess.findFirstViewablePid(rootPid));
		}
		return (String) options.get(FIRST_VIEWABLE_PID);
	}

}
