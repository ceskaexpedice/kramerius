<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<%@page import="com.google.inject.*"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext, cz.incad.kramerius.FedoraAccess"%>
<%
            Injector inj = (Injector) application.getAttribute(Injector.class.getName());
            LocalizationContext lctx = inj.getProvider(LocalizationContext.class).get();
            FedoraAccess fedoraAccess = inj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));
            pageContext.setAttribute("lctx", lctx);
            java.io.InputStream is = fedoraAccess.getDataStream("uuid:"+request.getParameter("uuid"), "ALTO");
            String alto = cz.incad.kramerius.utils.IOUtils.readAsString(is, java.nio.charset.Charset.forName("UTF-8") , true);
            pageContext.setAttribute("xml", alto);
%>
<c:catch var="exceptions">
    <c:url var="xslPage" value="inc/results/xsl/metsalto.xsl" />
    <c:import url="${xslPage}" var="xsltPage" charEncoding="UTF-8"  />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${url}" />
        <c:out value="${xml}" />
    </c:when>
    <c:otherwise>
        <c:if test="${param.debug =='true'}"><c:out value="${url}" /></c:if>
        <c:catch var="exceptions2">
            <% out.clear();%>
            <x:transform doc="${xml}"  xslt="${xsltPage}"  >
                <x:param name="q" value="${param.q}"/>
                <x:param name="w" value="${param.w}"/>
                <x:param name="h" value="${param.h}"/>
            </x:transform>
        </c:catch>
        <c:if test="${exceptions2 != null}"><c:out value="${exceptions2}" />
        </c:if>
    </c:otherwise>
</c:choose>