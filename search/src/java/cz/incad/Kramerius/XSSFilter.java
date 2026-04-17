package cz.incad.Kramerius;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;


/**
 * Cross site scripting avoid filter
 */
public class XSSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;
        Enumeration parameterNames = httpReq.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            Object o = parameterNames.nextElement();
            String[] parameters = httpReq.getParameterValues(o.toString());
            for (String param : parameters) {
                if (param.contains("<script")) {
                    //
                    httpRes.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
        }
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {

    }
}