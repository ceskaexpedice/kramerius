package cz.incad.kramerius.template;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import java.io.*;

public class DNNTSetCSVInputTemplate extends AbstractDNNTCSVInputTemplate {

    protected String templateName() {
        return "paramterizedcsvdnnt.st";
    }


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

    @Override
    protected String labeledProcess() {
        return "parametrizeddnntlabelset";
    }
}