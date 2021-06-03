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
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NKPLogProcess {


    public static Logger LOGGER = Logger.getLogger(NKPLogProcess.class.getName());

    // annonymize


    @Process
    public static void  process(@ParameterName("dateFrom")String from,
                                @ParameterName("dateTo")String to,
                                @ParameterName("folder")String folder,
                                @ParameterName("institution")String institution,
                                @ParameterName("anonymization")String anonymization
            ) throws ParseException, IOException, NoSuchAlgorithmException {


        List<String> annonymizationKeys = anonymization != null ? Arrays.asList(anonymization.split(",")) : new ArrayList<>();


        Client client = Client.create();

        Date start = StringUtils.isAnyString(to) ? StatisticReport.DATE_FORMAT.parse(from) : new Date();
        Date end = StringUtils.isAnyString(to) ? StatisticReport.DATE_FORMAT.parse(to) : new Date();
        Date processingDate = start;

        while(processingDate.before(end)) {

            LocalDateTime localDateTime = processingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            localDateTime = localDateTime.plusDays(1);
            Date nextDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

            String url = KConfiguration.getInstance().getConfiguration().getString("api.point")+(KConfiguration.getInstance().getConfiguration().getString("api.point").endsWith("/") ? "" : "/") + String.format("admin/statistics/nkp/export?action=READ&dateFrom=%s&dateTo=%s&visibility=ALL", StatisticReport.DATE_FORMAT.format(processingDate), StatisticReport.DATE_FORMAT.format(nextDate));
            WebResource r = client.resource(url);
            String authHeader = System.getProperty(ProcessStarter.AUTH_TOKEN_KEY);
            String groupHeader = System.getProperty(ProcessStarter.TOKEN_KEY);


            WebResource.Builder builder = r.header("auth-token", authHeader).header("token", groupHeader).accept(MediaType.APPLICATION_JSON);
            String content = builder.get(String.class);

            File outputFolder = new File(folder);
            if (!outputFolder.exists())  outputFolder.mkdirs();
            if (outputFolder.exists()) {

                StringBuilder stringBuilder = new StringBuilder();

                BufferedReader bufferedReader = new BufferedReader(new StringReader(content));
                String line = null;
                while((line = bufferedReader.readLine()) != null) {

                    JSONObject lineJSONObject = new JSONObject(line);
                    annonymizationKeys.stream().forEach(key-> {
                        if (lineJSONObject.has(key)) {
                            Object o = lineJSONObject.get(key);
                            if (o instanceof  String) {
                                String stringToBeHashed = o.toString();
                                String newVal = null;
                                if (stringToBeHashed.contains("@")) {
                                    String[] split = stringToBeHashed.split("@");
                                    newVal = String.format("%s@%s", hashVal(split[0]), hashVal(split[1]));
                                } else {
                                    newVal = hashVal(stringToBeHashed);
                                }
                                lineJSONObject.put(key, newVal);
                            }
                        }
                    });

                    stringBuilder.append(lineJSONObject.toString()).append("\n");
                }


                File outputFile = new File(outputFolder, String.format("statistics-%s-%s.log", institution, StatisticReport.DATE_FORMAT.format(processingDate)));
                LOGGER.info(String.format("Storing log to file %s",outputFile.getAbsolutePath()));
                IOUtils.saveToFile(stringBuilder.toString(), outputFile);
                //IOUtils.saveToFile(jsonObject.toString(), outputFile);
            }
            processingDate = nextDate;

        }
    }


    public static String hashVal(String value)  {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            Base64.Encoder base64Encoder = Base64.getEncoder();
            md5.update(value.getBytes("UTF-8"));
            return base64Encoder.encodeToString(md5.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return value;
        }
    }
}
