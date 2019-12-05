package org.kramerius.replications.output;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.processes.BatchStates;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.template.ProcessOutputTemplate;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.templates.BundleTemplateUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.kramerius.replications.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;

public class DataMigrationOutputTemplate implements ProcessOutputTemplate {

    public static String PROCESS_OUTPUT_ID = "def";
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DataMigrationOutputTemplate.class.getName());
    @Inject
    ResourceBundleService resourceBundleService;

    @Inject
    Provider<Locale> localesProvider;


    @Override
    public void renderOutput(LRProcess lrProcess, LRProcessDefinition definition, Writer writer) throws IOException {
        JSONObject jsonObject = description(lrProcess);
        Properties props = lrProcess.getParametersMapping();

        String url = props.getProperty("url");

        DataMigrationOutputTemplate.OutputContext ctx = new DataMigrationOutputTemplate.OutputContext();
        ctx.setPid(K4ReplicationProcess.pidFrom(url));

        ctx.setType((jsonObject != null && (jsonObject.containsKey("type"))) ? jsonObject.getString("type") : "-");
        ctx.setHandle((jsonObject != null && (jsonObject.containsKey("handle"))) ? jsonObject.getString("handle") : "-");
        ctx.setLrProcess(lrProcess);
        ctx.setBundle(BundleTemplateUtils.resourceBundleMap(this.resourceBundleService.getResourceBundle("labels", localesProvider.get())));
        setErrorFlagAndMessage(lrProcess, ctx);

        try {
            //TODO: full validation
            new URL(ctx.getHandle());
            ctx.setValidHandlePresent(true);
        } catch (Exception e) {
            ctx.setValidHandlePresent(false);
        }


        InputStream iStream = this.getClass().getResourceAsStream("data-migration-output.stg");
        StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(iStream, "UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = templateGroup.getInstanceOf("outputs");
        template.setAttribute("context", ctx);

        writer.write(template.toString());

    }

    public String escapedJavascriptString(String input) {
        List<Character> escaping = new ArrayList<Character>();
        {
            escaping.add('\'');
            escaping.add('"');
            escaping.add('\n');
        }
        Map<Character, String> replacemets = new HashMap<Character, String>();
        {
            replacemets.put('\'', "\\'");
            replacemets.put('"', "\\\"");
            replacemets.put('\n', "\\n");
        }
        return escapeString(input, escaping, replacemets);
    }

    public String escapeString(String str, List<Character> escapeCharatectes, Map<Character, String> replacements) {
        StringBuilder builder = new StringBuilder();
        char[] array = str.toCharArray();
        for (char c : array) {
            Character cObj = new Character(c);
            if (escapeCharatectes.contains(cObj)) {
                String repl = replacements.get(cObj);
                builder.append(repl);
            } else {
                builder.append(cObj);
            }
        }
        return builder.toString();
    }

    public void setErrorFlagAndMessage(LRProcess lrProcess, DataMigrationOutputTemplate.OutputContext ctx) {
        ctx.setErrorOccured(lrProcess.getProcessState() != States.FINISHED && lrProcess.getBatchState() != BatchStates.BATCH_FINISHED);
        if (ctx.isErrorOccured()) {
            try {
                InputStream is = lrProcess.getErrorProcessOutputStream();
                String error = IOUtils.readAsString(is, Charset.forName(System.getProperty("file.encoding")), true);
                ctx.setFormatedErrorMessage(error);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    JSONObject description(LRProcess lrProcess) throws IOException, FileNotFoundException {
        File descriptionFile = new File(lrProcess.processWorkingDirectory(), AbstractPhase.DESCRIPTION_FILE);
        if ((descriptionFile != null) && (descriptionFile.canRead())) {
            String stringInput = IOUtils.readAsString(new FileInputStream(descriptionFile), Charset.forName("UTF-8"), true);
            try {
                return JSONObject.fromObject(stringInput);
            } catch (JSONException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return null;
            }
        } else return null;
    }

    @Override
    public void renderName(LRProcess lrProcess, LRProcessDefinition definition, Writer writer) throws IOException {
        ResourceBundle resbundle = this.resourceBundleService.getResourceBundle("labels", localesProvider.get());
        String localized = resbundle.getString("process.data_migration.outputs");
        writer.write(localized);
    }

    @Override
    public String getOutputTemplateId() {
        return PROCESS_OUTPUT_ID;
    }

    private String[] jsonToArray(JSONArray jsonArr) {
        String[] strArr = new String[jsonArr.size()];
        for (int i = 0; i < strArr.length; i++) {
            strArr[i] = jsonArr.getString(i);
        }
        return strArr;
    }

    static class _Filter implements FileFilter {

        private String fname;

        public _Filter(String fname) {
            super();
            this.fname = fname;
        }


        @Override
        public boolean accept(File pathname) {
            return pathname.getName().startsWith(this.fname);
        }

    }

    /**
     * Output template rendering context
     *
     * @author pavels
     */
    public static class OutputContext {

        private String pid;
        private String type;
        private String handle;

        private boolean errorOccured = false;
        private String formatedErrorMessage = null;

        private LRProcess lrProcess;
        private Map<String, String> bundle;
        private boolean validHandle;



        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPid() {
            return pid;
        }

        public void setPid(String pid) {
            this.pid = pid;
        }


        private boolean isFailedFile(File f, String phName) {
            return f != null && f.getName().startsWith(phName) && f.getName().endsWith("failed");
        }

        private boolean isCompletedFile(File f, String phName) {
            return f != null && f.getName().startsWith(phName) && f.getName().endsWith("completed");
        }


        public boolean isFirstPhaseFilePresent() {
            return phraseFile(IterateThroughIndexPhase.class.getName()) != null ;
        }

        public boolean isSecondPhaseFilePresent() {
            return phraseFile(SecondPhase.class.getName()) != null;
        }

        public boolean isThirdPhaseFilePresent() {
            return phraseFile(ThirdPhase.class.getName()) != null;
        }

        public File phraseFile(String  phrase) {
            if (new File(lrProcess.processWorkingDirectory(),phrase+".failed").exists()) {
                return new File( lrProcess.processWorkingDirectory(), phrase+".failed");
            } else if (new File(lrProcess.processWorkingDirectory(),phrase+".completed").exists()) {
                return new File(lrProcess.processWorkingDirectory(),phrase+".completed");
            } else return null;
        }


        public boolean isFirstPhaseFailed() {
            return isFailedFile(phraseFile(IterateThroughIndexPhase.class.getName()), IterateThroughIndexPhase.class.getName());
        }

        public boolean isSecondPhaseFailed() {
            return isFailedFile(phraseFile(SecondPhase.class.getName()), SecondPhase.class.getName());
        }

        public boolean isThirdPhaseFailed() {
            return isFailedFile(phraseFile(StartIndexerPhase.class.getName()), StartIndexerPhase.class.getName());
        }

        public boolean isFirstPhaseCompleted() {
            return isCompletedFile(phraseFile(IterateThroughIndexPhase.class.getName()), FirstPhase.class.getName());
        }

        public boolean isSecondPhaseCompleted() {
            return isCompletedFile(phraseFile(SecondPhase.class.getName()), SecondPhase.class.getName());
        }


        public boolean isThirdPhaseCompleted() {
            return isCompletedFile(phraseFile(StartIndexerPhase.class.getName()), ThirdPhase.class.getName());
        }

        public boolean isRestartButtonEnabled() {
            return this.isErrorOccured() && this.isValidHandlePresent();
        }

        public boolean isValidHandlePresent() {
            return this.validHandle;
        }

        public void setValidHandlePresent(boolean flag) {
            this.validHandle = flag;
        }

        public boolean isErrorOccured() {
            return errorOccured;
        }

        public void setErrorOccured(boolean errorOccured) {
            this.errorOccured = errorOccured;
        }


        public String getFormatedErrorMessage() {
            return formatedErrorMessage;
        }

        public void setFormatedErrorMessage(String formatedErrorMessage) {
            this.formatedErrorMessage = formatedErrorMessage;
        }



        /**
         * @return the handle
         */
        public String getHandle() {
            return handle;
        }

        /**
         * @param handle the handle to set
         */
        public void setHandle(String handle) {
            this.handle = handle;
        }


        public LRProcess getLrProcess() {
            return lrProcess;
        }

        public void setLrProcess(LRProcess lrProcess) {
            this.lrProcess = lrProcess;
        }

        /**
         * @return the bundle
         */
        public Map<String, String> getBundle() {
            return bundle;
        }

        /**
         * @param bundle the bundle to set
         */
        public void setBundle(Map<String, String> bundle) {
            this.bundle = bundle;
        }

    }

}
