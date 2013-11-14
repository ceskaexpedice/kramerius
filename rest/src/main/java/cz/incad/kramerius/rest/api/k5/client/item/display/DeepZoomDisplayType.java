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
import cz.incad.kramerius.utils.RelsExtHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import net.sf.json.JSONObject;

public class DeepZoomDisplayType extends AbstractDisplayType {

	public static final Logger LOGGER = Logger.getLogger(DeepZoomDisplayType.class.getName());
	
	public static final String DISPLAY_TYPE = "ZOOMIFY";
	
	@Inject
	@Named("securedFedoraAccess")
	FedoraAccess fedoraAccess;
	
	@Inject
	Provider<HttpServletRequest> requestProvider;
	

	
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

	@Override
	public boolean isApplicable(String pid, HashMap<String, Object> options) {
		try {
			String url = RelsExtHelper.getRelsExtTilesUrl(calculateVieablePID(pid, this.fedoraAccess, options), fedoraAccess);
			if (url != null) {
				String zViewer = KConfiguration.getInstance().getProperty("zoom.viewer","zoomify");
				return !zViewer.equals("zoomify");
			} else return false;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		}
		return false;
	}
	
	private Object options(String vpid) {
		JSONObject options = new JSONObject();
		String url = ApplicationURL.applicationURL(this.requestProvider.get()).toString()+"/deepZoom/"+vpid;
		options.put("url", url);
		return options;
	}

}
