/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.kramerius.replications.outputs;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.kramerius.replications.AbstractPhase;
import org.kramerius.replications.FirstPhase;
import org.kramerius.replications.K4ReplicationProcess;
import org.kramerius.replications.SecondPhase;
import org.kramerius.replications.ThirdPhase;

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

/**
 * K4 replication output template
 * @author pavels
 */
public class OutputTemplate implements ProcessOutputTemplate {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(OutputTemplate.class.getName());
    
    public static String PROCESS_OUTPUT_ID="def";

    @Inject
    ResourceBundleService resourceBundleService;
    
    @Inject
    Provider<Locale> localesProvider;
    
    
    @Override
    public void renderOutput(LRProcess lrProcess, LRProcessDefinition definition, Writer writer) throws IOException {
        JSONObject jsonObject = description(lrProcess);
        Properties props = lrProcess.getParametersMapping();

        String url = props.getProperty("url");

        OutputContext ctx = new OutputContext(); 
        ctx.setPid(K4ReplicationProcess.pidFrom(url));
        ctx.setDate((jsonObject != null && (jsonObject.containsKey("date")))? jsonObject.getString("date"): "-");
        ctx.setTitle((jsonObject != null && (jsonObject.containsKey("title"))) ? jsonObject.getString("title"): "-");
        ctx.setEscapedTitle((jsonObject != null && (jsonObject.containsKey("title"))) ? escapedJavascriptString(jsonObject.getString("title")): "-");

        ctx.setType( (jsonObject != null && (jsonObject.containsKey("type"))) ? jsonObject.getString("type"): "-");
        ctx.setHandle((jsonObject != null && (jsonObject.containsKey("handle"))) ? jsonObject.getString("handle"): "-");
        ctx.setIdentifiers((jsonObject != null && (jsonObject.containsKey("identifiers"))) ?  jsonToArray(jsonObject.getJSONArray("identifiers")): new String[0]);
        ctx.setPublishers((jsonObject != null &&  (jsonObject.containsKey("publishers"))) ? jsonToArray(jsonObject.getJSONArray("publishers")): new String[0]);
        ctx.setCreators((jsonObject != null && (jsonObject.containsKey("creators"))) ? jsonToArray(jsonObject.getJSONArray("creators")): new String[0]);
        ctx.setLrProcess(lrProcess);
        ctx.setBundle(BundleTemplateUtils.resourceBundleMap(this.resourceBundleService.getResourceBundle("labels", localesProvider.get())));
        setPhasesFlags(ctx, lrProcess.processWorkingDirectory());
        setErrorFlagAndMessage(lrProcess, ctx);

        try {
            //TODO: full validation
            new URL(ctx.getHandle());
            ctx.setValidHandlePresent(true);
        } catch (Exception e) {
            ctx.setValidHandlePresent(false);
        }
        
        
        InputStream iStream = this.getClass().getResourceAsStream("replicationtemplate.st");
        StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = templateGroup.getInstanceOf("outputs");
        template.setAttribute("context", ctx);
        
        writer.write(template.toString());
        
    }

    public String escapedJavascriptString(String input) {
        List<Character> escaping = new ArrayList<Character>(); {
            escaping.add('\'');escaping.add('"');escaping.add('\n');
        }
        Map<Character, String> replacemets = new HashMap<Character, String>(); {
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
    
    public void setErrorFlagAndMessage(LRProcess lrProcess, OutputContext ctx) {
        ctx.setErrorOccured(lrProcess.getProcessState() != States.FINISHED && lrProcess.getBatchState() != BatchStates.BATCH_FINISHED);
        if (ctx.isErrorOccured()) {
            try {
                InputStream is = lrProcess.getErrorProcessOutputStream();
                String error = IOUtils.readAsString(is, Charset.forName(System.getProperty("file.encoding")), true);
                ctx.setFormatedErrorMessage(error);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }
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
    
    private void setPhasesFlags(OutputContext ctx, File processWorkingDirectory) {
        File[] firstFiles = processWorkingDirectory.listFiles(new _Filter(FirstPhase.class.getName()));
        if ((firstFiles != null) && (firstFiles.length > 0)) {
            ctx.setFirstPhaseFile(firstFiles[0]);
        }
        
        File[] secondFiles = processWorkingDirectory.listFiles(new _Filter(SecondPhase.class.getName()));
        if ((secondFiles != null) && (secondFiles.length > 0)) {
            ctx.setSecondPhaseFile(secondFiles[0]);
        }

        File[] thirdFiles = processWorkingDirectory.listFiles(new _Filter(ThirdPhase.class.getName()));
        if ((thirdFiles != null) && (thirdFiles.length > 0)) {
            ctx.setThirdPhaseFile(thirdFiles[0]);
        }
    }

    JSONObject description(LRProcess lrProcess) throws IOException, FileNotFoundException {
        File descriptionFile = new File(lrProcess.processWorkingDirectory(),AbstractPhase.DESCRIPTION_FILE);
        if ((descriptionFile != null) && (descriptionFile.canRead())) {
            String stringInput = IOUtils.readAsString(new FileInputStream(descriptionFile), Charset.forName("UTF-8"), true);
            try {
                return JSONObject.fromObject(stringInput);
            } catch (JSONException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                return null;
            }
        } else return null;
    }

    @Override
    public void renderName(LRProcess lrProcess, LRProcessDefinition definition, Writer writer) throws IOException {
        ResourceBundle resbundle = this.resourceBundleService.getResourceBundle("labels", localesProvider.get());
        String localized = resbundle.getString("process.k4_replications.outputs");
        writer.write(localized);
    }

    @Override
    public String getOutputTemplateId() {
        return PROCESS_OUTPUT_ID;
    }

    private String[] jsonToArray(JSONArray jsonArr) {
        String[] strArr = new String[jsonArr.size()];
        for (int i = 0; i < strArr.length; i++) {
            strArr[i]= jsonArr.getString(i);
        }
        return strArr;
    }

    
    
    /**
     * Output template rendering context
     * @author pavels
     */
    public static class OutputContext {
        
        private String pid;
        private String date;
        private String title;
        private String escapedTitle;
        private String type;
        private String handle;
        private String[] identifiers;
        private String[] publishers;
        private String[] creators;

        private File firstPhaseFile = null;
        private File secondPhaseFile = null;
        private File thirdPhaseFile = null;
        
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
        
        public String[] getIdentifiers() {
            return identifiers;
        }

        public void setIdentifiers(String[] identifiers) {
            this.identifiers = identifiers;
        }
        
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
        
        public String[] getPublishers() {
            return publishers;
        }
        
        public void setPublishers(String[] publishers) {
            this.publishers = publishers;
        }
        
        public String[] getCreators() {
            return creators;
        }
        
        public void setCreators(String[] creators) {
            this.creators = creators;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }

        
        
        public String getEscapedTitle() {
            return escapedTitle;
        }

        public void setEscapedTitle(String escapedTitle) {
            this.escapedTitle = escapedTitle;
        }

        private boolean isFiledFile(File f, String phName) {
            return f != null && f.getName().startsWith(phName) && f.getName().endsWith("failed");
        }
        
        private boolean isCompletedFile(File f, String phName) {
            return f != null && f.getName().startsWith(phName) && f.getName().endsWith("completed");
        }
        
        public boolean isFirstPhaseFilePresent() {
            return this.firstPhaseFile != null;
        }

        public boolean isSecondPhaseFilePresent() {
            return this.secondPhaseFile != null;
        }

        public boolean isThirdPhaseFilePresent() {
            return this.thirdPhaseFile != null;
        }

        public boolean isFirstPhaseFailed() {
            return isFiledFile(this.firstPhaseFile, FirstPhase.class.getName());
        }
        
        public boolean isSecondPhaseFailed() {
            return isFiledFile(this.secondPhaseFile, SecondPhase.class.getName());
        }

        public boolean isThirdPhaseFailed() {
            return isFiledFile(thirdPhaseFile, ThirdPhase.class.getName());
        }
        
        public boolean isFirstPhaseCompleted() {
            return isCompletedFile(this.firstPhaseFile, FirstPhase.class.getName());
        }

        public boolean isSecondPhaseCompleted() {
            return isCompletedFile(this.secondPhaseFile, SecondPhase.class.getName());
        }
        

        public boolean isThirdPhaseCompleted() {
            return isCompletedFile(this.thirdPhaseFile, ThirdPhase.class.getName());
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

        
        
        public File getFirstPhaseFile() {
            return firstPhaseFile;
        }

        public void setFirstPhaseFile(File firstPhaseFile) {
            this.firstPhaseFile = firstPhaseFile;
        }

        public File getSecondPhaseFile() {
            return secondPhaseFile;
        }

        public void setSecondPhaseFile(File secondPhaseFile) {
            this.secondPhaseFile = secondPhaseFile;
        }

        public File getThirdPhaseFile() {
            return thirdPhaseFile;
        }

        public void setThirdPhaseFile(File thirdPhaseFile) {
            this.thirdPhaseFile = thirdPhaseFile;
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
        
        @Override
        public String toString() {
            return "OutputContext [pid=" + pid + ", date=" + date + ", title=" + title + ", type=" + type + ", identifiers=" + Arrays.toString(identifiers) + ", publishers=" + Arrays.toString(publishers) + ", creators=" + Arrays.toString(creators) + "]";
        }
    }

}

