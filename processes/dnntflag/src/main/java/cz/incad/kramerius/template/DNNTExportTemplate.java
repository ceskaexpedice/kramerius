package cz.incad.kramerius.template;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.LicensesManagerException;
import cz.incad.kramerius.service.ResourceBundleService;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import java.io.*;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DNNTExportTemplate implements ProcessInputTemplate {

    public static final Logger LOGGER = Logger.getLogger(DNNTExportTemplate.class.getName());

    @Inject
    ResourceBundleService resourceBundleService;

    @Inject
    Provider<Locale> localeProvider;

    @Inject
    LicensesManager licensesManager;

    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {
        try {
            InputStream iStream = this.getClass().getResourceAsStream("parametrizedexportdnnt.st");
            StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
            StringTemplate template = templateGroup.getInstanceOf("form");
            ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localeProvider.get());

            template.setAttribute("bundle", AbstractDNNTCSVInputTemplate.resourceBundleMap(resbundle));
            template.setAttribute("process", "parametrizeddnntexport");
            template.setAttribute("allLabels", licensesManager.getLabels().stream().map(License::getName).collect(Collectors.toList()));

            writer.write(template.toString());
        } catch (LicensesManagerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }
}
