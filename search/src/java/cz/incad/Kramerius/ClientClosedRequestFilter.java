package cz.incad.Kramerius;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;

public class ClientClosedRequestFilter implements Filter {

    private final static Logger LOGGER = java.util.logging.Logger.getLogger(ClientClosedRequestFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(req, res);
        } catch (IOException e) {
            // from time to time some client closes a request (eg. weak signal), so don't make a mess with ugly stracktraces...
            if ("ClientAbortException".equals(e.getClass().getSimpleName())) {
                StringBuilder sb = new StringBuilder();
                if (req instanceof HttpServletRequest) {
                    HttpServletRequest httpRequest = (HttpServletRequest) req;
                    sb.append(httpRequest.getRequestURL());
                }
                LOGGER.info("Client closed request: " + sb);
            } else {
                throw e;
            }
        }
    }

    @Override
    public void destroy() {

    }
}
