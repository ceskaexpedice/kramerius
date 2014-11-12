package cz.incad.kramerius.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public final class CrawlHTTPFilter implements Filter {

    public static Logger LOGGER = Logger.getLogger(Logger.class.getName());

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        try {
            HttpServletRequest httpReq = (HttpServletRequest) request;
            String queryString = httpReq.getQueryString();
            if ((queryString != null) && (queryString.contains("_escaped_fragment_"))) {
                String param = httpReq.getParameter("_escaped_fragment_");
                ServletContext context = httpReq.getSession().getServletContext();
                context.getRequestDispatcher("/sites/share.vm?pid="+param).forward(request,response);
            } else {
                try {
                    chain.doFilter(request, response);
                } catch (ServletException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }
        } catch (ServletException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    public void destroy() {
     }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
     }
}