package cz.i.kramerius.gwtviewers.server.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cz.i.kramerius.gwtviewers.client.SimpleImageTO;
import cz.incad.kramerius.FedoraModels;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

public class UtilsDecorator {


	public static FedoraModels getModel(KConfiguration kConfiguration, String uuid) throws LexerException, ParserConfigurationException, IOException, SAXException {
		String callIdentification = CallCache.makeIdent(UtilsDecorator.class.getName(), "getModel", uuid);
		if (CallCache.isInCache(callIdentification)) {
			return (FedoraModels) CallCache.valueFromCache(callIdentification);
		} else {
			FedoraModels model = Utils.getModel(kConfiguration, uuid);
			CallCache.cacheValue(callIdentification, model);
			return model;
		}
	}
	
	public static ArrayList<SimpleImageTO> getPages(KConfiguration kConfiguration,String currentProcessinguuid) throws IOException,
			ParserConfigurationException, SAXException, LexerException {
		String callIdentification = CallCache.makeIdent(UtilsDecorator.class.getName(), "getPages", currentProcessinguuid);
		if (CallCache.isInCache(callIdentification)) {
			return (ArrayList<SimpleImageTO>) CallCache.valueFromCache(callIdentification);
		} else {
			ArrayList<SimpleImageTO> pages = Utils.getPages(kConfiguration, currentProcessinguuid);
			CallCache.cacheValue(callIdentification, pages);
			return pages;
		}
	}
}