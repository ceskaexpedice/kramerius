<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page isELIgnored="false"%>
<%@ page import="java.util.*, cz.incad.Kramerius.*, cz.incad.Solr.*" %>
<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />


<%-- fill path up to the end --%>
<%
//ArrayList<String> pids = new ArrayList<String>();
//pids.addAll(request.getParameter("pid_path").split("/"));
ArrayList<String> pids =  new ArrayList<String>(Arrays.asList((String [])request.getParameter("pid_path").split("/")));
ArrayList<String> models =  new ArrayList<String>(Arrays.asList((String [])request.getParameter("path").split("/")));

//ArrayList<String> models = new ArrayList<String>();
//models.addAll(request.getParameter("path").split("/"));
FedoraUtils.fillFirstPagePid(pids, models);

getServletContext().setAttribute("pids", pids);
getServletContext().setAttribute("models", models);
%>

<%--
Get Biblio mods
<c:set var="models" value="${fn:split(param.path, '/')}"/>
<c:set var="pids" value="${fn:split(param.pid_path, '/')}"/>
--%>
<c:set var="lastModel" value="${models[fn:length(models)-1]}" />
<c:set var="model_path" value="itemTree" scope="request" />
<c:set var="href" value="#{href}" />
<c:set var="label" value="#{label}" />
<c:set var="level" value="0"/>
<c:if test="${!empty param.level}" >
    <c:set var="level" value="${param.level}"/>
</c:if>

<c:forEach var="uuid" varStatus="status" items="${pids}">
    <c:set var="cur_level" value="${status.count + level}"/>
    <c:set var="obj" value="#tabs_${cur_level}"/>
    <script language="javascript">
        $(document).ready(function(){
            var obj = "<c:out value="${obj}" />";
            $(obj).tabs({ tabTemplate: '<li><a href="<c:out value="${href}" />"><c:out value="${label}" /></a><img width="12px" src="img/empty.gif" class="op_list" onclick="showList(this, \''+obj+'\', \'<c:out value="${label}" />\')" /></li>' });
           
           getItemRels("<c:out value="${pids[status.count-1]}" />",
                "<c:out value="${pids[status.count]}" />",
                <c:out value="${cur_level}" />,
                //    false
                <c:out value="${status.count == fn:length(models)}" />
            );
        });
    </script>
    <div id="tabs_<c:out value="${cur_level}" />" style="padding:2px;" pid="<c:out value="${uuid}" />">
        <ul><li><a href="#tab<c:out value="${status.count}" />-<c:out value="${models[status.count -1]}" />" ><fmt:message><c:out value="${models[status.count -1]}" /></fmt:message>
        </a><img width="12px" src="img/empty.gif" class="op_list" onclick="showList(this, '#tabs_<c:out value="${cur_level}" />', '<c:out value="${models[status.count -1]}" />')" /></li></ul>
        <div id="tab<c:out value="${cur_level}" />-<c:out value="${models[status.count -1]}" />" >
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
            <c:out value="${xml2}" />
            <c:out value="${xslt}" />
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