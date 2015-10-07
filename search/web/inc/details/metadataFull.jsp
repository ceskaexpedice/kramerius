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
            org.w3c.dom.Document xml = fedoraAccess.getBiblioMods(request.getParameter("pid"));

    cz.incad.kramerius.service.XSLService xs = (cz.incad.kramerius.service.XSLService) ctxInj.getInstance(cz.incad.kramerius.service.XSLService.class);
    
    pageContext.setAttribute("xml", xml);
    
    String xmlStr = xs.serialize(xml);
    pageContext.setAttribute("xmlStr", xmlStr);

%>
<c:set var="xsl" value="xsl/modsFull.xsl" scope="request" />
<c:url var="xslPage" value="${xsl}" >
</c:url>
<c:url var="xslIdent" value="xsl/ident.xsl" >
</c:url>
<c:catch var="exceptions"> 
    <c:import url="${xslPage}" var="xsltPage" charEncoding="UTF-8"  />
    <c:import url="${xslIdent}" var="xsltIdent" charEncoding="UTF-8"  />
</c:catch>
<c:set var="xmlStr"><x:transform doc="${xml}"  xslt="${xsltIdent}"  /></c:set>
<c:choose>
    <c:when test="${exceptions != null}" >
        <jsp:useBean id="exceptions" type="java.lang.Exception" />
        <% System.out.println(exceptions); %>
    </c:when>
    <c:otherwise>
        <c:catch var="exceptions2"> 
            <% out.clear(); %>
            <div id="mods-full" style="height: 100%;">
                <ul>
                    <li><a href="#mods-html" class="vertical-text" >html</a></li>
                    <li><a href="#mods-xml" class="vertical-text" >xml</a></li>
                </ul>
                <div id="mods-html">
<%
                    
    try {
        String xsl = "modsFull.xsl";
        if (xs.isAvailable(xsl)) {
            String text = xs.transform(xml, xsl, lctx.getLocale());
            out.println(text);
        }else{
%>
                    <x:transform doc="${xml}"  xslt="${xsltPage}"  >
                        <x:param name="pid" value="${param.pid}"/>
                        <x:param name="bundle_url" value="${i18nServlet}"/>
                        <x:param name="model" value="${param.model}"/>
                    </x:transform>
<%            
        }
    } catch (Exception e) {
        out.println(e);
    }
%>
                </div>
                <div id="mods-xml" style="overflow:scroll;  height: calc(100% - 40px);
  padding: 3px;">
<pre><c:out escapeXml="true" value="${xmlStr}" /></pre>
                </div>
            </div>
        </c:catch>
        <c:choose>
            <c:when test="${exceptions2 != null}" >
                <jsp:useBean id="exceptions2" type="java.lang.Exception" />
                <% System.out.println(exceptions2); %>
            </c:when>
            <c:otherwise></c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>