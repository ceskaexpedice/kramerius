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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

import net.sf.json.JSONArray;
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
        ctx.setDate(jsonObject.getString("date"));
        ctx.setTitle(jsonObject.getString("title"));
        ctx.setType(jsonObject.getString("type"));
        ctx.setIdentifiers(jsonToArray(jsonObject.getJSONArray("identifiers")));
        ctx.setPublishers(jsonToArray(jsonObject.getJSONArray("publishers")));
        ctx.setCreators(jsonToArray(jsonObject.getJSONArray("creators")));
        setPhasesFlags(ctx, lrProcess.processWorkingDirectory());
        setErrorFlagAndMessage(lrProcess, ctx);
        
        InputStream iStream = this.getClass().getResourceAsStream("replicationtemplate.st");
        StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = templateGroup.getInstanceOf("outputs");
        template.setAttribute("context", ctx);
        
        writer.write(template.toString());
        
    }

    public void setErrorFlagAndMessage(LRProcess lrProcess, OutputContext ctx) {
        ctx.setErrorOccured(lrProcess.getProcessState() == States.FINISHED && lrProcess.getBatchState() == BatchStates.BATCH_FINISHED);
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

    private void setPhasesFlags(OutputContext ctx, File processWorkingDirectory) {
        ctx.setFirstPhaseFailed(! new File(processWorkingDirectory,FirstPhase.class.getName()+".completed").exists());
        ctx.setSecondPhaseFailed(! new File(processWorkingDirectory,SecondPhase.class.getName()+".completed").exists());
        ctx.setThirdPhaseFailed(! new File(processWorkingDirectory,ThirdPhase.class.getName()+".completed").exists());
    }

    public JSONObject description(LRProcess lrProcess) throws IOException, FileNotFoundException {
        File descriptionFile = new File(lrProcess.processWorkingDirectory(),AbstractPhase.DESCRIPTION_FILE);
        String stringInput = IOUtils.readAsString(new FileInputStream(descriptionFile), Charset.forName("UTF-8"), true);
        return JSONObject.fromObject(stringInput);
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
        private String type;
        private String[] identifiers;
        private String[] publishers;
        private String[] creators;

        private boolean firstPhaseFailed = false;
        private boolean secondPhaseFailed = false;
        private boolean thirdPhaseFailed = false;
        private boolean errorOccured = false;

        private String formatedErrorMessage = null;
        
        
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

        public boolean isFirstPhaseFailed() {
            return this.firstPhaseFailed;
        }
        
        public boolean isSecondPhaseFailed() {
            return this.secondPhaseFailed;
        }

        public boolean isThirdPhaseFailed() {
            return this.thirdPhaseFailed;
        }
        
        public void setFirstPhaseFailed(boolean firstPhaseFailed) {
            this.firstPhaseFailed = firstPhaseFailed;
        }

        public void setSecondPhaseFailed(boolean secondPhaseFailed) {
            this.secondPhaseFailed = secondPhaseFailed;
        }

        public void setThirdPhaseFailed(boolean thirdPhaseFailed) {
            this.thirdPhaseFailed = thirdPhaseFailed;
        }

        public boolean isRestartButtonEnabled() {
            return this.secondPhaseFailed || this.thirdPhaseFailed;
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

        @Override
        public String toString() {
            return "OutputContext [pid=" + pid + ", date=" + date + ", title=" + title + ", type=" + type + ", identifiers=" + Arrays.toString(identifiers) + ", publishers=" + Arrays.toString(publishers) + ", creators=" + Arrays.toString(creators) + "]";
        }
    }

}

