package cz.incad.kramerius.auth.thirdparty.shibb.cdk;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import cz.incad.kramerius.auth.thirdparty.ExtAuthFilter;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.CDKShibbolethForwardUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class CDKShibbolethForwardFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean channel = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.channel", false);
        if (channel && CDKShibbolethForwardUtils.isTokenFromCDK((HttpServletRequest) request)) {
            String tokenVal = ((HttpServletRequest) request).getHeader(CDKShibbolethForwardUtils.CDK_HEADER_KEY);
            Map<String, String> headers = CDKShibbolethForwardUtils.tokenHeaders(tokenVal);
            String ipAddress = request.getRemoteAddr();
            if (headers.containsKey("ip_address")) {
                ipAddress = headers.get("ip_address");
            }
            
            HttpServletRequest authenticated = CDKSecuredChannelHTTPProxyRequest.newInstance((HttpServletRequest) request,
                    null, headers, ipAddress);
            
            chain.doFilter(authenticated, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
