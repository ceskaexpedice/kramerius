<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="com.google.inject.Injector"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocalizationContext"%>
<%
	Injector ctxInj = (Injector)application.getAttribute(Injector.class.getName());
	LocalizationContext lctx= ctxInj.getProvider(LocalizationContext.class).get();
	pageContext.setAttribute("lctx", lctx);
	
%>
<c:set var="sort" scope="request">level asc, title asc</c:set>
<%@ include file="inc/searchParams.jsp" %>
<% out.clear(); %>
<c:if test="${param.debug}" >
    <c:out value="${url}" /><br/><c:out value="${exceptions}" />
</c:if>
<%--
    <x:forEach varStatus="status" select="$doc/response/result/doc">
    <div id="uncoll_<c:out value="${uuid}"/>" class="r<c:out value="${status.count % 2}" />">
        <c:set var="uuid" >
            <x:out select="./str[@name='PID']"/>
        </c:set>
        <jsp:useBean id="uuid" type="java.lang.String" />
        <c:set var="itemUrl" >
            ./item.jsp?pid=<c:out value="${uuid}"/>&pid_path=<x:out select="./str[@name='pid_path']"/>&path=<x:out select="./str[@name='path']"/>
        </c:set>
        <x:if select="./str[@name='fedora.model'] = 'page'">
            <c:set var="itemUrl" ><c:out value="${itemUrl}"/>&format=<x:out select="./str[@name='page_format']"/></c:set>
        </x:if>
        <x:set select="./str[@name='PID']" var="pid" />
       
       <x:choose>
           <x:when select="./str[@name='dc.title'] = ''">
               <a href="<c:out value="${itemUrl}" escapeXml="false" />" ><b>none</b></a>
           </x:when>
           <x:otherwise>
            <a href="<c:out value="${itemUrl}" escapeXml="false" />" ><b><x:out select="./str[@name='dc.title']"/></b></a>
           </x:otherwise>
       </x:choose>
    
    <span class="textpole">(<fmt:message bundle="${lctx}">fedora.model.<x:out select="./str[@name='fedora.model']"/></fmt:message>)</span>
    <x:if select="./int[@name='pages_count'] != '0'">
    <span><x:out select="./int[@name='pages_count']"/></span>
    </x:if>
    <span><c:out value="${collapseCount}" escapeXml="false" /></span>
    
    </div>
</x:forEach>
--%>
<c:url var="xslPage" value="inc/results/xsl/uncollapsed.xsl" />
<c:catch var="exceptions"> 
    <c:import url="${xslPage}" var="xsltPage" charEncoding="UTF-8"  />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${url}" />
        <c:out value="${xml}" />
    </c:when>
    <c:otherwise>
        <% out.clear();%>
        <c:if test="${param.debug =='true'}"><c:out value="${url}" /></c:if>
        <c:catch var="exceptions2"> 
            <x:transform doc="${xml}"  xslt="${xsltPage}"  >
                <x:param name="root_pid" value="${param.root_pid}"/>
                <x:param name="q" value="${param.q}"/>
            </x:transform>
            <c:set var="obj" value="#tabs_${param.level}" />
            <c:set var="href" value="#{href}" />
            <c:set var="label" value="#{label}" />
            <c:set var="target" value="#tab${label}-page" />
        </c:catch>
        <c:if test="${exceptions2 != null}"><c:out value="${exceptions2}" />
        </c:if>
    </c:otherwise>
</c:choose>
<%@ include file="inc/paginationPageNum.jsp" %>