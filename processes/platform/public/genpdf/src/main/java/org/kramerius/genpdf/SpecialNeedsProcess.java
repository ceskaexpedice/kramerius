package org.kramerius.genpdf;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.lowagie.text.DocumentException;
import cz.incad.kramerius.document.guice.DocumentServiceModule;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.guice.PDFModule;
import cz.incad.kramerius.service.guice.I18NModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import org.apache.commons.io.FileUtils;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class SpecialNeedsProcess {

    public static Logger LOGGER = Logger.getLogger(SpecialNeedsProcess.class.getName());

    @ProcessMethod
    public static void generate(
            @ParameterName("pid") @IsRequired String pid,
            @ParameterName("output") @IsRequired String output,
            @ParameterName("user") String user
    ) {
        LOGGER.info("Generating PDF");
        LOGGER.info("pid: " + pid);
        LOGGER.info("output: " + output);
        LOGGER.info("output: " + user);

        Injector injector = Guice.createInjector(
                new SolrModule(),
                new RepoModule(),
                new NullStatisticsModule(),
                new PDFModule(),
                new ProcessModule(),
                new DocumentServiceModule(),
                new I18NModule()
        );

        SpecialNeedsService serv = injector.getInstance(SpecialNeedsService.class);
        try {
            File f = serv.generate(pid, user);
            FileUtils.moveFile(f, new File(output));
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (OutOfRangeException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        Injector injector = Guice.createInjector(
                new SolrModule(),
                new RepoModule(),
                new NullStatisticsModule(),
                new PDFModule(),
                new ProcessModule(),
                new DocumentServiceModule(),
                new I18NModule()
        );

        SpecialNeedsService serv = injector.getInstance(SpecialNeedsService.class);
        try {
            File f = serv.generate("pid", "user");
            //FileUtils.moveFile(f, new File(output));
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (OutOfRangeException e) {
            throw new RuntimeException(e);
        }
    }
}
