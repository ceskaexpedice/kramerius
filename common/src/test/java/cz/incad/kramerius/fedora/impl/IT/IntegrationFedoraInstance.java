package cz.incad.kramerius.fedora.impl.IT;

import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.utils.IPAddressUtils;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pstastny on 10/13/2017.
 */
public class IntegrationFedoraInstance {

    // fedora should be downloadable
    public static final String MAIN_CLZ = "org.simplericity.jettyconsole.JettyConsoleBootstrapMainClass";
    public static final Logger LOGGER  = Logger.getLogger(IntegrationFedoraInstance.class.getName());


//    protected void stopMeOsDependent() {
//        try {
//            LOGGER.info("Killing process "+getPid());
//            // taskkill /PID  <pid>
//            List<String> command = new ArrayList<String>();
//            command.add("taskkill");
//            command.add("/f");
//            command.add("/PID");
//            command.add(getPid());
//            ProcessBuilder processBuilder = new ProcessBuilder(command);
//            processBuilder.start();
//        } catch (IOException e) {
//            LOGGER.log(Level.SEVERE, e.getMessage(), e);
//        }
//    }
//  protected void stopMeOsDependent() {
//    try {
//        LOGGER.fine("Killing process "+getPid());
//        // kill -9 <pid>
//        List<String> command = new ArrayList<String>();
//        command.add("kill");
//        command.add("-9");
//        command.add(getPid());
//        ProcessBuilder processBuilder = new ProcessBuilder(command);
//        Process startedProcess = processBuilder.start();
//        LOGGER.fine("killing command '"+command+"' and exit command "/*+startedProcess.exitValue()*/);
//
//    } catch (IOException e) {
//        LOGGER.log(Level.SEVERE, e.getMessage(), e);
//    }
//}



    public static String getPID() {
        String pid = null;
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String[] split = name.split("@");
        if ((split != null) && (split.length > 1)) {
            pid = split[0];
        }
        return pid;
    }

//    public void startMe(boolean wait, String krameriusAppLib,
//                        String... additionalJarFiles) {
//        try {
//            File processWorkingDir = processWorkingDirectory();
//
//            // "java -D"+ProcessStarter.MAIN_CLASS_KEY+"="+mainClass
//            // create command
//            List<String> command = new ArrayList<String>();
//            command.add("java");
//
//            List<String> javaProcessParameters = this.definition
//                    .getJavaProcessParameters();
//            for (String jpParam : javaProcessParameters) {
//                command.add(jpParam);
//            }
//
//            command.add("-D" + ProcessStarter.MAIN_CLASS_KEY + "="
//                    + this.definition.getMainClass());
//
//            command.add("-D" + IPAddressUtils.X_IP_FORWARD + "="
//                    + this.ipAddress);
//
//            command.add("-D" + ProcessStarter.UUID_KEY + "=" + this.uuid);
//            command.add("-D" + ProcessStarter.TOKEN_KEY + "="
//                    + this.getGroupToken());
//            command.add("-D" + ProcessStarter.AUTH_TOKEN_KEY + "="
//                    + this.getAuthToken());
//            command.add("-D" + ProcessStarter.SHOULD_CHECK_ERROR_STREAM + "="
//                    + this.definition.isCheckedErrorStream());
//
//            File standardStreamFile = standardOutFile(processWorkingDir);
//            File errStreamFile = errorOutFile(processWorkingDir);
//
//            command.add("-D" + ProcessStarter.SOUT_FILE + "="
//                    + standardStreamFile.getAbsolutePath());
//            command.add("-D" + ProcessStarter.SERR_FILE + "="
//                    + errStreamFile.getAbsolutePath());
//
//            Set<Object> keySet = this.parametersMapping.keySet();
//            for (Object key : keySet) {
//                command.add("-D" + key + "="
//                        + this.parametersMapping.getProperty(key.toString()));
//            }
//
//            command.add(ProcessStarter.class.getName());
//
//            List<String> params = this.definition.getParameters();
//            for (String par : params) {
//                command.add(par);
//            }
//
//            List<String> runtimeParams = this.getParameters();
//            for (String par : runtimeParams) {
//                command.add(par);
//            }
//
//            // create CLASSPATH
//            StringBuffer buffer = new StringBuffer();
//            String libsDirPath = this.definition.getLibsDir();
//            if (libsDirPath == null) {
//                libsDirPath = krameriusAppLib;
//            }
//
//            File libsDir = new File(libsDirPath);
//            File[] listFiles = libsDir.listFiles();
//            if (listFiles != null) {
//                for (File file : listFiles) {
//                    buffer.append(file.getAbsolutePath());
//                    buffer.append(File.pathSeparator);
//                }
//            }
//            // TODO: co delat pri zmene definice?
//            for (String string : additionalJarFiles) {
//                buffer.append(new File(string).getAbsolutePath());
//                buffer.append(File.pathSeparator);
//            }
//
//            ProcessBuilder processBuilder = new ProcessBuilder(command);
//            processBuilder = processBuilder.directory(processWorkingDir);
//
//            processBuilder.environment().put(ProcessStarter.CLASSPATH_NAME,
//                    buffer.toString());
//            this.setStartTime(System.currentTimeMillis());
//            this.state = States.RUNNING;
//
//            manager.updateLongRunningProcessState(this);
//
//            manager.updateLongRunningProcessStartedDate(this);
//
//            LOGGER.fine("" + command);
//            LOGGER.fine(buffer.toString());
//
//            Process process = processBuilder.start();
//
//            // pokracuje dal.. rozhoduje se, jestli pocka na vysledek procesu
//            if (wait) {
//                int val = process.waitFor();
//                LOGGER.info("return value exiting process '" + val + "'");
//            }
//
//        } catch (IOException e) {
//            LOGGER.log(Level.SEVERE, e.getMessage(), e);
//        } catch (InterruptedException e) {
//            LOGGER.log(Level.SEVERE, e.getMessage(), e);
//        }
//    }


    public static void main(String[] args) {
        String pid = getPID();
        System.out.println(pid );
    }
}
