package cz.cas.lib.knav.templates;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.cas.lib.knav.ApplyMWUtils;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.def.DefaultTemplate;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ParametrizedIterateRepoApplyMWTemplate implements ProcessInputTemplate {

    @Inject
    ResourceBundleService resourceBundleService;
    
    @Inject
    Provider<Locale> localeProvider;

    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer,
            Properties paramsMapping) throws IOException {
        InputStream iStream = this.getClass().getResourceAsStream("_template.st");

        int mw = ApplyMWUtils.defaultConfiguredWall(KConfiguration.getInstance().getConfiguration());

        ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localeProvider.get());

        StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = templateGroup.getInstanceOf("form");
        template.setAttribute("defaultValue", new Integer(mw));
        template.setAttribute("bundle", DefaultTemplate.resourceBundleMap(resbundle));
        template.setAttribute("iterateRepo",true);
        writer.write(template.toString());
       
    }
}
