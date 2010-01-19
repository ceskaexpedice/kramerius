<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
<tr>
    <!-- rdf.kramerius.hasPage:"info:fedora/PID" -->
    <td >
                <a href="./item.jsp?pid=<x:out select="./str[@name='PID']"/>&&model=<x:out select="./str[@name='fedora.model']"/>">
                <b><x:out select="./arr[@name='dc.title']"/> -- <x:out select="./str[@name='PID']"/></b>
                </a> 
    </td>
    <td class="textpole">(<x:out select="./str[@name='fedora.model']"/>)</td>
</tr>