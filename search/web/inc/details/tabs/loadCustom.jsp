<%@page import="cz.incad.kramerius.utils.XMLUtils"%>
<%@page import="java.io.ByteArrayInputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="cz.incad.kramerius.utils.UnicodeUtil"%>
<%@page import="java.nio.charset.Charset"%>
<%@page import="cz.incad.kramerius.utils.IOUtils"%>
<%@page import="java.io.InputStream"%>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%

    Injector ctxInj = (Injector) application.getAttribute(Injector.class.getName());
    LocalizationContext lctx = ctxInj.getProvider(LocalizationContext.class).get();
    pageContext.setAttribute("lctx", lctx);
    String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang=" + lctx.getLocale().getLanguage() + "&country=" + lctx.getLocale().getCountry() + "&name=labels";
    pageContext.setAttribute("i18nServlet", i18nServlet);
    FedoraAccess fedoraAccess = ctxInj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));
    String tab = request.getParameter("tab");
    String ds = tab.split("\\.")[0];
    String xsl = tab.split("\\.")[1] + ".xsl";
    String pid_path = request.getParameter("pid_path");

    for (String pid : pid_path.split("/")) {
        if (fedoraAccess.isStreamAvailable(pid, ds)) {
            String mime = fedoraAccess.getMimeTypeForStream(pid, ds);
            if (mime.equals("text/plain")) {
                InputStream is = fedoraAccess.getDataStream(pid, ds);
                byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(is);
                String enc = UnicodeUtil.getEncoding(bytes);
                ByteArrayInputStream is2 = new ByteArrayInputStream(bytes);
                out.println("<pre>" + IOUtils.readAsString(is2, Charset.forName(enc), true) + "</pre>");
            } else if (mime.equals("text/xml")) {
                org.w3c.dom.Document xml = XMLUtils.parseDocument(fedoraAccess.getDataStream(pid, ds), true);
                cz.incad.kramerius.service.XSLService xs = (cz.incad.kramerius.service.XSLService) ctxInj.getInstance(cz.incad.kramerius.service.XSLService.class);
                try {
                    if (xs.isAvailable(xsl)) {
                        String text = xs.transform(xml, xsl, lctx.getLocale());
                        out.println(text);
                    }
                } catch (Exception e) {
                    out.println(e);
                }
            }
        }
    }
%>