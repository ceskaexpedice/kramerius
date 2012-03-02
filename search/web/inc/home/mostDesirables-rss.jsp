<%@ page contentType="text/xml" pageEncoding="UTF-8"%><rss version="2.0">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>
<%@ page isELIgnored="false"%> <%@page
 import="java.util.*"%> <%@page
 import="com.google.inject.Injector"%> <%@page
 import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="java.io.InputStream"%> <%@page
 import="java.io.InputStreamReader"%> <%@page
 import="cz.incad.kramerius.utils.RESTHelper"%> <%@page
 import="cz.incad.kramerius.utils.IOUtils"%> <%@page
 import="java.io.ByteArrayOutputStream"%> <%@page
 import="com.google.inject.Injector"%> <%@page
 import="cz.incad.kramerius.processes.LRProcessManager"%>
<%@page import="cz.incad.kramerius.processes.DefinitionManager"%>
<%@page import="cz.incad.kramerius.MostDesirable"%>
<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%> <%@page
 import="cz.incad.kramerius.utils.conf.KConfiguration"%>

<view:object name="rssHome"
 clz="cz.incad.Kramerius.views.inc.home.RSSHomeViewObject"></view:object> <channel>

<title><view:msg>application.title</view:msg></title> <description><view:msg>home.tab.mostDesirables</view:msg></description>
<link> <c:out value="${rssHome.channelURL}" escapeXml="true" />
</link> <c:forEach items="${rssHome.mostDesirables}" var="uuid">


 <c:url var="url" value="${rssHome.configuration.solrHost}/select/">
  <c:param name="q" value="PID:\"${uuid}\"" />
 </c:url>

 <c:catch var="exceptions">
  <c:import url="${url}" var="xml" charEncoding="UTF-8" />
 </c:catch>
 <c:choose>
  <c:when test="${exceptions != null}">
   <c:out value="${exceptions}" />
   <c:out value="${xml}" />
  </c:when>
  <c:otherwise>
   <x:parse var="doc" xml="${xml}" />

   <x:forEach varStatus="status" select="$doc/response/result/doc">
    <c:set var="pid">
     <x:out select="./str[@name='PID']" />
    </c:set>
    <c:set var="t">
     <x:out select="./str[@name='root_title']" />
    </c:set>
    <c:set var="title">
     <x:out select="./str[@name='dc.title']" />
    </c:set>
    <c:set var="fmodel">
     <x:out select="./str[@name='fedora.model']" />
    </c:set>
    <item> <title>${title}</title> <description>PID:
    ${pid} Model: <view:msg>${fmodel}</view:msg> </description> <link>${rssHome.applicationURL}/handle/${pid}</link>
    <guid>${rssHome.applicationURL}/handle/${pid}</guid> </item>
   </x:forEach>

  </c:otherwise>
 </c:choose>
</c:forEach> </channel> </rss>