package cz.incad.kramerius.statistics.impl.nkp;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.Constants;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.stemplates.ResourceBundleUtils;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.kramerius.processes.filetree.TreeItem;
import org.kramerius.processes.filetree.TreeModelFilter;
import org.kramerius.processes.utils.TreeModelUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ParametrizedNKPInputTemplate implements ProcessInputTemplate {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ParametrizedNKPInputTemplate.class.getName());

    static final SimpleDateFormat FORMAT = new SimpleDateFormat("yy.MM.dd");

    @Inject
    KConfiguration configuration;

    @Inject
    Provider<Locale> localesProvider;

    @Inject
    ResourceBundleService resourceBundleService;

    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR,-1);

        String folder = KConfiguration.getInstance().getConfiguration().getString("nkp.logs.root.dir", Constants.WORKING_DIR);
        File homeFolder = new File(folder);

        InputStream iStream = this.getClass().getResourceAsStream("nkp.stg");

        TreeItem rootNode = TreeModelUtils.prepareTreeModel(homeFolder,new TreeModelFilter() {
            String[] NAMES = { "lp","exported","deepZoom" , "data"};
            @Override
            public boolean accept(File file) {
                String sname = file.getName();
                for (String nm : NAMES) {
                    if (nm.equals(sname)) return false;
                }
                return true;
            }
        });

        Random randomGenerator = new Random();
        int idPostfix = randomGenerator.nextInt(2000);

        StringTemplateGroup parametrizedconvert = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = parametrizedconvert.getInstanceOf("form");

        ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localesProvider.get());
        template.setAttribute("bundle", ResourceBundleUtils.resourceBundleMap(resbundle));
        template.setAttribute("postfixdiv",""+idPostfix);

        template.setAttribute("nkpLogsDirectory", homeFolder.getAbsolutePath());
        template.setAttribute("rootDirectory",  rootNode);
        template.setAttribute("dateFrom",  FORMAT.format(cal.getTime()));
        template.setAttribute("dateTo",  FORMAT.format(new Date()));



        writer.write(template.toString());
    }
}
