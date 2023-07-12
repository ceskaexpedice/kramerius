package cz.inovatika.sdnnt.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.security.labels.Label;
import cz.incad.kramerius.security.labels.LabelsManager;
import cz.incad.kramerius.security.labels.LabelsManagerException;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class SDNNTTemplate implements ProcessInputTemplate {
	
	public static final String DEFAULT_SDNNT_ENDPOINT = "https://sdnnt.nkp.cz/sdnnt/api/v1.0/lists/changes";
	//public static final String DEFAULT_KRAMERIUS_INSTANCE="https://kramerius.domain.cz/search";
	
    public static final Logger LOGGER = Logger.getLogger(SDNNTTemplate.class.getName());

    @Inject
    ResourceBundleService resourceBundleService;

    @Inject
    Provider<Locale> localeProvider;

    @Inject
    LabelsManager labelsManager;
    
    @Inject
    Provider<HttpServletRequest> provider;

    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {
    	String api = KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.local.api");
    	String acronym = KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.acronym");
    	String sdnntInstance = KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.endpoint",
                "https://sdnnt.nkp.cz/sdnnt/api/v1.0/lists/changes");
    	String version = KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.version", "v5");
    	
    	
    	//String krameriusInstance = ApplicationURL.applicationURL(provider.get()); // KConfiguration.getInstance().getConfiguration().getString("sdnnt.kramerius.instance", DEFAULT_KRAMERIUS_INSTANCE);
    	
        InputStream iStream = this.getClass().getResourceAsStream("sdnnt.st");
        StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = templateGroup.getInstanceOf("form");
        ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localeProvider.get());

        template.setAttribute("bundle", resourceBundleMap(resbundle));
        //template.setAttribute("process", "parametrizedsdnntlist");
        template.setAttribute("sdnntendoint", sdnntInstance);
        template.setAttribute("kraminstance", api);
        template.setAttribute("acronym", acronym);
        

        writer.write(template.toString());
    }
    
    public static Map<String, String> resourceBundleMap(ResourceBundle bundle) {
        Map<String, String> map = new HashMap<String, String>();
        Set<String> keySet = bundle.keySet();
        for (String key : keySet) {
            map.put(key, bundle.getString(key));
        }
        return map;
    }

}
