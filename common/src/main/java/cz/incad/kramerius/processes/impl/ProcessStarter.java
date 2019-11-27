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
package cz.incad.kramerius.processes.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.WarningException;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.logging.LoggingLoader;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.utils.IPAddressUtils;

/**
 * Process starting point 
 * @author pavels
 */
public class ProcessStarter {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProcessStarter.class.getName());

    public static final String LOGGING_FILE_PROPERTY = "java.util.logging.config.file";
    public static final String LOGGING_CLASS_PROPERTY = "java.util.logging.config.class";
    
    
    public static final String MAIN_CLASS_KEY = "mainClass";
    public static final String UUID_KEY = "uuid";
    public static final String TOKEN_KEY = "token";
    public static final String AUTH_TOKEN_KEY="authToken";
    
    public static final String AUTOMATIC_CLOSE_TOKEN="shouldCloseToken";
    public static final String CLASSPATH_NAME = "CLASSPATH";

    public static final String SHOULD_CHECK_ERROR_STREAM="shouldCheckErrorStream";
    
    
    
    public static final String SOUT_FILE = "SOUT";
    public static final String SERR_FILE = "SERR";

    private static boolean STATUS_UPDATED = false;
    
    public static void main(String[] args)  {
        PrintStream outStream = null;
        PrintStream errStream = null;
        try {

            String mainClass = System.getProperty(MAIN_CLASS_KEY);
            //String forwardIP = System.getProperty(IPAddressUtils.X_IP_FORWARD);
            
            outStream = createPrintStream(System.getProperty(SOUT_FILE));
            errStream = createPrintStream(System.getProperty(SERR_FILE));
            System.setErr(errStream);
            System.setOut(outStream);
            
            setDefaultLoggingIfNecessary();

            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    try {
                        if (!STATUS_UPDATED) {
                            updateStatus(States.KILLED);
                        }
                    } catch (MalformedURLException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            });
            
            Class<?> clz = Class.forName(mainClass);
            
            String pid = getPID();
            updatePID(pid);
            

            MethodType processMethod = annotatedMethodType(clz);
            if (processMethod == null) processMethod = mainMethodType(clz);

            if (processMethod.getType() == MethodType.Type.ANNOTATED) {
                Object[] params = map(processMethod.getMethod(), args, System.getProperties());
                processMethod.getMethod().invoke(null, params);
            } else {
                Object[] objs = new Object[1];
                objs[0] = args;
                processMethod.getMethod().invoke(null, objs);
            }
            
            checkErrorFile();
            updateStatus(States.FINISHED);
        }catch(WarningException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            try {
                updateStatus(States.WARNING);
            } catch(IOException ex) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (Exception e1) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (errStream != null) {
                try {
                    errStream.close();
                } catch (Exception e1) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        	
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            try {
                updateStatus(States.FAILED);
            } catch(IOException ex) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (Exception e1) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (errStream != null) {
                try {
                    errStream.close();
                } catch (Exception e1) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        } finally {        
            String uuid = System.getProperty(ProcessStarter.UUID_KEY);
            String closeTokenFlag = System.getProperty(AUTOMATIC_CLOSE_TOKEN,"true");
            if (closeTokenFlag != null && closeTokenFlag.trim().toLowerCase().equals("true")) {
                ProcessUtils.closeToken(uuid);
            }
        }
    }

    private static void checkErrorFile() {
    	if (Boolean.getBoolean(ProcessStarter.SHOULD_CHECK_ERROR_STREAM)) {
        	String serrFileName = System.getProperty(SERR_FILE);
        	File serrFile = new File(serrFileName);
        	if (serrFile.length() > 0) throw new WarningException("system error file contains errors");
    	}
    }

	private static void setDefaultLoggingIfNecessary() {
        String classProperty = System.getProperty(LOGGING_CLASS_PROPERTY);
        String fileProperty = System.getProperty(LOGGING_FILE_PROPERTY);
        if ((classProperty == null) && (fileProperty == null)) {
            // loads default logging 
            new LoggingLoader();
        }
    }
    
    private static PrintStream createPrintStream(String file) throws FileNotFoundException {
        return new PrintStream(new FileOutputStream(file));
    }

    public static void updateStatus(States state) throws MalformedURLException, IOException {
        String uuid = System.getProperty(UUID_KEY);
//        LOGGER.info("user.home:"+System.getProperty("user.home"));
//        System.getProperties().store(System.out,"");
        String lrURl = ProcessUtils.getLrServlet();
        String restURL = lrURl + "?action=updateStatus&uuid=" + uuid + "&state=" + state;
        httpGet(restURL);
        STATUS_UPDATED = true;
    }

    public static void updatePID(String pid) throws IOException {
        String uuid = System.getProperty(UUID_KEY);
        String lrURl = ProcessUtils.getLrServlet();
        
        String restURL = lrURl + "?action=updatePID&uuid=" + uuid + "&pid=" + pid;
        httpGet(restURL);
    }

    public static void updateName(String name) throws IOException {
        String uuid = System.getProperty(UUID_KEY);
        String lrURl = ProcessUtils.getLrServlet();
        
        String restURL = lrURl + "?action=updateName&uuid=" + uuid + "&name=" + URLEncoder.encode(name, "UTF-8");
        LOGGER.info("requesting url :" + restURL);
        httpGet(restURL);
    }

    public static byte[] httpGet(String restURL) throws MalformedURLException, IOException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            URL url = new URL(restURL);
            URLConnection connection = url.openConnection();
            // authentication token -> identify user
            connection.addRequestProperty("auth-token",System.getProperty(AUTH_TOKEN_KEY));
            connection.addRequestProperty(IPAddressUtils.X_IP_FORWARD, System.getProperty(IPAddressUtils.X_IP_FORWARD));
            InputStream inputStream = connection.getInputStream();
            byte[] buffer = new byte[1 << 12];
            int read = -1;
            while ((read = inputStream.read(buffer)) > 0) {
                bos.write(buffer,0,read);
            }
            ;
            
            return buffer;
        } catch (Exception ex) {
            LOGGER.severe("Problem connecting to REST URL: " + restURL + " - " + ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns PID of process
     * @return
     */
    public static String getPID() {
        String pid = null;
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String[] split = name.split("@");
        if ((split != null) && (split.length > 1)) {
            pid = split[0];
        }
        return pid;
    }

    /**
     * Finds annotated method
     * @param clz Exploring class
     * @return
     */
    public static MethodType annotatedMethodType(Class clz) {
        Method annotatedMethod = annotatedMethod(clz);
        return annotatedMethod != null  ? new MethodType(annotatedMethod, MethodType.Type.ANNOTATED) : null;
    }

    public static Method annotatedMethod(Class clz) {
        Method annotatedMethod = null;
        for (Method m : clz.getMethods()) {
            if (m.isAnnotationPresent(Process.class)) {
                if (Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())) {
                    annotatedMethod = m;
                    break;
                }
            }
        }
        return annotatedMethod;
    }

    /**
     * Find main method
     * @param clz Exploring class
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    public static MethodType mainMethodType(Class clz) throws SecurityException, NoSuchMethodException {
        Method mainMethod = mainMethod(clz);
        return mainMethod != null ? new MethodType(mainMethod, MethodType.Type.MAIN) : null;
    }

    public static Method mainMethod(Class clz) throws NoSuchMethodException {
        Method mainMethod = clz.getMethod("main", (new String[0]).getClass());
        return mainMethod;
    }

    
    public static Annotation findNameAnnot(Annotation[] ann) {
        Annotation nameAnnot = null;
        for (Annotation paramAn : ann) {
            if (paramAn instanceof ParameterName) {
                nameAnnot = paramAn;
                break;
            }
        }
        return nameAnnot;
    }
    
    private static Object instantiate(String val, Class<?> class1) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
        Constructor<?> constructor = class1.getConstructor(new Class[] {String.class});
        return constructor.newInstance(val);
    }

    
    private static Object[] map(Method processMethod, String[] defaultParams, Properties processParametersProperties) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Annotation[][] annots = processMethod.getParameterAnnotations();
        Class<?>[] types = processMethod.getParameterTypes();
        //if (defaultParams.length < types.length) throw new IllegalArgumentException("defaultParams.length is small array. It must have at least "+types.length+" items");
        List<Object> params = new ArrayList<Object>();
        for (int i = 0; i < types.length; i++) {
            Annotation[] ann = annots[i];
            Annotation nameAnnot = findNameAnnot(ann);
            String val = null;
            if (nameAnnot != null) {
                String parameterName = ((ParameterName)nameAnnot).value();
                val = (String) processParametersProperties.get(parameterName);
                val = val != null ? val : defaultParam(defaultParams, i);
            } else {
                val = defaultParam(defaultParams, i);
            }

            if (!(types[i].equals(String.class))) {
                params.add(instantiate(val, types[i]));
            } else {
                params.add(val);
            }
        }
        return params.toArray();
    }

    public static String defaultParam(String[] defaultParams, int i) {
        return defaultParams.length > i ? defaultParams[i] : null;
    }


    
    /**
     * Wrapper which represents found method
     * @author pavels
     */
    static class MethodType { 

        /** enum for type of method */
        static enum Type { MAIN, ANNOTATED };
        
        private Method method;
        private Type type;
        
        public MethodType(Method method, Type type) {
            super();
            this.method = method;
            this.type = type;
        }
        
        
        /**
         * Returns type of method
         * @return
         */
        public Type getType() {
            return type;
        }
        
        /**
         * Returns method
         * @return
         */
        public Method getMethod() {
            return method;
        }
    }

}
