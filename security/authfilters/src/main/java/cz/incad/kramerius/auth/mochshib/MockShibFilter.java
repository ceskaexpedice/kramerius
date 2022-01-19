package cz.incad.kramerius.auth.mochshib;

import cz.incad.kramerius.utils.StringUtils;
import org.apache.commons.io.FileUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;

public class MockShibFilter implements Filter {

        public static  String SHIB_KEY = "shib";

        public static final Hashtable<String,String> shibTable = new Hashtable<>();
        static {
            shibTable.put("shib-session-id", "_dd68cbd66641c9b647b05509ac0241f7");
            shibTable.put("shib-session-index", "_36e3755e67acdeaf1b8b6f7ebebecdeb3abd6ddc98");
            shibTable.put("shib-session-expires", "1592847906");
            shibTable.put("shib-identity-provider", "https://shibboleth.mzk.cz/simplesaml/metadata.xml");
            shibTable.put("shib-authentication-method", "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
            shibTable.put("shib-handler", "https://dnnt.mzk.cz/Shibboleth.sso");
            //remote_user = ermak@mzk.cz
            shibTable.put("remote_user", "user@mzk.cz");
            //affiliation = staff@mzk.cz;member@mzk.cz;employee@mzk.cz
            shibTable.put("affilation","staff@mzk.cz;member@mzk.cz;employee@mzk.cz");
            shibTable.put("edupersonuniqueid","user@mzk.cz");
        }



    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String shib = servletRequest.getParameter(SHIB_KEY);
        HttpSession session = ((HttpServletRequest) servletRequest).getSession(false);
        Hashtable table = shibTable;
        // string u
        //if (shib != null && shib.equals("default")) {
        if (shib != null && StringUtils.isAnyString(shib)) {
            session = ((HttpServletRequest) servletRequest).getSession(true);
            session.setAttribute(SHIB_KEY, true);

            if(!shib.equals("default")) {
                Properties properties = new Properties();
                properties.load(new FileReader(shib));
                table = properties;
            }
        }

        if (session != null &&  session.getAttribute(SHIB_KEY) != null ) {
            HttpServletRequest httpReq = (HttpServletRequest) servletRequest;
            Object o = Proxy.newProxyInstance(servletRequest.getClass().getClassLoader(), new Class[]{HttpServletRequest.class}, new MockHTTPServletInvocationHandler(table, httpReq));
            filterChain.doFilter((ServletRequest) o, servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    @Override
    public void destroy() { }
}
