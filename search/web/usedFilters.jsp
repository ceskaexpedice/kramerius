<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<!-- pouzite filtry -->
<x:if select="$doc/response/lst/lst/arr[@name='fq'] or //str[@name='fq']">
        
                <x:forEach select="$doc/response/lst/lst/arr[@name='fq']/str">
                        <c:set var="facetName"><x:out select="substring-before(.,':')" /></c:set>
                        <c:set var="facetName"><c:out value="${fn:replace(facetName, '\"', '')}" /></c:set>
                        <c:set var="facetValue"><x:out select="substring-after(.,':')" escapeXml="false" /></c:set>
                        <c:set var="facetValue"><c:out value="${fn:replace(facetValue, '\"', '')}" /></c:set>
                        <c:if test="${facetName == 'fedora.model'}">
                            <c:set var="facetName"><fmt:message ><c:out value="${facetName}" /></fmt:message></c:set>
                            <c:set var="facetValue"><fmt:message ><c:out value="${facetValue}" /></fmt:message></c:set>
                        </c:if>
                         - <a title="" class="mainNav"
                            href="javascript:removeNavigation('<x:out select="." />');">
                            <c:out value="${facetValue}"/>&#160;<img src="img/x.png"  border="0" 
                            title="<fmt:message key="remove_criteria"/>: <c:out value="${facetName}"/>"/>
                        </a>
                </x:forEach>
                <x:forEach select="$doc/response/lst/lst/str[@name='fq']">
                    <c:set var="facetName"><x:out select="substring-before(.,':')" /></c:set>
                    <c:set var="facetName"><c:out value="${fn:replace(facetName, '\"', '')}" /></c:set>
                    <c:set var="facetValue"><x:out select="substring-after(.,':')" escapeXml="false" /></c:set>
                    <c:set var="facetValue"><c:out value="${fn:replace(facetValue, '\"', '')}" /></c:set>
                    <c:if test="${facetName == 'fedora.model'}">
                        <c:set var="facetName"><fmt:message ><c:out value="${facetName}" /></fmt:message></c:set>
                        <c:set var="facetValue"><fmt:message ><c:out value="${facetValue}" /></fmt:message></c:set>
                    </c:if>
                         - <a title="" class="mainNav"
                            href="javascript:removeNavigation('<x:out select="." />');">
                            <c:out value="${facetValue}"/>&#160;<img src="img/x.png"  border="0" 
                            title="<fmt:message key="remove_criteria"/>: <c:out value="${facetName}"/>"/>
                        </a>
                </x:forEach>
    
</x:if>
<!-- konec pouzite filtry -->

