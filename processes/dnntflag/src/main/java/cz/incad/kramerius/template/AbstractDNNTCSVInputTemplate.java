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
import org.kramerius.processes.filetree.TreeItem;
import org.kramerius.processes.filetree.TreeModelFilter;
import org.kramerius.processes.utils.TreeModelUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public abstract  class AbstractDNNTCSVInputTemplate implements ProcessInputTemplate {

    @Inject
    ResourceBundleService resourceBundleService;

    @Inject
    Provider<Locale> localeProvider;

    @Inject
    LicensesManager licensesManager;

    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {
        try {
            InputStream iStream = this.getClass().getResourceAsStream(templateName());
            StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
            StringTemplate template = templateGroup.getInstanceOf("form");
            ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localeProvider.get());


            TreeItem rootNode = TreeModelUtils.prepareTreeModel(rootDirectory(), new TreeModelFilter() {
                String[] NAMES = {"lp", "exported", "deepZoom"};

                @Override
                public boolean accept(File file) {
                    String sname = file.getName();
                    for (String nm : NAMES) {
                        if (nm.equals(sname)) return false;
                    }
                    return true;
                }
            }, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) return true;
                    else {
                        if (pathname.getName().startsWith(".")) return false;
                        else return pathname.getName().toLowerCase().endsWith(".csv") ?  true : false;
                    }
                }
            });

            Random randomGenerator = new Random();
            int idPostfix = randomGenerator.nextInt(2000);

            template.setAttribute("csvRootDirectory", rootNode);
            template.setAttribute("bundle", resourceBundleMap(resbundle));
            template.setAttribute("csvfile", csvFile().getAbsolutePath());
            template.setAttribute("postfixdiv",""+idPostfix);

            template.setAttribute("process", process());
            template.setAttribute("labelProcess", labeledProcess());

            template.setAttribute("allLabels", licensesManager.getLabels().stream().map(License::getName).collect(Collectors.toList()));

            writer.write(template.toString());
        } catch (LicensesManagerException e) {
            throw new IOException(e.getMessage(), e);
        }
    }



    public static Map<String, String> resourceBundleMap(ResourceBundle bundle) {
        Map<String, String> map = new HashMap<String, String>();
        Set<String> keySet = bundle.keySet();
        for (String key : keySet) {
            map.put(key, bundle.getString(key));
        }
        return map;
    }

    protected abstract  String templateName();
    protected abstract File rootDirectory();
    protected  abstract File csvFile();

    protected abstract String process();

    protected abstract String labeledProcess();
}
