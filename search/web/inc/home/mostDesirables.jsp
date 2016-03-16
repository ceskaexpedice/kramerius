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
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view"%>

<view:kconfig var="kConfigSolrHost" key="solrHost" />


<view:object name="mostDesirableViewObject" clz="cz.incad.Kramerius.views.inc.MostDesirableViewObject"></view:object>

<c:forEach items="${mostDesirableViewObject.pids}" var="uuid" varStatus="status">
    <c:url var="url" value="${kConfigSolrHost}/select/" >
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
            <div align="center" style="overflow:hidden; border:1px solid #eeeeee; width:100px; height:100px; float:left; margin:5px;">
                <a href="i.jsp?pid=${pid}" >
                    <img align="middle" vspace="2" id="img_${pid}" src="img?uuid=${pid}&stream=IMG_THUMB&action=SCALE&scaledHeight=96" border="0"
                         title="${t}" alt="${t}" />
                </a>
            </div>
        </x:forEach>
        
     </c:otherwise>
    </c:choose>

 </c:forEach>

<div ><a href="inc/home/mostDesirables-rss.jsp"><span class="ui-icon ui-icon-signal-diag"></span></a></div>