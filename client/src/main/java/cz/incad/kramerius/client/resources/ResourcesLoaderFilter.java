package cz.incad.kramerius.client.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.openid4java.util.HttpResponse;

import cz.incad.kramerius.client.resources.merge.Merge;
import cz.incad.kramerius.processes.guice.LongRunningProcessModule;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.utils.StringUtils;

public class ResourcesLoaderFilter implements Filter {

    
    public static final Logger LOGGER = Logger.getLogger(ResourcesLoaderFilter.class.getName());

    public static String getExtensionsHome() {
        String path = System.getProperty("user.home") + File.separator + ".kramerius4" + 
                File.separator + "k5client" + File.separator + 
                File.separator + "exts" + File.separator;
        return path;
    }

    protected String prefix = null;
    protected String confMountpoint = null;
    protected String warMountpoint = null;
    protected Merge merger = null;
    protected String mimeType = null;
    protected String fileExtRegexp = null; 
    

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try { 
            String mergeClass = filterConfig.getInitParameter("merge");

            Class<?> clz = Class.forName(mergeClass);
            this.merger = (Merge) clz.newInstance();

            this.prefix = filterConfig.getInitParameter("prefix");

            this.mimeType = filterConfig.getInitParameter("mimetype");
            this.confMountpoint = getExtensionsHome()+File.separator + filterConfig.getInitParameter("confmount");
            this.warMountpoint = filterConfig.getServletContext().getRealPath(filterConfig.getInitParameter("warmount"));
            this.fileExtRegexp = filterConfig.getInitParameter("fileext");
        } catch (ClassNotFoundException  e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (InstantiationException  e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;
        String mn = StringUtils.minus(httpReq.getRequestURI(), this.prefix);
        if (fileExtRegexp != null) {
            if (mn.matches(this.fileExtRegexp)) {
                this.dispatchFile(httpResp, mn);
            } else {
                chain.doFilter(request, response);
            }
        } else {
            dispatchFile(httpResp, mn);
        }
    }

    private void dispatchFile(HttpServletResponse httpResp, String mn)
            throws IOException, FileNotFoundException {
        File realPath = new File(this.warMountpoint+File.separator+mn);
        File configPath = new File(this.confMountpoint+File.separator+mn);
        mergeFiles(this.merger, httpResp, realPath, configPath, this.mimeType);
    }

    public static void mergeFiles(Merge merge, HttpServletResponse httpResp, File realPath,
            File configPath, String mimetype) throws IOException, FileNotFoundException {
        if (realPath.exists() && configPath.exists()) {
            String realString = IOUtils.readAsString(new FileInputStream(realPath), Charset.forName("UTF-8"), true);
            String confString = IOUtils.readAsString(new FileInputStream(configPath), Charset.forName("UTF-8"), true);
            String mergered = merge.merge(realString, confString);
            httpResp.setStatus(HttpServletResponse.SC_OK);
            httpResp.setContentType(mimetype);
            httpResp.getWriter().write(mergered);
        } else if (realPath.exists()) {
            String string = IOUtils.readAsString(new FileInputStream(realPath), Charset.forName("UTF-8"), true);
            httpResp.setStatus(HttpServletResponse.SC_OK);

            String mergered = merge.merge(string, "");
            httpResp.setStatus(HttpServletResponse.SC_OK);
            httpResp.setContentType(mimetype);
            httpResp.getWriter().write(mergered);

        } else if (configPath.exists()){
            String string = IOUtils.readAsString(new FileInputStream(configPath), Charset.forName("UTF-8"), true);

            String mergered = merge.merge(string, "");
            httpResp.setStatus(HttpServletResponse.SC_OK);
            httpResp.setContentType(mimetype);
            httpResp.getWriter().write(mergered);
        }
    }

    @Override
    public void destroy() {
    }
}
