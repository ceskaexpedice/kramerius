<%@ page contentType="text/xml" pageEncoding="UTF-8" %><rss version="2.0">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>
<%@ page isELIgnored="false"%>
<%@page import="java.util.*"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="cz.incad.kramerius.utils.RESTHelper"%>
<%@page import="cz.incad.kramerius.utils.IOUtils"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="cz.incad.kramerius.processes.LRProcessManager"%>
<%@page import="cz.incad.kramerius.processes.DefinitionManager"%>
<%@page import="cz.incad.kramerius.MostDesirable"%>
<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>
<%@page import="cz.incad.kramerius.FedoraAccess"%>
<%@page import="cz.incad.Kramerius.I18NServlet"%>
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
  <channel> 
<%

	Injector ctxInj = (Injector)application.getAttribute(Injector.class.getName());
        KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
        pageContext.setAttribute("kconfig", kconfig);
	pageContext.setAttribute("lrProcessManager",ctxInj.getInstance(LRProcessManager.class));
	pageContext.setAttribute("dfManager",ctxInj.getInstance(DefinitionManager.class));
	
	LocalizationContext lctx= ctxInj.getProvider(LocalizationContext.class).get();
	pageContext.setAttribute("lctx", lctx);
        String i18nServlet = I18NServlet.i18nServlet(request) + "?action=bundle&lang="+lctx.getLocale().getLanguage()+"&country="+lctx.getLocale().getCountry()+"&name=labels";
            pageContext.setAttribute("i18nServlet", i18nServlet);
            
        String urlString = request.getRequestURL().toString();
        String query = request.getQueryString();
        pageContext.setAttribute("channelUrl", urlString +"?"+query);

        FedoraAccess fedoraAccess = ctxInj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));
%>
  <title><view:msg>application.title</view:msg></title> 
  <description><view:msg>home.tab.mostDesirables</view:msg></description> 
  <link>   <c:out value="${channelUrl}" escapeXml="true" />  </link> 
<%
	List<String> uuids = (List<String>)ctxInj.getInstance(MostDesirable.class).getMostDesirable(18);
        Iterator it = uuids.iterator();
        
        String itemUrl;
        String path = "";
        for(String pid :uuids){
            //itemUrl = "./item.jsp?pid="+ pi + "&pid_path=" + uuid + "&path=" + path;
            //imagePid = "thumb?uuid=" + FedoraUtils.findFirstPagePid("uuid:" + pid);
            pageContext.setAttribute("uuid", pid);
    %>
    <c:url var="url" value="${kconfig.solrHost}/select/" >
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
        <x:parse var="doc" xml="${xml}"  />
        
         <x:forEach varStatus="status" select="$doc/response/result/doc">
            <c:set var="pid"><x:out select="./str[@name='PID']"/></c:set>
            <c:set var="t"><x:out select="./str[@name='root_title']"/></c:set>
            <c:set var="title"><x:out select="./str[@name='dc.title']"/></c:set>
            <c:set var="fmodel"><x:out select="./str[@name='fedora.model']"/></c:set>
            <item>
                <title>${title}</title>    
                <description>PID: ${pid} Model: <view:msg>${fmodel}</view:msg>     
                </description>
                <link>${applUrl}/i.jsp?pid=${pid}</link>
                <guid>${applUrl}/i.jsp?pid=${pid}</guid>
            </item>
        </x:forEach>
        
    </c:otherwise>
</c:choose>
<%}%>
  </channel>
  </rss>