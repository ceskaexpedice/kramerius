package cz.incad.kramerius.rest.api.k5.client.item.display;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.RelsExtHelper;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import net.sf.json.JSONObject;

public class PDFDisplayType extends AbstractDisplayType {
	
	
	public static Logger LOGGER = Logger.getLogger(PDFDisplayType.class.getName());
	
	private static final String DISPLAY_TYPE = "PDF";

	@Inject
	@Named("securedFedoraAccess")
	FedoraAccess fedoraAccess;
	
	@Inject
	Provider<HttpServletRequest> requestProvider;
	
	
	@Override
	public boolean isApplicable(String pid, HashMap<String, Object> options) {
		try {
			String mimeTypeForStream = this.fedoraAccess.getMimeTypeForStream(calculateVieablePID(pid, this.fedoraAccess, options), FedoraUtils.IMG_FULL_STREAM);
			ImageMimeType imType = ImageMimeType.loadFromMimeType(mimeTypeForStream);
			return oneOf(imType, ImageMimeType.PDF);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}


	@Override
	public JSONObject getDisplay(String pid, HashMap<String, Object> options) {
		try {
			JSONObject display = new JSONObject();
			display.put("type", DISPLAY_TYPE);
			display.put("options", options(calculateVieablePID(pid, this.fedoraAccess, options)));
			return display;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
			return new JSONObject();
		}
	}


	private Object options(String pid) {
		JSONObject options = new JSONObject();
		String url = ApplicationURL.applicationURL(this.requestProvider.get()).toString()+"nimg/IMG_FULL/"+pid; // page?
		//nimg/IMG_FULL/'+viewerOptions.uuid+'#page='+page;
		options.put("url", url);
		return options;
	}
	
	
}
