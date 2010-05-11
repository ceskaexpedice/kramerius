package cz.incad.kramerius.gwtviewers.server.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraModels;
import cz.incad.kramerius.gwtviewers.client.SimpleImageTO;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

public class UtilsDecorator {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(UtilsDecorator.class.getName());

	public synchronized static FedoraModels getModel(KConfiguration kConfiguration, String uuid) throws LexerException, ParserConfigurationException, IOException, SAXException {
		String callIdentification = CallCache.makeIdent(UtilsDecorator.class.getName(), "getModel", uuid);
		if (CallCache.isInCache(callIdentification)) {
			return (FedoraModels) CallCache.valueFromCache(callIdentification);
		} else {
			FedoraModels model = Utils.getModel(kConfiguration, uuid);
			CallCache.cacheValue(callIdentification, model);
			return model;
		}
	}
	
	public synchronized static ArrayList<SimpleImageTO> getPages(KConfiguration kConfiguration, HttpServletRequest request,String currentProcessinguuid) throws IOException,
			ParserConfigurationException, SAXException, LexerException {
		String callIdentification = CallCache.makeIdent(UtilsDecorator.class.getName(), "getPages", currentProcessinguuid);
		if (CallCache.isInCache(callIdentification)) {
			ArrayList<SimpleImageTO> valueFromCache = (ArrayList<SimpleImageTO>) CallCache.valueFromCache(callIdentification);
			LOGGER.info("Value from cache for argument("+currentProcessinguuid+")"+(valueFromCache == null ? "null" : valueFromCache.size()));
			return valueFromCache;
		} else {
			ArrayList<SimpleImageTO> pages = Utils.getPages(kConfiguration, request, currentProcessinguuid);
			CallCache.cacheValue(callIdentification, pages);
			ArrayList<SimpleImageTO> valueFromCache = (ArrayList<SimpleImageTO>) CallCache.valueFromCache(callIdentification);
			if (valueFromCache == null) throw new IllegalStateException("Cache errror");
			return pages;
		}
	}
}