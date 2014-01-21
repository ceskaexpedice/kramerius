package cz.incad.Kramerius.backend.guice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;

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
import cz.incad.kramerius.security.guice.GuiceSecurityModule;
import cz.incad.kramerius.security.impl.http.GuiceSecurityHTTPModule;
import cz.incad.kramerius.service.guice.I18NModule;
import cz.incad.kramerius.service.guice.MailModule;
import cz.incad.kramerius.service.guice.ServicesModule;
import cz.incad.kramerius.users.guice.LoggedUsersModule;
import cz.incad.kramerius.utils.IOUtils;

public class GuiceConfigBean extends GuiceServletContextListener {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GuiceConfigBean.class.getName());
    
    public GuiceConfigBean() {
        super();
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        String realPath = servletContextEvent.getServletContext().getRealPath("WEB-INF/lib");
        System.setProperty(LongRunningProcessModule.DEFAULT_LIBS_KEY, realPath);
        super.contextInitialized(servletContextEvent);
    }

    @Override
    protected Injector getInjector() {
        Injector injector = Guice.createInjector(
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
                servletModule()
        );

        
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
    
    //only one extension module is now supported
    public static Module extensionModule() throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
    	URL urlRes = GuiceConfigBean.class.getResource("res/guice.module");
    	if (urlRes != null) {
    		InputStream istream = urlRes.openConnection().getInputStream();
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		IOUtils.copyStreams(istream, bos);
    		Class clz = Class.forName(new String(bos.toByteArray()));
    		return (Module) clz.newInstance();
    	} else  return new AbstractModule() {
			
			@Override
			protected void configure() {
				// TODO Auto-generated method stub
				
			}
		};
    }
    
    
    @Provides
    @Named("fontsDir")
    public File getWebAppsFontsFolder() {
        String dirName = Constants.WORKING_DIR + File.separator + "fonts";
        return new File(dirName);
    }
}
