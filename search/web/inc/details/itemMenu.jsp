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
<c:set var="urlSolr" >
    <c:out value="${kconfig.solrHost}" />?q=PID:"<c:out value="${param.pid}" />"
</c:set>
<%--
Get Biblio mods
--%>
<c:set var="models" value="${fn:split(param.path, '/')}"/>
<c:set var="pids" value="${fn:split(param.pid_path, '/')}"/>
<c:set var="lastModel" value="${models[fn:length(models)-1]}" />
<c:set var="model_path" value="itemTree" scope="request" />
<c:set var="href" value="#{href}" />
<c:set var="label" value="#{label}" />
<div id="itemTree">
<c:forEach var="uuid" varStatus="status" items="${fn:split(param.pid_path, '/')}">
    <c:set var="obj" value="#tabs_${status.count}"/>
    <script language="javascript">
        $(document).ready(function(){
            var obj = "#tabs_<c:out value="${status.count}" />";
            $(obj).tabs({ tabTemplate: '<li><a href="<c:out value="${href}" />"><c:out value="${label}" /></a><img src="img/empty.gif" class="op_list <c:out value="${label}" />" onclick="showList(this, \''+obj+'\', \'<c:out value="${label}" />\')" /></li>' });
           //var tabs<c:out value="${status.count}" /> = $(obj).tabs();
           getItemRels("<c:out value="${pids[status.count-1]}" />",
                "<c:out value="${pids[status.count]}" />",
                <c:out value="${status.count}" />,
                <c:out value="${status.count == fn:length(models)}" />
            );
        });
    </script>
    <div id="tabs_<c:out value="${status.count}" />" style="padding:2px;">
        <ul><li><a href="#tab<c:out value="${status.count}" />-<c:out value="${models[status.count -1]}" />" ><fmt:message><c:out value="${models[status.count -1]}" /></fmt:message>
        </a><img src="img/empty.gif" class="op_list" onclick="showList(this, '#tabs_<c:out value="${status.count}" />', '<c:out value="${models[status.count -1]}" />')" /></li></ul>
        <div id="tab<c:out value="${status.count}" />-<c:out value="${models[status.count -1]}" />" >
    <jsp:useBean id="uuid" type="java.lang.String" />
    <c:set var="urlStr" >
        <c:out value="${kconfig.fedoraHost}" />/get/uuid:<c:out value="${uuid}" />/BIBLIO_MODS
    </c:set>
    <c:set var="display" value="none"/>
    
    <c:catch var="exceptions"> 
        <c:import url="${urlStr}" var="xml2" charEncoding="UTF-8"  />
        <c:import url="inc/details/xsl/${models[status.count -1]}.jsp?display=${display}&language=${param.language}${others}" var="xslt" charEncoding="UTF-8"  />
    </c:catch>
    <c:choose>
        <c:when test="${exceptions != null}" >
            <c:out value="${exceptions}" />
        </c:when>
        <c:otherwise>
            <div class="relList" style="display:none;" id="list-<c:out value="${models[status.count -1]}" />"></div>
            <div id="info-<c:out value="${models[status.count -1]}" />"><x:transform doc="${xml2}"  xslt="${xslt}"  >
                <x:param name="pid" value="${uuid}"/>
            </x:transform></div>
        </c:otherwise>
    </c:choose>
    
</c:forEach>
<c:forEach var="model" varStatus="status" items="${models}">
    </div></div>
</c:forEach>
</div>