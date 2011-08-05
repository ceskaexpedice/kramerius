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
            String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang="+lctx.getLocale().getLanguage()+"&country="+lctx.getLocale().getCountry()+"&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);
            FedoraAccess fedoraAccess = ctxInj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));
            String tab = request.getParameter("tab");
            if(tab.equals("text_ocr")){
                if(fedoraAccess.isStreamAvailable(request.getParameter("pid"), "TEXT_OCR")){
                    InputStream is = fedoraAccess.getDataStream(request.getParameter("pid"), "TEXT_OCR");
                    out.println("<pre>"+IOUtils.readAsString(is, Charset.forName("UTF-8"), true)+"</pre>");
                    //out.println("<pre>"+ org.apache.commons.io.IOUtils.toString(is)+"</pre>");
                    
                }
                return;
            }
            org.w3c.dom.Document xml = fedoraAccess.getBiblioMods(request.getParameter("pid"));
            cz.incad.kramerius.service.XSLService xs = (cz.incad.kramerius.service.XSLService) ctxInj.getInstance(cz.incad.kramerius.service.XSLService.class);
            try {
                String xsl = tab + ".xsl";
                if (xs.isAvailable(xsl)) {
                    String text = xs.transform(xml, xsl, lctx.getLocale());
                    out.println(text);
                    return;
                }
            } catch (Exception e) {
                out.println(e);
            }
            pageContext.setAttribute("xml", xml);

%>