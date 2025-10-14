package cz.incad.kramerius.statistics.impl.nkp;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.gdpr.AnonymizationSupport;
import cz.incad.kramerius.service.Mailer;
import cz.incad.kramerius.service.impl.MailerImpl;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;
import org.json.JSONObject;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class NKPLogProcess {

    private static final String NKP_LOGS_FOLDER_KEY = "nkp.logs.folder";
    private static final String NKP_LOGS_VISIBILITY_KEY = "nkp.logs.visibility";
    private static final String NKP_LOGS_INSTITUTION_KEY = "nkp.logs.institution";
    private static final String NKP_LOGS_ANONYMIZATION_KEY = "nkp.logs.anonymization";
    
    private static final String NKP_LOGS_EMAIL_NOTIFICATION = "nkp.logs.notification.enabled";
    private static final String NKP_LOGS_EMAIL_FROM = "nkp.logs.notification.from";
    private static final String NKP_LOGS_EMAIL_TEXT = "nkp.logs.notification.text";
    private static final String NKP_LOGS_EMAIL_SUBJECT = "nkp.logs.notification.subject";
    private static final String NKP_LOGS_EMAIL_RECIPIENTS = "nkp.logs.notification.recipients";
    
    
    public static Logger LOGGER = Logger.getLogger(NKPLogProcess.class.getName());

    public static void main(String[] args) throws NoSuchAlgorithmException, ParseException, IOException, MessagingException {
        LOGGER.log(Level.INFO, "Process parameters: " + Arrays.asList(args).toString());
        if (args.length > 2) {

            String from = args[1];
            String to = args[2];

            String defaultInst = KConfiguration.getInstance().getConfiguration().getString("acronym");
            String folder = KConfiguration.getInstance().getConfiguration().getString(NKP_LOGS_FOLDER_KEY, System.getProperty("java.io.tmpdir"));
            String visibility = KConfiguration.getInstance().getConfiguration().getString(NKP_LOGS_VISIBILITY_KEY, "ALL");
            String institution = KConfiguration.getInstance().getConfiguration().getString(NKP_LOGS_INSTITUTION_KEY, StringUtils.isAnyString(defaultInst) ? defaultInst :  "-none-");
            List<Object> anonymization = getAnnonymizedKeys();

            process(from, to, folder, institution, visibility, anonymization);

            boolean  emailNotification = KConfiguration.getInstance().getConfiguration().getBoolean(NKP_LOGS_EMAIL_NOTIFICATION,  false);
            if (args.length > 3) { emailNotification = Boolean.parseBoolean(args[3]); }
            
            if (emailNotification) {
                String administratorEmail = KConfiguration.getInstance().getConfiguration().getString("administrator.email");
                String emailFrom =   KConfiguration.getInstance().getConfiguration().getString(NKP_LOGS_EMAIL_FROM,  administratorEmail);
                String text  = KConfiguration.getInstance().getConfiguration().getString(NKP_LOGS_EMAIL_TEXT,  "NKP Logs notification");
                String subject  = KConfiguration.getInstance().getConfiguration().getString(NKP_LOGS_EMAIL_SUBJECT,  "NKP Logs notification");
                List<Object> recipients  = KConfiguration.getInstance().getConfiguration().getList(NKP_LOGS_EMAIL_RECIPIENTS,  new ArrayList<>());
                if (recipients != null && recipients.size() > 0 && 
                        StringUtils.isAnyString(emailFrom) && 
                        StringUtils.isAnyString(text)  && 
                        StringUtils.isAnyString(subject)) {
                    
                    sendEmailNotification(emailFrom, recipients, subject, text);
                } else {
                    LOGGER.warning("Warning: Recipients missing, unable to send email");
                }
            }
        }
    }
    
    //TODO: Do it better - change configuration key
    public static List<Object> getAnnonymizedKeys() {
        List<Object> anonymization = KConfiguration.getInstance().getConfiguration().getList(NKP_LOGS_ANONYMIZATION_KEY, 
                AnonymizationSupport.DEFAULT_ANONYMIZATION_PROPERTIES);
        return anonymization;
    }

    public static void sendEmailNotification(String emailFrom, List<Object> recipients, String subject, String text) throws MessagingException {
        Mailer mailer= new MailerImpl();
        javax.mail.Session sess = mailer.getSession(null, null);
        MimeMessage msg = new MimeMessage(sess);
        
        msg.setHeader("Content-Type", "text/plain; charset=UTF-8");
        msg.setFrom(new InternetAddress(emailFrom));
        for (Object recp : recipients) {
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recp.toString()));
        }
        msg.setSubject(subject, "UTF-8");
        msg.setText(text,"UTF-8");
        Transport.send(msg);
    }

    @ProcessMethod
    public static void process(@ParameterName("from") String from,
                               @ParameterName("to") String to,
                               @ParameterName("folder") String folder,
                               @ParameterName("institution") String institution,
                               @ParameterName("visibility") String visibility,
                               List<Object> anonymization
                              
    ) throws ParseException, IOException, NoSuchAlgorithmException {
        List<String> logs = new ArrayList<>();
        PluginContext pluginContext = PluginContextHolder.getContext();
        pluginContext.updateProcessName(String.format("Generování NKP logů pro období %s - %s", from, to));
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
                        JSONObject lineJSONObject = AnonymizationSupport.annonymizeObject(annonymizationKeys, line);

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
    
}


