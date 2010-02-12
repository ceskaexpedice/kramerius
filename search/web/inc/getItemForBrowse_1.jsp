<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>

<c:choose>
    <c:when test="${param.language != null}" >
        <fmt:setLocale value="${param.language}" />
    </c:when>
</c:choose>

<fmt:setBundle basename="labels" />
<fmt:setBundle basename="labels" var="bundleVar" />
<%@ include file="initVars.jsp" %>

<c:url var="url" value="${kconfig.solrHost}" >
    <c:param name="q" value="parent_pid:\"${param.pid}\" OR PID:\"${param.pid}\"" />
    <c:choose>
        <c:when test="${param.rows != null}" >
            <c:set var="rows" value="${param.rows}" scope="request" />
        </c:when>
        <c:otherwise>
            <c:set var="rows" value="1" scope="request" />
        </c:otherwise>
    </c:choose>
    <c:param name="rows" value="${rows}" />
    <c:param name="facet" value="true" />
    <c:param name="facet.mincount" value="1" />
    <c:param name="facet.field" value="fedora.model" />
    <c:param name="start" value="${param.offset}" />
    <c:param name="sort" value="level asc" />
    
</c:url>
<c:catch var="exceptions"> 
    <c:import url="${url}" var="xml" charEncoding="UTF-8" />
</c:catch>
<c:choose>
    <c:when test="${exceptions != null}">
        <c:out value="${exceptions}" />
        <c:out value="${url}" />
        <c:out value="${xml}" />
    </c:when>
    <c:otherwise>
        <x:parse var="doc" xml="${xml}"  />
        <%@ include file="proccessFacets.jsp" %>
        <c:set var="prefix" value="uuid:" />
        <c:set var="uuidSimple" >${fn:replace(param.pid, "uuid:", "")}</c:set>
        <c:set var="numDocs" scope="request" >
            <x:out select="$doc/response/result/@numFound" />
        </c:set><%//out.clear();%>
        <div class="resultsInTree" >
            <x:forEach varStatus="status" select="$doc/response/result/doc">
                <div class="resultInTree" >
                    <c:set var="uuid" >
                        <x:out select="./str[@name='PID']"/>
                    </c:set>
                    <c:set var="uuidSimple" >
                        <x:out select="substring-after(./str[@name='PID'], 'uuid:')"/>
                    </c:set>
                    <x:choose>
                        <x:when select="./str[@name='fedora.model'] = 'info:fedora/model:monograph'">
                            <%@ include file="results/monograph.jsp" %>
                        </x:when>
                        <x:when select="./str[@name='fedora.model'] = 'info:fedora/model:monographunit'">
                            <%@ include file="results/monographunit.jsp" %>
                        </x:when>
                        <x:when select="./str[@name='fedora.model'] = 'info:fedora/model:page'">
                            <%@ include file="results/page.jsp" %>
                        </x:when>
                        <x:when select="./str[@name='fedora.model'] = 'info:fedora/model:periodical'">
                            <%@ include file="results/periodical.jsp" %>
                        </x:when>
                        <x:when select="./str[@name='fedora.model'] = 'info:fedora/model:periodicalvolume'">
                            <%@ include file="results/periodicalvolume.jsp" %>
                        </x:when>
                        <x:when select="./str[@name='fedora.model'] = 'info:fedora/model:periodicalitem'">
                            <%@ include file="results/periodicalitem.jsp" %>
                        </x:when>
                        <x:otherwise>
                            <x:out select="./str[@name='fedora.model']" />
                            <%@ include file="results/default.jsp" %>
                        </x:otherwise>
                    </x:choose>
                </div>
            </x:forEach>    
            
            <div id="paths"><%

            Facet f = facets.get("fedora.model");
            String filter;
            if ((f != null) && (f.infos.size() > 0)) {
                for (int k = 0; k < f.infos.size(); k++) {
                    if (!f.infos.get(k).name.equals(request.getParameter("model"))) {
                        filter = "fedora.model:\\\"" + f.infos.get(k).name + "\\\" AND " +
                                "parent_pid:\\\"" + request.getParameter("pid") + "\\\"";
                %>
                <div>
                    <a href='javascript:searchInTree("<c:out value="${uuid}"/>", "<%=filter%>", "node_<%=k%>_<c:out value="${uuidSimple}"/>" );'><%=f.infos.get(k).displayName%> (<%=f.infos.get(k).count%>)</a>
                <div class="resultsInTree" id="node_<%=k%>_<c:out value="${uuidSimple}"/>"></div>
                </div>
                <%
                    }
                }
            }
                %>
            </div>
        </div>
    </c:otherwise>
</c:choose>

