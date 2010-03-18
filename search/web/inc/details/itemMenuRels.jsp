<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page isELIgnored="false"%>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<%@ include file="../initVars.jsp" %>
<c:url var="url" value="${kconfig.solrHost}/select/select" >
    <c:param name="q" >parent_pid:"<c:out value="${param.pid}" />"</c:param>
    <c:param name="fl" value="PID,details,fedora.model,page_format" />
    <c:param name="sort" value="fedora.model asc" />
    <c:param name="rows" value="1000" />
</c:url>

<c:catch var="exceptions"> 
        <c:import url="${url}" var="xml" charEncoding="UTF-8"  />
        <x:parse  xml="${xml}" var="doc"  />
        <%--
        <c:import url="xsl/relsextDetails.jsp?language=${param.language}" var="xslt" charEncoding="UTF-8"   />
        <c:import url="xsl/1.xsl" var="xslt" charEncoding="UTF-8"  />
        --%>
    </c:catch>
    <c:choose>
        <c:when test="${exceptions != null}" >
            error <c:out value="${exceptions}" />
        </c:when>
        <c:otherwise>
            <%-- generate tabs header --%>  
            <% out.clear(); %>
            <x:if select="$doc/response/result/@numFound != '0'">
            <div id="tabs_<c:out value="${param.pid}" />">
            <ul>
            <x:forEach varStatus="status" select="$doc/response/result/doc">
                <x:if select="not(preceding-sibling::*[1]/str[@name='fedora.model'] = ./str[@name='fedora.model']/text())">
                    <li><a href="#tabs-<x:out select="./str[@name='fedora.model']" />"><x:out select="./str[@name='fedora.model']" /></a></li>
                </x:if>
            </x:forEach>
            </ul>
            <%--generate tabs contents --%>
            <x:forEach varStatus="status" select="$doc/response/result/doc">
                <x:if select="not(preceding-sibling::*[1]/str[@name='fedora.model'] = ./str[@name='fedora.model']/text())">
                    <div id="tabs-<x:out select="./str[@name='fedora.model']" />" class="relList">
                </x:if>
                <c:set var="cpid"><x:out select="./str[@name='PID']" /></c:set>
                <div id="<x:out select="./str[@name='PID']" />" 
                 class="relItem" title="<x:out select="./str[@name='page_format']" />" >
                    <x:forEach select="./arr[@name='details']/str" >
                        <c:set var="s"><fmt:message><x:out select="." /></fmt:message></c:set>
                        <c:out value="${fn:replace(s, '???','')}" />&#160;
                    </x:forEach>
                </div>
                <x:if select="not(following-sibling::*[1]/str[@name='fedora.model'] = ./str[@name='fedora.model']/text())">
                    </div>
                </x:if>
            </x:forEach>
        </div>  
        </x:if>
        </c:otherwise>
    </c:choose>