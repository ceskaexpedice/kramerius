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
        <x:parse var="doc" xml="${xml}"  />
        <x:forEach varStatus="status" select="$doc/response/result/doc">
            <c:set var="pid"><x:out select="./str[@name='PID']"/></c:set>
            <c:set var="t"><x:out select="./str[@name='root_title']"/></c:set>
            <c:set var="title"><x:out select="./str[@name='dc.title']"/></c:set>
            <c:set var="fmodel"><x:out select="./str[@name='fedora.model']"/></c:set>
          	<c:set var="date"><x:out select="./str[@name='datum_str']"/></c:set>
          	<c:set var="issn"><x:out select="./str[@name='issn']"/></c:set>
          	<c:set var="rozsah"><x:out select="./int[@name='pages_count']"/></c:set>
          	<c:set var="jazyk"><x:out select="./arr[@name='language']"/></c:set> 
          	<c:set var="pocet_jazyku"><x:out select="count(./arr[@name='language']/str)"/></c:set>         	         	          
            <item>
            	<title>${title}</title>
            	<description>
            		<![CDATA[
            		<p>
            		<a href="${rssHome.applicationURL}/handle/${pid}">
            		<img src="${rssHome.applicationURL}/img?uuid=${pid}&stream=IMG_THUMB&action=SCALE&scaledHeight=96" hspace="10" vspace="2" style="border: 0pt none;" align="left" />
            		</a>             		
            		]]>            		           	
             	    <x:forEach select="./arr[@name='dc.creator']/str" var="aut"> 
             	     <c:set var="autor"><x:out select="." /></c:set>
             	     <c:choose>
             	   	 	<c:when test="${not empty autor}">  
             	   	 		<![CDATA[Autor: ${autor} <br/>]]>     	   	 		               	    		
             	  	 	</c:when>             	    			
             	   	  </c:choose> 				
					</x:forEach>	
															
					<c:choose>
             	   	 	<c:when test="${not empty date}">
             	   	 		<![CDATA[Rok vydání: ${date} <br/>]]>
             	  	 	</c:when>             	    			
             	   	</c:choose>
             	   	
             	   	<c:choose>
             	   	 	<c:when test="${not empty fmodel}">
             	   	 		<![CDATA[Typ svazku: <view:msg>fedora.model.${fmodel}</view:msg> <br/>]]>
             	  	 	</c:when>             	    			
             	   	</c:choose> 
             	   	
             	   	<c:choose>
             	   	 	<c:when test="${not empty rozsah && rozsah > 0}">
             	   	 		<![CDATA[Rozsah: ${rozsah} <br/>]]>
             	  	 	</c:when>             	  	 	            	    		
             	   	</c:choose>

             	   	<%
             	   		int zbyvajiciJazyky = Integer.parseInt(pageContext.getAttribute("pocet_jazyku").toString());
             	   		if (zbyvajiciJazyky > 0) {
							out.print("<![CDATA[Jazyk: ]]>");
             	   		}
					%>        	   	             	   	
             	   	<x:forEach select="./arr[@name='language']/str" var="lang">             	                	                	   	
             	     <c:set var="jazyk"><x:out select="." /></c:set>
             	     <c:choose>
             	   	 	<c:when test="${not empty jazyk}">
             	   	 		<![CDATA[<view:msg>language.${jazyk}</view:msg>]]><%
             	   	 			zbyvajiciJazyky--;
             	   	 			if (zbyvajiciJazyky > 0) {
             	   	 				out.print(", ");
             	   	 			}
             	   	 		%>                  	   	 		        	               	   	 			 		             	   	 	
             	  	 	</c:when>              	    			
             	   	  </c:choose> 				
					</x:forEach> 
					<![CDATA[<br/>]]>
					
             	   	<c:choose>
             	   	 	<c:when test="${not empty issn}">
             	   	 	<% 
             	   	 	    String issn = pageContext.getAttribute("issn").toString();
             	   	 		String type = "";
             	   	 		if (issn.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9X]")) {
        	   	 	  			type = "ISSN";        	   	 	  			 
        	   	 	    	} else {
        	   	 	    		type = "ISBN";
        	   	 	    	}
             	   	 	%> 
             	   	 		<![CDATA[<%=type%>: ${issn} <br/>]]>
             	  	 	</c:when>             	    			
             	   	</c:choose> 
             	   	
             	   	<![CDATA[</p>]]>
                </description>                
                <link>${rssHome.applicationURL}/handle/${pid}</link>
                <guid>${rssHome.applicationURL}/handle/${pid}</guid>
            </item>
        </x:forEach>
        
    </c:otherwise>
</c:choose>
<c:if test="${param.debug}" >
    <c:out value="${url}" /><br/>
    <c:out value="${param.parentPid}" />
</c:if>
        </c:forEach>
  </channel>
  </rss>