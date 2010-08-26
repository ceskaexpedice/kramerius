<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<%@page import="cz.incad.kramerius.utils.FedoraUtils"%>

<%@ include file="initVars.jsp" %>
<c:url var="url" value="${kconfig.solrHost}/select/" >
    <c:param name="q" value="level:0" />
    <c:choose>
        <c:when test="${param.rows != null}" >
            <c:set var="rows" value="${param.rows}" scope="request" />
        </c:when>
        <c:otherwise>
            <c:set var="rows"  value="12" scope="request" />
        </c:otherwise>
    </c:choose>
    <c:param name="rows" value="${rows}" />
    <c:forEach var="fqs" items="${paramValues.fq}">
        <c:param name="fq" value="${fqs}" />
        <c:set var="filters" scope="request"><c:out value="${filters}" />&fq=<c:out value="${fqs}" /></c:set>
    </c:forEach>
    <c:param name="start" value="${param.offset}" />
    <c:param name="sort" value="created_date desc" />
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
            <%--<x:if select="./str[@name='fedora.model'] = 'page'">--%>
                <c:set var="itemUrl" ><c:out value="${itemUrl}"/>&format=<x:out select="./str[@name='page_format']"/></c:set>
            <%--</x:if>--%>
            <x:set select="./str[@name='PID']" var="pid" />
            <%
            if (fedora_model.equals("page")) {
                imagePid = "thumb?uuid=" + uuid.split("/@")[0];
            } else {
                String pageuuid = FedoraUtils.findFirstPagePid("uuid:" + uuid);
                if (pageuuid==null) pageuuid = uuid;
                imagePid = "thumb?uuid=" + pageuuid;
                //imagePid = "thumb?uuid=" + FedoraUtils.findFirstPagePid("uuid:" + uuid);
            }
            %>
            <div align="center" style="overflow:hidden; border:1px solid silver; width:100px; height:100px; float:left; margin:5px;"><a href="<c:out value="${itemUrl}" escapeXml="false" />" >
            <img align="middle" vspace="2" id="img_<c:out value="${uuid}"/>" src="<%=imagePid%>&scaledHeight=96" border="0" title="<x:out select="./str[@name='root_title']"/>"  /></a>
            </div>
        </x:forEach>
        
        
        
        
        
    </c:otherwise>
</c:choose>
<c:if test="${param.debug}" >
    <c:out value="${url}" /><br/>
    <c:out value="${param.parentPid}" />
</c:if>

