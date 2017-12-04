package cz.incad.kramerius.processes.starter;

import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Logger;

public abstract class ProcessUpdatingChannel {


    public static final Logger LOGGER = Logger.getLogger(cz.incad.kramerius.processes.starter.ProcessUpdatingChannel.class.getName());

    public abstract boolean updateStatus(States state) throws IOException;

    public abstract boolean updatePID(String pid) throws IOException;

    public abstract boolean updateName(String name) throws IOException;


    public static ProcessUpdatingChannel getChannel() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String property = System.getProperty(ProcessUpdatingChannel.class.getName(), StandardProcessUpdatingChannel.class.getName());
        Class<?> aClass = Class.forName(property);
        return (ProcessUpdatingChannel) aClass.newInstance();
    }


    public static class FileProcessUpdatingChannel extends ProcessUpdatingChannel {
        @Override
        public boolean updateStatus(States state) throws IOException {
            File f = new File("status.txt");
            FileUtils.write(f, state.name(), "UTF-8");
            return true;
        }

        @Override
        public boolean updatePID(String pid) throws IOException {
            File f = new File("pid.txt");
            FileUtils.write(f, pid, "UTF-8");
            return true;
        }

        @Override
        public boolean updateName(String name) throws IOException {
            File f = new File("name.txt");
            FileUtils.write(f, name, "UTF-8");
            return true;
        }
    }

    public static class StandardProcessUpdatingChannel extends   ProcessUpdatingChannel {

        @Override
        public boolean updateStatus(States state) throws IOException {
            String uuid = System.getProperty(ProcessStarter.UUID_KEY);
            String lrURl = ProcessUtils.getLrServlet();
            String restURL = lrURl + "?action=updateStatus&uuid=" + uuid + "&state=" + state;
            ProcessUtils.httpGet(restURL);
            return true;
        }

        @Override
        public boolean updatePID(String pid) throws IOException {
            String uuid = System.getProperty(ProcessStarter.UUID_KEY);
            String lrURl = ProcessUtils.getLrServlet();

            String restURL = lrURl + "?action=updatePID&uuid=" + uuid + "&pid=" + pid;
            ProcessUtils.httpGet(restURL);
            return true;
        }

        @Override
        public boolean updateName(String name) throws IOException {
            String uuid = System.getProperty(ProcessStarter.UUID_KEY);
            String lrURl = ProcessUtils.getLrServlet();

            String restURL = lrURl + "?action=updateName&uuid=" + uuid + "&name=" + URLEncoder.encode(name, "UTF-8");
            LOGGER.info("requesting url :" + restURL);
            ProcessUtils.httpGet(restURL);
            return true;
        }




    }

    public static void main(String[] args) {
        String typeName = FileProcessUpdatingChannel.class.getTypeName();
        System.out.println(typeName);

    }
}
