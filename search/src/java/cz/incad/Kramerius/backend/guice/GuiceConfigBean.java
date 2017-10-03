package cz.incad.Kramerius.backend.guice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;

import com.google.gwt.user.client.ui.DelegatingFocusListenerCollection;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import cz.incad.Kramerius.exts.menu.context.guice.ContextMenuConfiguration;
import cz.incad.Kramerius.exts.menu.main.guice.MainMenuConfiguration;
import cz.incad.Kramerius.statistics.formatters.guice.FormatterModule;
import cz.incad.kramerius.Constants;
import cz.incad.kramerius.database.guice.DatabaseVersionGuiceModule;
import cz.incad.kramerius.document.guice.DocumentServiceModule;
import cz.incad.kramerius.imaging.guice.ImageModule;
import cz.incad.kramerius.pdf.guice.PDFModule;
import cz.incad.kramerius.printing.guice.PrintModule;
import cz.incad.kramerius.processes.guice.LongRunningProcessModule;
import cz.incad.kramerius.rest.api.guice.IiifServletModule;
import cz.incad.kramerius.security.guice.GuiceSecurityModule;
import cz.incad.kramerius.security.impl.http.GuiceSecurityHTTPModule;
import cz.incad.kramerius.service.guice.I18NModule;
import cz.incad.kramerius.service.guice.MailModule;
import cz.incad.kramerius.service.guice.ServicesModule;
import cz.incad.kramerius.users.guice.LoggedUsersModule;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class GuiceConfigBean extends GuiceServletContextListener {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GuiceConfigBean.class.getName());
    
    public GuiceConfigBean() {
        super();
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        String realPath = servletContextEvent.getServletContext().getRealPath("/WEB-INF/lib");
        String defaultProcesses = KConfiguration.getInstance().getProperty(".kramerius.deafult_processes_libs_dir");
        // check if it is null or not
        if (realPath != null || defaultProcesses != null) {
            System.setProperty(LongRunningProcessModule.DEFAULT_LIBS_KEY, realPath != null ? realPath : defaultProcesses);
        } else {
            LOGGER.warning("cannot resolve path to WEB-INF/lib - couldn't to start processes");
        }
        super.contextInitialized(servletContextEvent);
    }

    @Override
    protected Injector getInjector() {
    	List<AbstractModule> modules = new ArrayList<AbstractModule>(Arrays.asList(
                new BaseModule(), // base  module
                
                new ServicesModule(), // base services
                
                new PDFModule(), // pdf services
                
                new ImageModule(), // images
                new I18NModule(), // i18n module
                new LoggedUsersModule(), new MailModule(), // mail service
                                                           // module

                new DocumentServiceModule(),
                new GuiceSecurityModule(), 
                new GuiceSecurityHTTPModule(),
                new LongRunningProcessModule(), // for long running processes

                new PrintModule(), // printing
                new DatabaseVersionGuiceModule(), // db versions
                
                new MainMenuConfiguration(), // menu modules
                new ContextMenuConfiguration(), // menu modules

                new FormatterModule(), // statistics formatters

                new IiifServletModule(),

                servletModule()
		)); 
    	
    	try {
			// api extensions
			modules.addAll(extensionModule());
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE,"ignoring API extensions");
			LOGGER.log(Level.SEVERE,e.getMessage(),e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,"ignoring API extensions");
			LOGGER.log(Level.SEVERE,e.getMessage(),e);
		} catch (InstantiationException e) {
			LOGGER.log(Level.SEVERE,"ignoring API extensions");
			LOGGER.log(Level.SEVERE,e.getMessage(),e);
		} catch (IllegalAccessException e) {
			LOGGER.log(Level.SEVERE,"ignoring API extensions");
			LOGGER.log(Level.SEVERE,e.getMessage(),e);
		}
    	
    	Injector injector = Guice.createInjector(modules);
        return injector;
    }
    
    public static ServletModule servletModule() {
        try {
            Class<?> clz = Class.forName("cz.incad.kramerius.rest.api.guice.ApiServletModule");
            return (ServletModule) clz.newInstance();
        } catch (ClassNotFoundException e) {
            // no problem
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return new ServletModule();
    }
    
    public static List<AbstractModule> extensionModule() throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
    	List<AbstractModule> list = new ArrayList<AbstractModule>();
    	String resGuiceModule = "res/guice.module";
		Enumeration<URL> urlRes = GuiceConfigBean.class.getClassLoader().getResources(resGuiceModule);
    	while(urlRes.hasMoreElements()) {
    		URL url = urlRes.nextElement();
    		InputStream istream = url.openConnection().getInputStream();
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		IOUtils.copyStreams(istream, bos);
    		Class clz = Class.forName(new String(bos.toByteArray()));
    		list.add( (AbstractModule) clz.newInstance());
    	}
    	return list;
    }
    
    
    @Provides
    @Named("fontsDir")
    public File getWebAppsFontsFolder() {
        String dirName = Constants.WORKING_DIR + File.separator + "fonts";
        return new File(dirName);
    }
}
