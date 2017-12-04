package cz.incad.kramerius.fedora.IT;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.impl.AbstractLRProcessImpl;
import cz.incad.kramerius.processes.impl.LRProcessDefinitionImpl;
import cz.incad.kramerius.processes.utils.PIDList;
import cz.incad.kramerius.security.database.TypeOfOrdering;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.database.Offset;
import cz.incad.kramerius.utils.database.SQLFilter;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;

public class Fedora4IntegretionTests {

    public static final String SOURCE = "" +
            "<processes>" +
            "   <process>\n" +
            "        <id>fedora4</id>\n" +
            "        <description>Fedora 4 start</description>\n" +
            "        <mainClass>org.simplericity.jettyconsole.JettyConsoleBootstrapMainClass2</mainClass>\n" +
            "        <standardOs>lrOut</standardOs>\n" +
            "        <errOs>lrErr</errOs>\n" +
            "        <securedaction>aggregate</securedaction>\n" +
            "        <parameters>--port 18080 --headless</parameters>\n" +
            "        <javaProcessParameters>-Xmx3072m -Xms512m -Dcz.incad.kramerius.processes.starter.ProcessUpdatingChannel=cz.incad.kramerius.processes.starter.ProcessUpdatingChannel$FileProcessUpdatingChannel</javaProcessParameters>\n" +
            "    </process>\n" +
            "</processes>\n" +
            "\n";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        FileUtils.deleteQuietly(new File("/home/pavels/tmp/repo-2/pid.txt"));
        FileUtils.deleteQuietly(new File("/home/pavels/tmp/repo-2/status.txt"));
    }

    @Test
    public void testStartFedora() throws IOException, SAXException, ParserConfigurationException, InterruptedException {
        Fedora4ProcessManager pm = new Fedora4ProcessManager();
        LRProcessDefinitionImpl fedora4ProcessDefinition = new LRProcessDefinitionImpl(pm, null);
        Document document = XMLUtils.parseDocument(new StringReader(SOURCE));
        Element processElm = XMLUtils.findElement(document.getDocumentElement(), "process");
        fedora4ProcessDefinition.loadFromXml(processElm);
        LRProcess process = fedora4ProcessDefinition.createNewProcess(null, null);
        String s = LRProcess.class.getName() + ".workingdir";
        System.setProperty(s,"/home/pavels/tmp/repo-2");
        process.startMe(true, "/home/pavels/tmp/repo-2");

        Thread.sleep(20000);

        /*
        PIDList pidList = PIDList.createPIDList();
        List<String> processesPIDS = pidList.getProcessesPIDS();
        */
    }

    @Test
    @Ignore
    public void testStopFedora() throws IOException, SAXException, ParserConfigurationException {
        Fedora4ProcessManager pm = new Fedora4ProcessManager();
        LRProcessDefinitionImpl fedora4ProcessDefinition = new LRProcessDefinitionImpl(pm, null);
        Document document = XMLUtils.parseDocument(new StringReader(SOURCE));
        Element processElm = XMLUtils.findElement(document.getDocumentElement(), "process");
        fedora4ProcessDefinition.loadFromXml(processElm);
        LRProcess process = fedora4ProcessDefinition.createNewProcess(null, null);
        String s = LRProcess.class.getName() + ".workingdir";
        System.setProperty(s,"/home/pavels/tmp/repo-2");
        process.setPid(FileUtils.readFileToString(new java.io.File("/home/pavels/tmp/repo-2/pid.txt"), "UTF-8"));
        process.stopMe();

    }

    @AfterClass
    public static void tearDownAfterClass() {

    }


    private class Fedora4ProcessManager implements LRProcessManager {

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
