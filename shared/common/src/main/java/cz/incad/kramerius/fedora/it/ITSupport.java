package cz.incad.kramerius.fedora.it;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.impl.LRProcessDefinitionImpl;
import cz.incad.kramerius.processes.utils.PIDList;
import cz.incad.kramerius.security.database.TypeOfOrdering;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.Offset;
import cz.incad.kramerius.utils.database.SQLFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

public class ITSupport {

    public static final Logger LOGGER = Logger.getLogger(ITSupport.class.getName());

    public static final String SOURCE = "" +
            "<processes>" +
            "   <process>\n" +
            "        <id>fedora4</id>\n" +
            "        <description>Fedora 4 start</description>\n" +
            "        <mainClass>org.simplericity.jettyconsole.JettyConsoleBootstrapMainClass</mainClass>\n" +
            "        <standardOs>lrOut</standardOs>\n" +
            "        <errOs>lrErr</errOs>\n" +
            "        <securedaction>aggregate</securedaction>\n" +
            "        <parameters>--port 18080 --headless</parameters>\n" +
            "        <javaProcessParameters>-Xmx1024m -Xms512m -Dcz.incad.kramerius.processes.starter.ProcessUpdatingChannel=cz.incad.kramerius.processes.starter.ProcessUpdatingChannel$FileProcessUpdatingChannel</javaProcessParameters>\n" +
            "    </process>\n" +
            "</processes>\n" +
            "\n";

    public static enum Commands {

        CHECK {
            @Override
            public void command() throws IOException, SAXException, ParserConfigurationException, InterruptedException {
                String s = LRProcess.class.getName() + ".workingdir";
                File itfolder = new File(Constants.WORKING_DIR,"it");
                System.setProperty(s, itfolder.getAbsolutePath());
                PIDList pidList = PIDList.createPIDList();
                List<String> processesPIDS = pidList.getProcessesPIDS();
                File file = new File(itfolder, "pid.txt");
                if (file.exists()) {
                    final String expectedPid = FileUtils.readFileToString(new File(itfolder, "pid.txt"), "UTF-8");
                    if (!processesPIDS.contains(expectedPid.trim())) {
                        throw new IOException("process not started");
                    }
                    LOGGER.info("OK ");
                } else throw new IOException("pid is not exist");
            }
        },

        CONTROL {
            @Override
            public void command() throws IOException, SAXException, ParserConfigurationException, InterruptedException {
                String s = LRProcess.class.getName() + ".workingdir";
                File itfolder = new File(Constants.WORKING_DIR,"it");
                System.setProperty(s, itfolder.getAbsolutePath());
                PIDList pidList = PIDList.createPIDList();
                List<String> processesPIDS = pidList.getProcessesPIDS();
                File file = new File(itfolder, "pid.txt");
                if (file.exists()) {
                    final String expectedPid = FileUtils.readFileToString(new File(itfolder, "pid.txt"), "UTF-8");
                    if (!processesPIDS.contains(expectedPid.trim())) {
                        START.command();
                    }
                } else {
                    START.command();
                }
            }
        },
        START {

            int THREADSHOLD = 5;

            @Override
            public void command() throws IOException, ParserConfigurationException, SAXException, InterruptedException {
                LOGGER.info("Starting fedora");
                Fedora4ProcessManager pm = new Fedora4ProcessManager();
                LRProcessDefinitionImpl fedora4ProcessDefinition = new LRProcessDefinitionImpl(pm, null);
                Document document = XMLUtils.parseDocument(new StringReader(SOURCE));
                Element processElm = XMLUtils.findElement(document.getDocumentElement(), "process");
                fedora4ProcessDefinition.loadFromXml(processElm);
                LRProcess process = fedora4ProcessDefinition.createNewProcess(null, null);
                String s = LRProcess.class.getName() + ".workingdir";
                File itfolder = new File(Constants.WORKING_DIR,"it");
                System.setProperty(s, itfolder.getAbsolutePath());
                process.startMe(false, itfolder.getAbsolutePath());


                // waiting while it is up
                for (int i=0;i<THREADSHOLD;i++) {
                    if (process.getProcessOutputFile().exists()) {
                        InputStream stream = process.getStandardProcessOutputStream();
                        if (stream != null) {
                            String content = IOUtils.toString(stream, "UTF-8");
                            System.out.println(content);
                            if (content.contains("cz.incad.kramerius.processes.starter.ProcessStarter main")) {
                                check();
                                return;
                            }
                        }
                    }
                    LOGGER.info("Waiting 10s ");
                    Thread.sleep(10000);
                }
            }

            private void check() throws IOException {
                URL url = new URL("http://localhost:18080/rest");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(10000);
                int responseCode = urlConnection.getResponseCode();
                LOGGER.info("Response code "+responseCode);
            }
        },



        STOP {
            @Override
            public void command() throws IOException, SAXException, ParserConfigurationException {
                LOGGER.info("Stoping fedora");

                Fedora4ProcessManager pm = new Fedora4ProcessManager();
                LRProcessDefinitionImpl fedora4ProcessDefinition = new LRProcessDefinitionImpl(pm, null);
                Document document = XMLUtils.parseDocument(new StringReader(SOURCE));
                Element processElm = XMLUtils.findElement(document.getDocumentElement(), "process");
                fedora4ProcessDefinition.loadFromXml(processElm);
                LRProcess process = fedora4ProcessDefinition.createNewProcess(null, null);
                String s = LRProcess.class.getName() + ".workingdir";
                File itfolder = new File(Constants.WORKING_DIR,"it");

                if (new File(itfolder,"pid.txt").exists()) {
                    process.setPid(FileUtils.readFileToString(new File(itfolder,"pid.txt"), "UTF-8"));
                    process.stopMe();
                } else {
                    LOGGER.warning("Cound not find pid.txt file");
                }

                FileUtils.deleteQuietly(new File(itfolder,"lrOut"));
                FileUtils.deleteQuietly(new File(itfolder,"lrErr"));
                FileUtils.deleteQuietly(new File(itfolder,"pid.txt"));
                FileUtils.deleteQuietly(new File(itfolder,"status.txt"));

            }
        };

        public abstract  void command() throws IOException, SAXException, ParserConfigurationException, InterruptedException;
    }

    public void start() throws IOException, SAXException, ParserConfigurationException { }

    public static void main(String[] args) throws InterruptedException, SAXException, ParserConfigurationException, IOException {
        if (args.length == 1) {
            Commands.valueOf(args[0]).command();
        }
    }


    private static class Fedora4ProcessManager implements LRProcessManager {

        private LRProcess process;

        @Override
        public void registerLongRunningProcess(LRProcess lp, String sessionKey, Properties parametersMapping) {

        }

        @Override
        public LRProcess getLongRunningProcess(String uuid) {
            return null;
        }

        @Override
        public List<LRProcess> getLongRunningProcesses() {
            return null;
        }

        @Override
        public List<LRProcess> getPlannedProcess(int howMany) {
            return null;
        }

        @Override
        public List<LRProcess> getLongRunningProcessesAsGrouped(LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering, Offset offset, SQLFilter filter) {
            return null;
        }

        @Override
        public List<LRProcess> getLongRunningProcessesAsFlat(LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering, Offset offset) {
            return null;
        }

        @Override
        public List<LRProcess> getLongRunningProcesses(States state) {
            return null;
        }

        @Override
        public List<LRProcess> getLongRunningProcessesByGroupToken(String grpToken) {
            return null;
        }

        @Override
        public int getNumberOfLongRunningProcesses(SQLFilter filter) {
            return 0;
        }

        @Override
        public void updateLongRunningProcessState(LRProcess lrProcess) {

        }

        @Override
        public void updateLongRunninngProcessBatchState(LRProcess lrProcess) {

        }

        @Override
        public void updateLongRunningProcessName(LRProcess lrProcess) {

        }

        @Override
        public void updateLongRunningProcessPID(LRProcess lrProcess) {

        }

        @Override
        public void updateLongRunningProcessStartedDate(LRProcess lrProcess) {

        }

        @Override
        public void updateLongRunningProcessFinishedDate(LRProcess lrProcess) {

        }

        @Override
        public void updateAuthTokenMapping(LRProcess lrProcess, String sessionKey) {

        }

        @Override
        public String getSessionKey(String authToken) {
            return null;
        }

        @Override
        public boolean isSessionKeyAssociatedWithProcess(String sessionKey) {
            return false;
        }

        @Override
        public void closeAuthToken(String authToken) {

        }

        @Override
        public boolean isAuthTokenClosed(String authToken) {
            return false;
        }

        @Override
        public void deleteLongRunningProcess(LRProcess lrProcess) {

        }

        @Override
        public void deleteBatchLongRunningProcess(LRProcess longRunningProcess) {

        }

        @Override
        public Properties loadParametersMapping(LRProcess lrProcess) {
            return null;
        }

        @Override
        public Lock getSynchronizingLock() {
            return null;
        }
    }

}
