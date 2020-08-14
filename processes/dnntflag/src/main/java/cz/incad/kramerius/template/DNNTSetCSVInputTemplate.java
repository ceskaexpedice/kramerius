package cz.incad.kramerius.template;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.kramerius.processes.filetree.TreeItem;
import org.kramerius.processes.filetree.TreeModelFilter;
import org.kramerius.processes.utils.TreeModelUtils;

import java.io.*;
import java.util.*;

public class DNNTSetCSVInputTemplate extends AbstractDNNTInputTemplate {


    protected  File rootDirectory() {
        String exportRoot = KConfiguration.getInstance().getConfiguration().getString("import.dnnt.diretory");
        return IOUtils.checkDirectory(exportRoot);
    }

    protected File csvFile() {
        return new File(rootDirectory().getAbsolutePath()+ File.separator+"dnnt.csv");
    }

    protected String process() {
        return "parametrizeddnntset";
    }

}