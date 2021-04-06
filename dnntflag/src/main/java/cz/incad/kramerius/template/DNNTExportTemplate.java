package cz.incad.kramerius.template;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.service.ResourceBundleService;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.kramerius.processes.filetree.TreeItem;
import org.kramerius.processes.filetree.TreeModelFilter;
import org.kramerius.processes.utils.TreeModelUtils;

import java.io.*;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

public class DNNTExportTemplate implements ProcessInputTemplate {

    @Inject
    ResourceBundleService resourceBundleService;

    @Inject
    Provider<Locale> localeProvider;

    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {
        InputStream iStream = this.getClass().getResourceAsStream("parametrizedexportdnnt.st");
        StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = templateGroup.getInstanceOf("form");
        ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localeProvider.get());

        template.setAttribute("bundle", AbstractDNNTCSVInputTemplate.resourceBundleMap(resbundle));
        template.setAttribute("process", "parametrizeddnntexport");

        writer.write(template.toString());
    }
}
