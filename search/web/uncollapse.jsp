<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<c:set var="sort" scope="request">title asc</c:set>
<%@ include file="inc/searchParams.jsp" %>
<% out.clear(); %>
<c:if test="${param.debug}" >
            <c:out value="${url}" />
            <br/>
            <c:out value="${exceptions}" />
        </c:if>
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
    
    <span class="textpole">(<fmt:message bundle="${lctx}"><x:out select="./str[@name='fedora.model']"/></fmt:message>)</span>
    <x:if select="./int[@name='pages_count'] != '0'">
    <span><x:out select="./int[@name='pages_count']"/></span>
    </x:if>
    <span><c:out value="${collapseCount}" escapeXml="false" /></span>
    <%--
    <x:choose>
        <x:when select="./str[@name='fedora.model'] = 'monograph2'">
            <%@ include file="inc/results/monograph.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'monographunit2'">
            <%@ include file="inc/results/monographunit.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'page2'">
            <%@ include file="inc/results/page.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'periodical2'">
            <%@ include file="inc/results/periodical.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'periodicalvolume2'">
            <%@ include file="inc/results/periodicalvolume.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'periodicalitem2'">
            <%@ include file="inc/results/periodicalitem.jsp" %>
        </x:when>
        <x:otherwise>
            <%@ include file="inc/results/default.jsp" %>
        </x:otherwise>
    </x:choose>
    --%>
    </div>
</x:forEach>
    <%@ include file="inc/paginationPageNum.jsp" %>
