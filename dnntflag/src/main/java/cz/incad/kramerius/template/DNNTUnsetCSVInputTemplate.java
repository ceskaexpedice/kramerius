package cz.incad.kramerius.template;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import java.io.File;

public class DNNTUnsetCSVInputTemplate  extends AbstractDNNTCSVInputTemplate {

    protected String templateName() {
        return "paramterizedcsvdnnt.st";
    }

    protected  File rootDirectory() {
        String exportRoot = KConfiguration.getInstance().getConfiguration().getString("import.dnnt.diretory");
        return IOUtils.checkDirectory(exportRoot);
    }

    protected File csvFile() {
        return new File(rootDirectory().getAbsolutePath()+ File.separator+"unsetdnnt.csv");
    }

    protected String process() {
        return "parametrizeddnntunset";
    }

    @Override
    protected String labeledProcess() {
        return "parametrizeddnntlabelunset";
    }
}
