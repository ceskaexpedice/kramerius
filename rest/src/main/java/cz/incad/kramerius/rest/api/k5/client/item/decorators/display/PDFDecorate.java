/*
 * Copyright (C) 2013 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.rest.api.k5.client.item.decorators.display;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import net.sf.json.JSONObject;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.rest.api.k5.client.AbstractDecorator;
import cz.incad.kramerius.rest.api.k5.client.AbstractItemDecorator;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.imgs.ImageMimeType;

public class PDFDecorate extends AbstractDisplayDecorate {
	
	public static final Logger LOGGER = Logger.getLogger(ZoomDecorate.class.getName());

	public static final String KEY = AbstractDisplayDecorate.key("PDF");

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public void decorate(JSONObject jsonObject, Map<String, Object> context) {
		try {
			String pidFromJSON = getPidFromJSON(jsonObject);
			if (!PIDSupport.isComposedPID(pidFromJSON) && this.fedoraAccess.isImageFULLAvailable(pidFromJSON)) {
				String mimeTypeForStream = this.fedoraAccess.getMimeTypeForStream(pidFromJSON, FedoraUtils.IMG_FULL_STREAM);
				ImageMimeType imType = ImageMimeType.loadFromMimeType(mimeTypeForStream);
				if (imType != null && ImageMimeType.PDF.equals(imType)) {
					jsonObject.put("pdf", options(pidFromJSON));
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		}
	}

	
	private Object options(String pid) {
		JSONObject options = new JSONObject();
		String url = ApplicationURL.applicationURL(this.requestProvider.get()).toString()+"/img?pid="+pid+"&stream="+FedoraUtils.IMG_FULL_STREAM+"&action=GETRAW";
		options.put("url", url);
		return options;
	}

	@Override
	public boolean apply(JSONObject jsonObject, String context) {
		TokenizedPath tpath = super.itemContext(tokenize(context));
		return (tpath.isParsed() && tpath.getRestPath().isEmpty());
	}


	public static void main(String[] args) {
		System.out.println(PDFDecorate.KEY);
	}
}
