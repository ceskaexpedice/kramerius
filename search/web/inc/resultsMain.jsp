<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>



<%@page import="cz.incad.kramerius.utils.FedoraUtils"%><table id="results_main" cellspacing="0" cellpadding="0" border="0">
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
        <x:if select="./str[@name='fedora.model'] = 'page'">
            <c:set var="itemUrl" ><c:out value="${itemUrl}"/>&format=<x:out select="./str[@name='page_format']"/></c:set>
        </x:if>
        <x:set select="./str[@name='PID']" var="pid" />
    <tr id="res_<c:out value="${uuid}"/>" class="result r<c:out value="${status.count % 2}" />">
        <%//@ include file="../admin/resultOptions.jsp" %>
        <x:forEach select="//response/lst[@name='collapse_counts']/lst[@name='results']/lst">
            <x:if select="./@name=$pid">
            <c:set var="collapseCount" >
                <a href="javascript:uncollapse('<c:out value="${root_pid}" />', 'uncollapsed_<c:out value="${uuid}"/>', 0)"><img src="img/collapsed.png" 
                   alt="<x:out select="./int[@name='collapseCount']/text()"/> <fmt:message bundle="${lctx}">collapsed</fmt:message>"
                   title="<x:out select="./int[@name='collapseCount']/text()"/> <fmt:message bundle="${lctx}">collapsed</fmt:message>" border="0" /></a>
            </c:set>  
            </x:if>
        </x:forEach>
        
    <td style="float:left;">
    <img src="img/empty.gif" 
    <c:if test="${status.count > 5}" >
    class="plus" onclick="$('#more_<c:out value="${uuid}"/>').toggle();$('#img_<c:out value="${uuid}"/>').toggle();$(this).toggleClass('minus')" 
    </c:if>
    />
    </td>
    <td>
    <% 
    if(fedora_model.equals("page")){
        imagePid = "thumb?uuid=" + uuid;
    }else{
        imagePid = "thumb?uuid=" + FedoraUtils.findFirstPagePid("uuid:" + uuid);
    }
    %>
    <img id="img_<c:out value="${uuid}"/>" 
    <c:if test="${status.count > 5}" >
        style="display:none;"
    </c:if>
    src="<%=imagePid%>&scaledHeight=64" 
    border="1"   />
    </td>
    <td>
    <a href="<c:out value="${itemUrl}" escapeXml="false" />" ><b><x:out select="./str[@name='root_title']"/></b></a>
    <span class="textpole">(<fmt:message bundle="${lctx}"><x:out select="./str[@name='fedora.model']"/></fmt:message>)</span>
    <x:if select="./int[@name='pages_count'] != '0'">
    <span class="count"><x:out select="./int[@name='pages_count']"/></span>
    </x:if>
    <span><c:out value="${collapseCount}" escapeXml="false" /></span>
    <%@ include file="results/default.jsp" %>
    <%--
    <x:choose>
        <x:when select="./str[@name='fedora.model'] == 'monograph2'">
            <%@ include file="results/monograph.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'monographunit2'">
            <%@ include file="results/monographunit.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'page2'">
            <%@ include file="results/page.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'periodical2'">
            <%@ include file="results/periodical.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'periodicalvolume2'">
            <%@ include file="results/periodicalvolume.jsp" %>
        </x:when>
        <x:when select="./str[@name='fedora.model'] = 'periodicalitem'">
            <%@ include file="results/periodicalitem.jsp" %>
        </x:when>
        <x:otherwise>
            <%@ include file="results/default.jsp" %>
        </x:otherwise>
    </x:choose>
    --%>
    </td>
    
    </tr>
    <tr class="uncollapsed r<c:out value="${status.count % 2}"/>"><td></td><td></td>
    <td id="uncollapsed_<c:out value="${uuid}"/>"></td>
    </tr>
</x:forEach>
    </table>