<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>


<tr>
    <!-- rdf.kramerius.hasPage:"info:fedora/PID" -->
    <td >
            <x:choose>
            <x:when select="./str[@name='title_to_show']">
                <a href="<c:out value="${kconfig.fedoraHost}" />/get/<x:out select="./str[@name='PID']"/>">
                    <b><x:out select="./str[@name='title_to_show']"/></b>
                </a> 
                <br/>
                <fmt:message><x:out select="./str[@name='fedora.model']"/></fmt:message>: <x:out select="./arr[@name='dc.title']"/>
            </x:when>
            <x:otherwise>
                <a href="<c:out value="${kconfig.fedoraHost}" />/get/<x:out select="./str[@name='PID']"/>">
                <b><x:out select="./arr[@name='dc.title']"/></b>
                </a> 
            </x:otherwise>
            </x:choose>
    </td>
    <td class="textpole">(<fmt:message><x:out select="./str[@name='fedora.model']"/></fmt:message>)</td>
</tr>
