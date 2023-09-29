package cz.incad.kramerius.statistics.impl.nkp;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.starter.ProcessStarter;
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
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class NKPLogProcess {

    private static final List<String> DEFAULT_ANONYMIZATION_PROPERTIES = Arrays.asList(
            "username",
            "session_eppn",
            "dnnt_user",
            "eduPersonUniqueId",
            "affilation",
            "remoteAddr",
            "eduPersonPrincipalName",
            "email",
            "preffered_user_name");
    
    private static final String NKP_LOGS_FOLDER_KEY = "nkp.logs.folder";
    private static final String NKP_LOGS_VISIBILITY_KEY = "nkp.logs.visibility";
    private static final String NKP_LOGS_INSTITUTION_KEY = "nkp.logs.institution";
    private static final String NKP_LOGS_ANONYMIZATION_KEY = "nkp.logs.anonymization";
    public static Logger LOGGER = Logger.getLogger(NKPLogProcess.class.getName());

    public static void main(String[] args) throws NoSuchAlgorithmException, ParseException, IOException {
        LOGGER.log(Level.INFO, "Process parameters: " + Arrays.asList(args).toString());
        if (args.length > 2) {
            String from = args[1];
            String to = args[2];

            String defaultInst = KConfiguration.getInstance().getConfiguration().getString("acronym");
            String folder = KConfiguration.getInstance().getConfiguration().getString(NKP_LOGS_FOLDER_KEY, System.getProperty("java.io.tmpdir"));
            String visibility = KConfiguration.getInstance().getConfiguration().getString(NKP_LOGS_VISIBILITY_KEY, "ALL");
            String institution = KConfiguration.getInstance().getConfiguration().getString(NKP_LOGS_INSTITUTION_KEY, StringUtils.isAnyString(defaultInst) ? defaultInst :  "-none-");
            List<Object> anonymization = KConfiguration.getInstance().getConfiguration().getList(NKP_LOGS_ANONYMIZATION_KEY, 
                    DEFAULT_ANONYMIZATION_PROPERTIES);

            process(from, to, folder, institution, visibility, anonymization);
        }
    }

    public static void process(String from,
                               String to,
                               String folder,
                               String institution,
                               String visibility,
                               List<Object> anonymization
    ) throws ParseException, IOException, NoSuchAlgorithmException {
        List<String> logs = new ArrayList<>();
        
        //TODO: I18N
        ProcessStarter.updateName(String.format("Generování NKP logů pro období %s - %s", from, to));

        // folder, institution, visibility from configuration
        LOGGER.info(String.format("Process parameters dateFrom=%s, dateTo=%s, folder=%s, institution=%s,visibility=%s,anonymization=%s", from, to, folder, institution, visibility, anonymization));
        
        List<String> annonymizationKeys = anonymization != null ? anonymization.stream().map(Objects::toString).collect(Collectors.toList()) : new ArrayList<>();

        Client client = Client.create();

        Date start = StringUtils.isAnyString(to) ? StatisticReport.DATE_FORMAT.parse(from) : new Date();
        Date end = StringUtils.isAnyString(to) ? StatisticReport.DATE_FORMAT.parse(to) : new Date();

        Date processingDate = start;
        while (processingDate.before(end)) {

            LocalDateTime localDateTime = processingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            localDateTime = localDateTime.plusDays(1);
            Date nextDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

            String url = KConfiguration.getInstance().getConfiguration().getString("api.admin.v7.point") + (KConfiguration.getInstance().getConfiguration().getString("api.point").endsWith("/") ? "" : "/") + String.format("statistics/nkp/export?action=READ&dateFrom=%s&dateTo=%s&visibility=%s", StatisticReport.DATE_FORMAT.format(processingDate), StatisticReport.DATE_FORMAT.format(nextDate), visibility);
            WebResource r = client.resource(url);

            WebResource.Builder builder = r.accept(MediaType.APPLICATION_JSON);
            InputStream clientResponse = builder.get(InputStream.class);


            File outputFolder = new File(folder);
            if (!outputFolder.exists()) outputFolder.mkdirs();
            if (outputFolder.exists()) {

                File outputFile = new File(outputFolder, String.format("statistics-%s-%s.log", institution, StatisticReport.DATE_FORMAT.format(processingDate)));
                logs.add(outputFile.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(outputFile);
                try (OutputStreamWriter fileWriter = new OutputStreamWriter(fos, "UTF-8")) {

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientResponse, "UTF-8"));
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {

                        JSONObject lineJSONObject = new JSONObject(line);
                        annonymizationKeys.stream().forEach(key -> {
                            if (lineJSONObject.has(key)) {
                                Object o = lineJSONObject.get(key);
                                if (o instanceof String) {
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

                        fileWriter.write(lineJSONObject.toString() + "\n");

                        // memory dump
                        boolean nkpLogMemoryDump = KConfiguration.getInstance().getConfiguration().getBoolean("nkp.logs.memorydump", false);
                        if (nkpLogMemoryDump) {
                            Runtime runtime = Runtime.getRuntime();
                            long freeMemory = runtime.freeMemory();
                            long totalMemory = runtime.totalMemory();

                            long usedMemory = totalMemory - freeMemory;

                            long maxMemory = runtime.maxMemory();
                            double ratioA = (double) usedMemory / (double) totalMemory;
                            double ratioB = (double) usedMemory / (double) maxMemory;

                            LOGGER.fine(String.format("Free memory (bytes) %d,Used memory (bytes) %d, Total memory(bytes) %d, Max memory (bytes) %d, ratio (used/totalmemory)  %f,ratio (used/maxmemory)  %f", freeMemory, usedMemory, totalMemory, maxMemory, ratioA, ratioB));
                        }
                    }
                }
                LOGGER.info(String.format("Storing log to file %s", outputFile.getAbsolutePath()));
            }
            processingDate = nextDate;
        }
        
        
        try {
            File zipFile = new File(new File(folder), String.format("statistics-%s-%s.zip",  StatisticReport.DATE_FORMAT.format(start), StatisticReport.DATE_FORMAT.format(end)));
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            
            for (String lF : logs) {
                addFileToZip(String.format("statistics-%s-%s",  StatisticReport.DATE_FORMAT.format(start), StatisticReport.DATE_FORMAT.format(end)), lF, zos); 
            }
            
            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    private static void addFileToZip(String path, String srcFile, ZipOutputStream zipOut) throws IOException {
        File file = new File(srcFile);
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(path + "/" + file.getName());
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }


    
    public static String hashVal(String value) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            Base64.Encoder base64Encoder = Base64.getEncoder();
            md5.update(value.getBytes("UTF-8"));
            return base64Encoder.encodeToString(md5.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return value;
        }
    }

    
}


