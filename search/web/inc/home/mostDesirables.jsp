<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
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
<%@page import="cz.incad.kramerius.utils.conf.KConfiguration"%>
<%

	Injector ctxInj = (Injector)application.getAttribute(Injector.class.getName());
        KConfiguration kconfig = ctxInj.getProvider(KConfiguration.class).get();
        pageContext.setAttribute("kconfig", kconfig);
	pageContext.setAttribute("lrProcessManager",ctxInj.getInstance(LRProcessManager.class));
	pageContext.setAttribute("dfManager",ctxInj.getInstance(DefinitionManager.class));
	
	LocalizationContext lctx= ctxInj.getProvider(LocalizationContext.class).get();
	pageContext.setAttribute("lctx", lctx);

        FedoraAccess fedoraAccess = ctxInj.getInstance(com.google.inject.Key.get(FedoraAccess.class, com.google.inject.name.Names.named("securedFedoraAccess")));

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
        <c:param name="q" value="PID:${uuid}" />
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
            <c:set var="uuid" >
                <x:out select="./str[@name='PID']"/>
            </c:set>
            <c:set var="root_pid" >
                <x:out select="./str[@name='root_pid']"/>
            </c:set>
            <jsp:useBean id="uuid" type="java.lang.String" />
            <c:set var="fedora_model" >
                <x:out select="./str[@name='fedora.model']"/>
            </c:set>
            <jsp:useBean id="fedora_model" type="java.lang.String" />
            <c:set var="itemUrl" >
                ./item.jsp?pid=<c:out value="${uuid}"/>&pid_path=<x:out select="./str[@name='pid_path']"/>&path=<x:out select="./str[@name='path']"/>
            </c:set>
            <c:set var="itemUrl" ><c:out value="${itemUrl}"/>&format=<x:out select="./str[@name='page_format']"/></c:set>
            
            <x:set select="./str[@name='PID']" var="pid" />
            <div align  ="center" style="overflow:hidden; border:1px solid  #eeeeee; width:100px; height:100px; float:left; margin:5px;"><a href="<c:out value="${itemUrl}" escapeXml="false" />" >
            <img align="middle" vspace="2" id="img_<c:out value="${uuid}"/>" src="img?uuid=${uuid}&stream=IMG_THUMB&action=SCALE&scaledHeight=96" border="0"
                 title="<x:out select="./str[@name='root_title']"/> (<fmt:message bundle="${lctx}"><x:out select="./str[@name='fedora.model']"/></fmt:message>)"
                 alt="<x:out select="./str[@name='root_title']"/>" /></a>
            </div>
        </x:forEach>
        
    </c:otherwise>
</c:choose>
<%}%>