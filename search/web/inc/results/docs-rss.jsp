<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/securedContent.tld" prefix="scrd" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext, cz.incad.kramerius.*"%>
<%@page import="cz.incad.Kramerius.backend.guice.LocalesProvider"%>
<%@page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%@page import="cz.incad.Kramerius.*"%>

    <view:object name="docsRSS" clz="cz.incad.Kramerius.views.inc.results.DocsRSSView"></view:object>

    <c:url var="results" value="inc/results/xsl/results_main_rss.xsl" />
    <c:import url="${results}" var="resultsxsl" charEncoding="UTF-8"  />
     <x:transform doc="${xml}"  xslt="${resultsxsl}">
         <x:param name="bundle_url" value="${i18nServlet}"/>

         <x:param name="q" value="${param.q}"/>
         <x:param name="applUrl" value="${docsRSS.applicationBase}"/>
         <x:param name="channelUrl" value="${docsRSS.channelUrl}"/>

         <c:if test="${!empty param.collection}">
             <x:param name="collection" value="&collection=${param.collection}" />
         </c:if>
         <x:param name="fqs"><c:forEach var="fqs" items="${paramValues.fq}">&fq=<c:out value="${fqs}" escapeXml="false" /></c:forEach></x:param>
     </x:transform>
