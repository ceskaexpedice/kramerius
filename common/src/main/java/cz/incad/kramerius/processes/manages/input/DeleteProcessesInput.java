/*
 * Copyright (C) 2012 Pavel Stastny
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
/**
 * 
 */
package cz.incad.kramerius.processes.manages.input;

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

import cz.incad.kramerius.processes.BatchStates;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.stemplates.ResourceBundleUtils;

/**
 * @author pavels
 *
 */
public class DeleteProcessesInput implements ProcessInputTemplate{


    @Inject
    KConfiguration configuration;
    
    @Inject
    Provider<Locale> localesProvider;
    
    @Inject
    ResourceBundleService resourceBundleService;

    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {
        InputStream iStream = this.getClass().getResourceAsStream("res/manages.stg");
        StringTemplateGroup processManages = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = processManages.getInstanceOf("form");

        States[] sts = new States[] {States.NOT_RUNNING, States.FINISHED,States.FAILED, States.KILLED };
        template.setAttribute("states",sts);
        template.setAttribute("batchStates", BatchStates.values());
        ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localesProvider.get());
        template.setAttribute("bundle", ResourceBundleUtils.resourceBundleMap(resbundle));
        writer.write(template.toString());
    }
}
