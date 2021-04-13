package cz.incad.kramerius.statistics.impl.nkp;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.filters.StatisticsFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import net.sf.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class NKPLogProcess {


    public static Logger LOGGER = Logger.getLogger(NKPLogProcess.class.getName());

    // annonymize


    @Process
    public static void  process(@ParameterName("dateFrom")String from,
                                @ParameterName("dateTo")String to,
                                @ParameterName("folder")String folder,
                                @ParameterName("institution")String institution

            ) throws ParseException, IOException {


        Client client = Client.create();

        Date start = StatisticReport.DATE_FORMAT.parse(from);
        Date end = StatisticReport.DATE_FORMAT.parse(to);
        Date processingDate = start;

        while(processingDate.before(end)) {

            LocalDateTime localDateTime = processingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            localDateTime = localDateTime.plusDays(1);
            Date nextDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());


            String url = KConfiguration.getInstance().getConfiguration().getString("api.point")+(KConfiguration.getInstance().getConfiguration().getString("api.point").endsWith("/") ? "" : "/") + String.format("admin/statistics/nkp/export?action=READ&dateFrom=%s&dateTo=%s&visibility=ALL", StatisticReport.DATE_FORMAT.format(processingDate), StatisticReport.DATE_FORMAT.format(nextDate));
            //LOGGER.info(String.format("Requesting api point %s", url));
            WebResource r = client.resource(url);
            String authHeader = System.getProperty(ProcessStarter.AUTH_TOKEN_KEY);
            String groupHeader = System.getProperty(ProcessStarter.TOKEN_KEY);


            WebResource.Builder builder2 = r.header("auth-token", authHeader).header("token", groupHeader).accept(MediaType.APPLICATION_JSON);
            String content = builder2.get(String.class);

            File outputFolder = new File(folder);
            if (!outputFolder.exists())  outputFolder.mkdirs();
            if (outputFolder.exists()) {
                File outputFile = new File(outputFolder, String.format("statistics-%s-%s.log", institution, StatisticReport.DATE_FORMAT.format(processingDate)));
                LOGGER.info(String.format("Storing log to file %s",outputFile.getAbsolutePath()));
                IOUtils.saveToFile(content, outputFile);
            }


            processingDate = nextDate;

        }
    }
}
