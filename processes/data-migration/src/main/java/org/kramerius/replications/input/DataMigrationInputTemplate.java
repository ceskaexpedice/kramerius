package org.kramerius.replications.input;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.service.ResourceBundleService;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.*;

public class DataMigrationInputTemplate implements ProcessInputTemplate {

    @Inject
    ResourceBundleService resourceBundleService;

    @Inject
    Provider<Locale> localeProvider;

    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {
        InputStream iStream = DataMigrationInputTemplate.class.getResourceAsStream("data-migration-template.st");
        StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = templateGroup.getInstanceOf("form");
        ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localeProvider.get());
        template.setAttribute("bundle", resourceBundleMap(resbundle));
        template.setAttribute("handle", "");

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
